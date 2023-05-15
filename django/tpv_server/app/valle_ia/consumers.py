from django.conf import settings
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from .tools.texto import preguntar_gpt
from django.contrib.auth import authenticate
import json
import asyncio
import threading

class ChatConsumer(AsyncWebsocketConsumer):

    @database_sync_to_async
    def async_authenticate(self, user, token):
        user = authenticate(pk=user, token=token)
        return user
    
    async def connect(self):
       self.user = self.scope['url_route']['kwargs']['user']
       self.group_name = settings.EMPRESA + "__gestion_ia__" + self.user
       
       await self.channel_layer.group_add(
            self.group_name,
            self.channel_name
        )

       await self.accept()

    async def disconnect(self, close_code):
        await self.channel_layer.group_discard(
            self.group_name,
            self.channel_name
        )

    async def receive(self, text_data):
        text_data_json = json.loads(text_data) 
        token = text_data_json.get("token", {"user":0, "token": ""})
        user = await self.async_authenticate(token["user"], token["token"])
        
        if user is None:
             await self.channel_layer.group_send(
                self.group_name,
                {
                    'type': 'send_message',
                    'message': "Error en la autentificacion. usuario no valido."
                }
            )
        else:
            message = text_data_json['message']
            
            # Ejecutar acciones en segundo plano en un hilo
            loop = asyncio.get_event_loop()
            thread = threading.Thread(target=self.background_task, args=(message,))
            thread.start()
            await asyncio.sleep(0)  


    def background_task(self, message):
        # Acciones en segundo plano
        # ...
        
        # Enviar mensaje al grupo
        self.channel_layer.group_send(
            self.group_name,
            {
                'type': 'send_message',
                'message': message
            }
        )

    # Receive message from room group
    async def send_message(self, event):
        message = ""
        if 'message' in event:
            message = event['message']
        if 'content' in event:
            message = event["content"]

        # Send message to WebSocket
        await self.send(text_data=json.dumps({
            'message': message
        }))

    