import asyncio
import uuid
from gestion.tools.config_logs import log_debug_chatbot as logger
from ..agentes.main_agent import MainAgent
from ..llm.llm_manager import create_llm, GROQ_OPENAI_GPT_OSS_120B, GEMINI_2_5_FLASH
from ..agentes.pedidos_expert import PedidosExpert
from ..agentes.mesas_expert import MesasExpert
from ..agentes.utilidades_expert import UtilidadesExpert
from ..agentes.ventas_expert import VentasExpert  # Placeholder si no se usa VentasExpert
from ..prompts_system.pormpt_router_comandos import PROMPT_ROUTER_COMANDOS
from ..transcriptores.transcriptor_gemini import TranscriptorGeminiLangChain


from ..utilidades.ws_sender import (
    CallbackHandler, 
    tool_message_sender, 
    current_message_id_var,
    current_camarero_id_var
)


class PedidosProcessor:
    def __init__(self, consumer, camarero_id=None, uid_device=None):
        self.consumer = consumer
        self.lock = asyncio.Lock()
        self.camarero_id = camarero_id
        self.uid_device = uid_device

        current_camarero_id_var.set(camarero_id)
        pedidos_expert = PedidosExpert()  # Inicializa el agente de pedidos
        mesas_expert = MesasExpert()  # Alias para mantener consistencia con el nombre original
        utilidades_expert = UtilidadesExpert()
        ventas_expert = VentasExpert()  # Placeholder si no se usa VentasExpert
        # Agente de Pedidos
        self.main_agent = MainAgent(
            llm=create_llm(GROQ_OPENAI_GPT_OSS_120B, temperature=0),
            tools=[pedidos_expert.asTool(), 
                   mesas_expert.asTool(), 
                   utilidades_expert.asTool(),
                   ventas_expert.asTool()],
            system_prompt=PROMPT_ROUTER_COMANDOS,
            agent_name="PedidosRouter",
            callbacks=[CallbackHandler()],
            transcriptor=TranscriptorGeminiLangChain(
                llm=create_llm(GEMINI_2_5_FLASH, temperature=0.1)
            ),
            max_iterations=5,
            memoria_dinamica=f"camarero_{camarero_id}.dat"
         )

    async def process_pedido_response(self, message_content, message_id):
        """
        Procesa el mensaje usando el agente de pedidos.
        """
        try:
            await self.send_message_to_client(message_id, "🍽️ Procesando pedido...", "status")
            
            async with self.lock:
                # Envolver la llamada síncrona para establecer contexto
                async def run_invoke_async_with_context(agent_input):
                    token = current_message_id_var.set(message_id)
                    token_sender = tool_message_sender.set(self.send_message_to_client)
                   
                    try:
                        response = await self.main_agent.run_tool_async(agent_input)
                        return response
                    finally:
                        current_message_id_var.reset(token)  # Resetea la ContextVar
                        tool_message_sender.reset(token_sender)  # Resetea el sender
                      
                # Llama a la función síncrona envuelta usando el agente configurado
                response_data = await run_invoke_async_with_context(message_content)
                
            # Enviar respuesta final al cliente
            await self.send_message_to_client(message_id, response_data.strip(), "bot")
            
        except Exception as e:
            logger.error(f"Error GRANDE al procesar el pedido ID {message_id}: {str(e)}", exc_info=True)
            await self.send_message_to_client(message_id, "¡Ups! Algo salió mal al procesar tu pedido. Inténtalo de nuevo.", "status")

    async def generate_response(self, message, message_id=None):
        """
        Inicia el procesamiento del mensaje del usuario para pedidos.
        """
        if message_id is None:
            message_id = str(uuid.uuid4())  # Genera ID si no viene uno
     
        # Mensaje inicial
        await self.send_message_to_client(message_id, "🔍 Analizando pedido...", "status")
            
        asyncio.create_task(self.process_pedido_response(message, message_id))

    async def send_message_to_client(self, message_id, message, sender):
        """
        Función helper para enviar mensajes al cliente via group_send.
        """
        if not message or not isinstance(message, str):
            message = "No he podido procesar tu pedido. Inténtalo de nuevo. Disculpa las molestias."
            sender = "bot"
            logger.warning(f"Intento de enviar mensaje vacío o no string a cliente. ID: {message_id}, Sender: {sender}")
            

        if not self.consumer.group_name:
            logger.error(f"Intento de enviar mensaje pero consumer.group_name no está definido! ID: {message_id}")
            return

        try:
                
            await self.consumer.channel_layer.group_send(
                self.consumer.group_name,
                {
                    "type": "send_message",  # Este es el nombre del handler en tu Consumer
                    "message_id": message_id,
                    "message": message,
                    "sender": sender
                }
            )
        except Exception as e:
            logger.error(f"Error en channel_layer.group_send para ID {message_id}: {e}", exc_info=True)



    
    