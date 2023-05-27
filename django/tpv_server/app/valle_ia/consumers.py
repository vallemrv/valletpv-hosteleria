from django.conf import settings
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth import authenticate
from asgiref.sync import async_to_sync
from .tools.class_tools import ExecSQLTools, QuerySQLTools
from .tools.embedings import crear_emedings
from .tools.agents import SQLAgent
from langchain.agents import AgentExecutor
from langchain.prompts import PromptTemplate
from langchain.chat_models import ChatOpenAI
from langchain.chains import LLMChain
from langchain.tools import Tool
from langchain.schema import (
    HumanMessage,
    SystemMessage
)
import os
import json
import asyncio
import threading


class ChatConsumer(AsyncWebsocketConsumer):

    def __init__(self, *args, **kwargs):
        
        path = os.path.join(settings.BASE_DIR, "app", "valle_ia", "LLM_embedings", "ejemplos_sql.json")
        self.prompt = crear_emedings(file_db=path, num_ejemplos=4)
        
        
        tools = [ExecSQLTools().set_ws_callback(self), 
                 QuerySQLTools().set_ws_callback(self)]
        
        agent = SQLAgent()

        self.agent = AgentExecutor.from_agent_and_tools(agent=agent, tools=tools, verbose=True)

        super().__init__(*args, **kwargs)

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

    
    def background_task(self, message):
        '''
        llm = ChatOpenAI(openai_api_key=os.environ.get("API_KEY"), temperature=0)
        p = self.prompt.format(text=message)
        print(p)
        messages = [
            SystemMessage(content="Eres especialista en traducir texto a consultas SQL"),
            HumanMessage(content=p)
        ]
        output = llm(messages=messages)
        print(output.content)
        '''
        self.agent.run(self.prompt.format(text=message))   
    