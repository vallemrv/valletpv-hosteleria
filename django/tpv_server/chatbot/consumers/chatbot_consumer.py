import json
import uuid
from channels.generic.websocket import AsyncWebsocketConsumer
from django.conf import settings
from gestion.tools.config_logs import log_debug_chatbot as logger
from ..processors.message_processor import MessageProcessor
from langchain_core.messages import HumanMessage
from django.contrib.auth import authenticate
from asgiref.sync import sync_to_async


class ChatConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        try:
            query_params = self.scope['query_string'].decode('utf-8').split('&')
            params = {}
            for param in query_params:
                parts = param.split('=', 1)
                if len(parts) == 2:
                    params[parts[0]] = parts[1]
                elif len(parts) == 1 and parts[0]:
                    params[parts[0]] = ''

            self.user = params.get('user', '')
            self.token = params.get('token', '')

            if not self.user or not self.token:
                 logger.warning(f"❌ Conexión rechazada - Faltan parámetros (user={self.user})")
                 await self.accept()
                 await self.send(text_data=json.dumps({
                     'type': 'error',
                     'message': 'Faltan parámetros de usuario o token.'
                 }))
                 await self.close(code=4001)
                 return

            # Validación del Token
            if not await self.validate_token(self.token, self.user):
                logger.warning(f"❌ Token inválido - Usuario: {self.user}")
                await self.accept()
                await self.send(text_data=json.dumps({
                    'type': 'error',
                    'message': 'Token inválido o expirado.'
                }))
                await self.close(code=4003)
                return

            # Continuar con conexión normal
            room = self.scope['url_route']['kwargs']['room']
            empresa = settings.EMPRESA
            self.group_name = f"{room}-{empresa}-{self.user}"

            await self.channel_layer.group_add(self.group_name, self.channel_name)
            await self.accept()
            
            logger.info(f"✅ Chatbot conectado - Usuario: {self.user}, Grupo: {self.group_name}")

            self.message_processor = MessageProcessor(self)

        except KeyError as e:
             logger.error(f"❌ Error configuración - Falta clave: {e}")
             await self.close(code=4004)
        except Exception as e:
            logger.error(f"❌ Error conexión: {e}", exc_info=True)
            await self.close(code=4005)

    async def disconnect(self, close_code):
        if hasattr(self, 'group_name'):
            await self.channel_layer.group_discard(self.group_name, self.channel_name)
            logger.info(f"🔌 Desconectado - Usuario: {self.user}, Código: {close_code}")
        else:
            logger.info(f"🔌 Desconectado sin grupo - Código: {close_code}")
        # Puedes añadir limpieza adicional si es necesario (ej: self.audio_processor.cleanup())

    # --- Tus otros métodos (receive, validate_token, etc.) irían aquí ---

    async def receive(self, text_data):
        data = json.loads(text_data)
        message = data.get('text', '')
        sender = data.get('sender', '')
        type_message = data.get('type', '')
        message_id =  str(uuid.uuid4())
        if type_message == "message" and sender == 'user':
            user_input = HumanMessage(
                content=[
                    {
                    "role": sender,
                    "text":message,
                    "type": type_message,
                   }
                ]
            ) 

        else:
            audio_base64 = data.get('audio', '')
            if not audio_base64:
                logger.error("No se recibió audio en el mensaje.")
                return
            user_input = HumanMessage(
                content=[
                    {
                    "type": "text",
                    "text": "Necesitamos una transcripcion del audio. Lo mas fiel posible.",
                    },
                    {
                        "data":audio_base64,
                        "type": "media",
                        "mime_type": "audio/webm",
                   }
                ]
            )  
        
        await self.message_processor.generate_response(user_input, message_id)       
       

    async def send_message(self, event):
        """Método para recibir mensajes del grupo y enviarlos al cliente"""
        await self.send(text_data=json.dumps({
            'type': "message",
            'message_id': event.get('message_id'),  # Pasar el message_id
            'message': event['message'],
            'sender': event['sender']
        }))

    async def validate_token(self, token, user_id):
        user = await sync_to_async(authenticate)(pk=user_id, token=token)
        if user:
            logger.info(f"Token válido para el usuario {user_id}.")
            return True
        else:
            logger.error(f"Token inválido para el usuario {user_id}.")
            return False