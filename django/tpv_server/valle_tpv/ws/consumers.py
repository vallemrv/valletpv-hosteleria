# @Author: Manuel Rodriguez <valle>
# @Date:   10-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-17T00:43:08+01:00
# @License: Apache license vesion 2.0

from django.conf import settings
from channels.generic.websocket import AsyncWebsocketConsumer
import json


class ImpresionConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        self.print_name = self.scope['url_route']['kwargs']['print_name']
        
        self.print_group_name = settings.EMPRESA + "_impresion_" + self.print_name
        # Join room group
        await self.channel_layer.group_add(
            self.print_group_name,
            self.channel_name
        )

        await self.accept()

    async def disconnect(self, close_code):
        # Leave room group
        await self.channel_layer.group_discard(
            self.print_group_name,
            self.channel_name
        )

    # Receive message from WebSocket
    async def receive(self, text_data):
        text_data_json = json.loads(text_data)
        message = text_data_json['message']

        # Send message to room group
        await self.channel_layer.group_send(
            self.print_group_name,
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


class ComunicacionConsumer(AsyncWebsocketConsumer):

    async def connect(self):
        self.roon = self.scope['url_route']['kwargs']["receptor"]
        self.group_name = settings.EMPRESA + "_comunicaciones_" + self.roon
        # Join room group
        await self.channel_layer.group_add(
            self.group_name,
            self.channel_name
        )

        await self.accept()



    async def disconnect(self, close_code):
        # Leave room group
        await self.channel_layer.group_discard(
            self.group_name,
            self.channel_name
        )

    # Receive message from WebSocket
    async def receive(self, text_data):
        text_data_json = json.loads(text_data)
        message = text_data_json['content']

        # Send message to room group
        await self.channel_layer.group_send(
            self.group_name,
            {
                'type': 'send_message',
                'content': message
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
        await self.send(text_data=message)
