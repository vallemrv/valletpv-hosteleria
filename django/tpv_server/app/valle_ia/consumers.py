from django.conf import settings
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth import authenticate
from asgiref.sync import async_to_sync
from .tools.class_tools import ExecSQLTools, SearchInfoDBTools
from .tools.embedings import crear_emeddings_ex
from .tools.prompts import create_customprompt, CustomOutputParser
from langchain.chat_models import ChatOpenAI
from langchain.chains import LLMChain
from langchain.agents import LLMSingleActionAgent, AgentExecutor
from langchain.tools import Tool


import os
import json
import asyncio
import threading


class ChatConsumer(AsyncWebsocketConsumer):

    def __init__(self, *args, **kwargs):
        
        path = os.path.join(settings.BASE_DIR, "app", "valle_ia", "LLM_embedings", "ejemplos_sql.json")
        rt = crear_emeddings_ex(file_db=path)
        
        # Initiate our LLM - default is 'gpt-3.5-turbo'
        llm = ChatOpenAI(temperature=0)
        
        sql_exc = ExecSQLTools()
        search_info = SearchInfoDBTools().set_embbeding(rt)
                  
        tools = [
            Tool(
                name= sql_exc.name,
                func= sql_exc.run,
                description=sql_exc.description
            ),
            Tool(
                 name=search_info.name,
                 func=search_info.run,
                 description=search_info.description
            )
        ]
        

        # LLM chain consisting of the LLM and a prompt
        llm_chain = LLMChain(llm=llm, prompt=create_customprompt(tools))

        # Using tools, the LLM chain and output_parser to make an agent
        tool_names = [tool.name for tool in tools]
        
        single_action = LLMSingleActionAgent(
            llm_chain=llm_chain, 
            output_parser=CustomOutputParser(),
            # We use "Observation" as our stop sequence so it will stop when it receives Tool output
            # If you change your prompt template you'll need to adjust this as well
            stop=["\nObservación:"], 
            allowed_tools=tool_names,
            max_iterations=3
        )
        
        # Initiate the agent that will respond to our queries
        # Set verbose=True to share the CoT reasoning the LLM goes through
        self.agent = AgentExecutor.from_agent_and_tools(agent=single_action, tools=tools, verbose=True)

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
        self.enviar_mensaje_sync(self.agent.run(message))
    