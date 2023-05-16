from django.conf import settings
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from .tools.openai import preguntar_gpt, create_men
from .tools.texto import estructura_base, find_models_in_phrase
from .tools.base import ejecutar_sql
from django.db import connection
from django.contrib.auth import authenticate
from asgiref.sync import async_to_sync
import json
import asyncio
import threading

class ChatConsumer(AsyncWebsocketConsumer):

    def enviar_mensaje_texto_sync(self, mensaje):
        # Enviar mensaje al grupo
        async_to_sync(self.channel_layer.group_send)(
            self.group_name,
            {
                'type': 'send_message_text',
                'message': mensaje
            }
        )


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
                    'type': 'send_message_text',
                    'message': "Error en la autentificacion. usuario no valido."
                }
            )
        else:
            message = text_data_json["message"]["query"]
           
            # Ejecutar acciones en segundo plano en un hilo
            loop = asyncio.get_running_loop()
            thread = threading.Thread(target=self.background_task, args=(message,))
            thread.start()
            await asyncio.sleep(0)
             
    # Receive message from room group
    async def send_message_text(self, event):
        message = event['message']
        
        # Send message to WebSocket
        await self.send(text_data=json.dumps({
            'type': "anwser",
            'text': message
        }))

    def background_task(self, message):
        modelos = find_models_in_phrase(message)
        if (len(modelos) <= 0):
            # Enviar mensaje al grupo
            self.enviar_mensaje_texto_sync('''No hay ninguna tabla con este nombre. 
                                      Si me das mas informacion para la proxima ya sabre como hacerlo.''')
            return
        
        structura = ""
        for m in modelos:
            structura += f" {m}={estructura_base[m]}"

        mesajes = [
            create_men("system", "Eres un gran traductor del leguaje humano al leguaje SQL, sin explicaciones."),
            create_men("user", f"teniendo en cuenta esta estructua de base de datos {structura}"),   
            create_men("user", f"traduce esto {message}, solo las consultas sql serparadas por comas. 'NO EXPLICACIONES'. gracias")
        ]

        respuesta = preguntar_gpt(mesajes)
        print(respuesta)
        #resultados = ejecutar_sql(respuesta)
        
        #print(resultados)
        
        # Enviar mensaje al grupo
        self.enviar_mensaje_texto_sync(respuesta)
    
    