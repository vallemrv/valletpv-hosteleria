# @Author: Manuel Rodriguez <valle>
# @Date:   10-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-17T00:43:08+01:00
# @License: Apache license vesion 2.0

from django.conf import settings
from channels.generic.websocket import AsyncWebsocketConsumer
import json
from gestion.tools.config_logs import log_comunicaciones as logger
from django.db import close_old_connections
from channels.db import database_sync_to_async

class ImpresionConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        try:
            self.print_name = self.scope['url_route']['kwargs']['print_name']
            self.print_group_name = settings.EMPRESA + "_impresion_" + self.print_name
            
            await self.channel_layer.group_add(
                self.print_group_name,
                self.channel_name
            )
            await self.accept()
            
            logger.info(f"🖨️  Impresora conectada: {self.print_name}")
            
        except Exception as e:
            logger.error(f"❌ Error conectando impresora: {e}")
            await self.close()

    async def disconnect(self, close_code):
        try:
            await self.channel_layer.group_discard(
                self.print_group_name,
                self.channel_name
            )
            logger.info(f"🖨️  Impresora desconectada: {self.print_name}")
            
        except Exception as e:
            logger.error(f"❌ Error desconectando impresora: {e}")

    # Receive message from WebSocket
    async def receive(self, text_data):
        try:
            text_data_json = json.loads(text_data)
            message = text_data_json['message']

            await self.channel_layer.group_send(
                self.print_group_name,
                {
                    'type': 'send_message',
                    'message': message
                }
            )
            
        except (json.JSONDecodeError, KeyError) as e:
            logger.error(f"❌ Error mensaje impresora: {e}")
        except Exception as e:
            logger.error(f"❌ Error receive impresora: {e}")

    # Receive message from room group
    async def send_message(self, event):
        try:
            message = event.get('message') or event.get('content', '')
            await self.send(text_data=json.dumps({'message': message}))
            
        except Exception as e:
            logger.error(f"❌ Error enviando a impresora: {e}")


class ComunicacionConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        try:
            self.roon = self.scope['url_route']['kwargs']["receptor"]
            
            # Si el receptor es "devices", verificar que el dispositivo esté activo
            if self.roon == "devices":
                uid = self.scope.get('query_string', b'').decode('utf-8')
                if uid.startswith('uid='):
                    uid = uid.split('uid=')[1].split('&')[0]
                    
                    dispositivo_activo = await self.verificar_dispositivo_activo(uid)
                    
                    if not dispositivo_activo:
                        logger.warning(f"🚫 Dispositivo inactivo: {uid[:8]}...")
                        await self.close(code=4001)
                        return
                    
                    logger.info(f"✅ Dispositivo activo: {uid[:8]}...")
                else:
                    logger.warning(f"🚫 Conexión devices sin UID")
                    await self.close(code=4002)
                    return
            
            self.group_name = settings.EMPRESA + "_comunicaciones_" + self.roon
            
            await self.channel_layer.group_add(
                self.group_name,
                self.channel_name
            )
            await self.accept()
            
            logger.info(f"📡 Receptor conectado: {self.roon}")
            
        except Exception as e:
            logger.error(f"❌ Error conectando receptor: {e}")
            await self.close()
    
    @database_sync_to_async
    def verificar_dispositivo_activo(self, uid):
        """Verificar si el dispositivo existe y está activo"""
        try:
            from gestion.models.dispositivos import Dispositivo
            dispositivo = Dispositivo.objects.get(uid=uid)
            return dispositivo.activo
        except Dispositivo.DoesNotExist:
            return False
        except Exception as e:
            logger.error(f"❌ Error verificando dispositivo: {e}")
            return False

    async def disconnect(self, close_code):
        try:
            await self.channel_layer.group_discard(
                self.group_name,
                self.channel_name
            )
            logger.info(f"📡 Receptor desconectado: {self.roon}")
            
        except Exception as e:
            logger.error(f"❌ Error desconectando receptor: {e}")

    # Receive message from WebSocket
    async def receive(self, text_data):
        try:
            await database_sync_to_async(close_old_connections)()
            text_data_json = json.loads(text_data)
            message = text_data_json['content']

            await self.channel_layer.group_send(
                self.group_name,
                {
                    'type': 'send_message',
                    'content': message
                }
            )
            
        except (json.JSONDecodeError, KeyError) as e:
            logger.error(f"❌ Error mensaje receptor: {e}")
        except Exception as e:
            logger.error(f"❌ Error receive receptor: {e}")

    # Receive message from room group
    async def send_message(self, event):
        try:
            message = event.get('message') or event.get('content', '')
            await self.send(text_data=message)
            
        except Exception as e:
            logger.error(f"❌ Error enviando a receptor: {e}")
