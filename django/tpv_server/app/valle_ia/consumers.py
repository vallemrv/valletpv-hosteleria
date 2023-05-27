from django.conf import settings
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth import authenticate
from asgiref.sync import async_to_sync
from .tools.class_tools import ExecSQLTools, QuerySQLTools
from .tools.embedings import crear_emedings
from langchain.agents import initialize_agent
from langchain.agents.types import AgentType
from langchain.chains import LLMChain
from langchain.tools import Tool
from langchain.prompts import PromptTemplate
from langchain.chat_models import ChatOpenAI
import os
import json
import asyncio
import threading


class ChatConsumer(AsyncWebsocketConsumer):

    def __init__(self, *args, **kwargs):
        
        
        path = os.path.join(settings.BASE_DIR, "app", "valle_ia", "LLM_embedings", "ejemplos_sql.json")
        self.prompt = crear_emedings(file_db=path, num_ejemplos=4)
        
        llm = ChatOpenAI( openai_api_key=os.environ.get("API_KEY"), temperature=0)

        llm_chain = LLMChain(llm=llm, prompt=PromptTemplate(
                                input_variables=["query"],
                                template="{query}"
                            ))

        llm_tool = Tool(
            name="Modelo del lenguaje",
            func=llm_chain.run,
            description='use this tool for general purpose queries an logic'
        )

        tools = [ExecSQLTools(ws_callback=self.enviar_mensaje_sync), QuerySQLTools(ws_callback=self.enviar_mensaje_sync), llm_tool]
        self.agent = initialize_agent(tools, llm, agent=AgentType.ZERO_SHOT_REACT_DESCRIPTION, max_iterations=3, verbose=True)


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
        self.agent.run(self.prompt.format(message))   
    