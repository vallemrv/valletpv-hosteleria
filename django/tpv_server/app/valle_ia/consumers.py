from django.conf import settings
from channels.generic.websocket import AsyncWebsocketConsumer
from .tools.texto import preguntar_gpt
import json

class ChatConsumer(AsyncWebsocketConsumer):
    async def connect(self):
       self.user = self.scope['url_route']['kwargs']['user']
       self.group_name = settings.EMPRESA + "__gestion_ia___" + self.user
       
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
        message = text_data_json['message']
        # Send message to room group
        await self.channel_layer.group_send(
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