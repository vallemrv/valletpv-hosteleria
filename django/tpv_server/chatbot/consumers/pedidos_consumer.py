import json
import uuid
from channels.generic.websocket import AsyncWebsocketConsumer
from django.conf import settings
from gestion.tools.config_logs import log_debug_chatbot as logger
from ..processors.pedidos_processor import PedidosProcessor
from langchain_core.messages import HumanMessage
from asgiref.sync import sync_to_async
from gestion.models.dispositivos import Dispositivo


class PedidosConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        try:
            query_params = self.scope['query_string'].decode('utf-8').split('&')
            # Manejo más robusto por si falta '=' en algún parámetro
            params = {}
            for param in query_params:
                parts = param.split('=', 1)
                if len(parts) == 2:
                    params[parts[0]] = parts[1]
                elif len(parts) == 1 and parts[0]:  # Parámetro sin valor
                    params[parts[0]] = ''

            self.device_uid = params.get('uid', '')
            self.camarero_id = params.get('camarero_id', '')

            if not self.device_uid:
                # Aceptamos para poder enviar el mensaje de error
                await self.accept()
                await self.send(text_data=json.dumps({
                    'type': 'error',
                    'message': 'Falta parámetro uid del dispositivo.'
                }))
                # Cerramos inmediatamente después de enviar el error
                await self.close(code=4001)  # Código personalizado para error de parámetros
                return

            # --- Validación del Dispositivo ---
            device_info = await self.validate_device(self.device_uid)
            if not device_info:
                # Aceptamos para poder enviar el mensaje de error
                await self.accept()
                await self.send(text_data=json.dumps({
                    'type': 'error',
                    'message': 'Dispositivo no autorizado o inactivo.'
                }))
                # Cerramos inmediatamente después de enviar el error
                await self.close(code=4003)  # Código personalizado para dispositivo no autorizado
                return

            self.device_info = device_info

            # --- Continuar con la conexión normal si el dispositivo es válido ---
            room = self.scope['url_route']['kwargs']['room']
            empresa = settings.EMPRESA  # Asegúrate que settings.EMPRESA esté disponible
            self.group_name = f"{room}-{empresa}-device-{self.device_uid}"

            # Añadir al grupo ANTES de aceptar completamente
            await self.channel_layer.group_add(self.group_name, self.channel_name)

            # Aceptar la conexión de forma definitiva
            await self.accept()

            # Inicializar MessageProcessor como dispositivo de pedidos (no administrador)
            self.message_processor = PedidosProcessor(
                self, 
                camarero_id=self.camarero_id if self.camarero_id else None
            )

            # Enviar mensaje simple al conectarse
            await self.send(text_data=json.dumps({
                'type': 'welcome',
                'message': 'Pídeme lo que quieras. ¿En qué puedo ayudarte?',
                'sender': 'bot'
            }))

        except KeyError as e:
            logger.error(f"Error de configuración en dispositivo: Falta la clave {e} en scope o settings.")
            await self.close(code=4004)  # Código para error de configuración
        except Exception as e:
            logger.error(f"Error inesperado durante la conexión del dispositivo: {e}", exc_info=True)
            await self.close(code=4005)  # Código para error genérico del servidor

    async def disconnect(self, close_code):
        # Es buena práctica verificar si group_name fue asignado antes de usarlo
        if hasattr(self, 'group_name'):
            await self.channel_layer.group_discard(self.group_name, self.channel_name)

    async def receive(self, text_data):
        """
        Recibe mensajes del dispositivo de pedidos.
        Los dispositivos de pedidos pueden enviar mensajes de texto o audio para comandar pedidos.
        """
        try:
            data = json.loads(text_data)
            message = data.get('text', '')
            sender = data.get('sender', '')
            type_message = data.get('type', 'message')
            message_id = str(uuid.uuid4())

            if type_message == "message" and sender == 'user' and message.strip():
                # Crear mensaje de texto para el agente de pedidos
                user_input = HumanMessage(
                    content=[
                        {
                            "role": "user",  # Dispositivo actúa como usuario
                            "text": message,
                            "type": type_message,
                        }
                    ]
                )

                # Procesar el mensaje como pedido
                await self.message_processor.generate_response(user_input, message_id)

            elif type_message != "message" or sender != 'user':
                # Procesamiento de audio (similar a chatbot_consumer)
                audio_base64 = data.get('audio', '')
                if not audio_base64:
                    logger.error(f"No se recibió audio en el mensaje del dispositivo {self.device_uid}.")
                    await self.send(text_data=json.dumps({
                        'type': 'error',
                        'message': 'No se recibió audio en el mensaje.'
                    }))
                    return
                
                # Crear mensaje de audio para transcripción INCLUYENDO información del camarero
                user_input = HumanMessage(
                    content=[
                        {
                            "type": "text",
                            "text": "Necesitamos una transcripcion del audio. Lo mas fiel posible.",
                         },
                        {
                            "data": audio_base64,
                            "type": "media",
                            "mime_type": "audio/webm",
                        }
                    ]
                )
                
                # Procesar el audio como pedido
                await self.message_processor.generate_response(user_input, message_id)
                await self.send(text_data=json.dumps({
                    'type': 'error',
                    'message': 'Mensaje vacío o formato inválido.'
                }))

        except json.JSONDecodeError:
            logger.error(f"JSON inválido recibido del dispositivo {self.device_uid}")
            await self.send(text_data=json.dumps({
                'type': 'error',
                'message': 'Formato de mensaje inválido.'
            }))
        except Exception as e:
            logger.error(f"Error procesando mensaje del dispositivo {self.device_uid}: {e}", exc_info=True)
            await self.send(text_data=json.dumps({
                'type': 'error',
                'message': 'Error interno procesando el mensaje.'
            }))

    async def send_message(self, event):
        """Método para recibir mensajes del grupo y enviarlos al dispositivo"""
        await self.send(text_data=json.dumps({
            'type': "message",
            'message_id': event.get('message_id'),
            'message': event['message'],
            'sender': event['sender'],
            'timestamp': event.get('timestamp')
        }))

    @sync_to_async
    def validate_device(self, device_uid):
        """
        Valida si el dispositivo existe y está activo.
        
        Args:
            device_uid: UID único del dispositivo
            
        Returns:
            dict: Información del dispositivo si es válido, None si no
        """
        try:
            dispositivo = Dispositivo.objects.get(uid=device_uid, activo=True)
            
            return {
                'id': dispositivo.id,
                'uid': dispositivo.uid,
                'nombre': dispositivo.descripcion or f"Dispositivo {dispositivo.uid}",
                'tipo': 'pedidos',  # Tipo fijo para dispositivos de pedidos
                'activo': dispositivo.activo
            }
            
        except Dispositivo.DoesNotExist:
            logger.error(f"Dispositivo con UID {device_uid} no encontrado o inactivo.")
            return None
        except Exception as e:
            logger.error(f"Error validando dispositivo {device_uid}: {e}", exc_info=True)
            return None

    async def send_pedido_confirmation(self, event):
        """Método específico para enviar confirmaciones de pedidos al dispositivo"""
        await self.send(text_data=json.dumps({
            'type': "pedido_confirmation",
            'pedido_id': event.get('pedido_id'),
            'message': event['message'],
            'status': event.get('status', 'success'),
            'total': event.get('total'),
            'items': event.get('items', [])
        }))

    async def send_error(self, event):
        """Método específico para enviar errores al dispositivo"""
        await self.send(text_data=json.dumps({
            'type': "error",
            'error_code': event.get('error_code'),
            'message': event['message'],
            'details': event.get('details')
        }))
