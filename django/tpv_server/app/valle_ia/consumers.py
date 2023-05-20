from django.conf import settings
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from .tools.openai import preguntar_gpt, create_men
from .tools.texto import identificar_tablas
from .tools.base import (ejecutar_select, get_tipo_consulta, 
                         dividir_consultas, ejecutar_accion)
from django.contrib.auth import authenticate
from asgiref.sync import async_to_sync
import json
import asyncio
import threading

class ChatConsumer(AsyncWebsocketConsumer):

    def enviar_mensaje_sync(self, mensaje, tipo="text"):
        # Enviar mensaje al grupo
        async_to_sync(self.channel_layer.group_send)(
            self.group_name,
            {
                'type': 'send_message',
                'message': mensaje,
                "tipo":tipo,
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
    async def send_message(self, event):
        # Send message to WebSocket
        await self.send(text_data=json.dumps({
            'type': "anwser",
             event["tipo"]: event['message'],
        }))


    def crear_tabla(self, datos):
        mensajes = [
            create_men("user", "Crea una tabla html (solo desde <table> hasta </table>) con los siguientes datos: "+ str(datos)),
            create_men("user", "Crea la tabla centrada, con el texto wrap y cada linea gris y blanco"),
            create_men("user", "Con la cabecera resaltada gris oscuro width 100%. gracias, sin explicacione solo codigo. overflow scroll")
            ]
        return  preguntar_gpt(mensajes)
    
    def background_task(self, message):
        modelos = identificar_tablas(message)
        if (len(modelos) <= 0):
            mensajes = [
                create_men("user",f"en la frase: {message} hay algun nombre de pila o de persona. responde solo con si o no.")
            ]
            respuesta = preguntar_gpt(mensajes)
            if "Sí." in respuesta:
                modelos = ["camareros"]
            else:    
                # Enviar mensaje al grupo
                self.enviar_mensaje_sync('''No hay ninguna tabla con este nombre. 
                                            Si me das mas informacion para la 
                                            proxima ya sabre como hacerlo.''')
                return
        

        mensajes = [
            create_men("system", "Eres un gran traductor del leguaje humano al leguaje SQL, sin explicaciones."),
            create_men("user", f"teniendo en cuenta esta estructua de base de datos {modelos}"),   
            create_men("user", f"traduce esto {message}, solo las consultas sql serparadas por puno y coma. 'NO EXPLICACIONES'. gracias")
        ]

        respuesta = preguntar_gpt(mensajes)
        
        
        consultas = dividir_consultas(respuesta)
        
        for c in consultas:
            print(c)
            tipo = get_tipo_consulta(c)
            if "select" in tipo:
                res = ejecutar_select(c)
                self.enviar_mensaje_sync(json.dumps(res), "tabla")
            elif tipo in ['update', 'insert']:
                res = ejecutar_accion(c)
                self.enviar_mensaje_sync(res, "text") 
            else:
                resultado = {"error" : f"Tipo de consulta no soportado: {tipo}"} 
                self.enviar_mensaje_sync(resultado, "text") 
        
        
    
    