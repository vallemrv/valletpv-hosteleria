# --- Importaciones Clave ---
import asyncio
import uuid
from gestion.tools.config_logs import log_debug_chatbot as logger
from ..agentes import MainAgent
from ..llm.llm_manager import (create_llm, GROK_4_FAST_NON_REASONING, 
                                           GEMINI_2_5_FLASH,
                                           GROQ_OPENAI_GPT_OSS_120B)
from ..tools import all_tools
from ..transcriptores.transcriptor_gemini import TranscriptorGeminiLangChain
from ..prompts_system.prompt_main import PROMPT_MAIN_BACK
from ..utilidades.ws_sender import (
    CallbackHandler, 
    tool_message_sender, 
    current_message_id_var
)


class MessageProcessor:
    """
    Procesa un mensaje de texto genérico utilizando un único agente de IA.
    """
    def __init__(self, consumer):
        self.consumer = consumer
        self.lock = asyncio.Lock()
        
        
        
        # --- Agente Único y Genérico ---
        # Toda la lógica de roles ha sido eliminada.
        
        self.main_agent = MainAgent(
            llm=create_llm(GROK_4_FAST_NON_REASONING, temperature=0.3),
            tools= all_tools,
            system_prompt=PROMPT_MAIN_BACK,
            agent_name="ChatbotAgent",
            transcriptor=TranscriptorGeminiLangChain(
                llm=create_llm(GEMINI_2_5_FLASH, temperature=0.1)
            ),
            callbacks= [CallbackHandler()],
            max_iterations=5,
        )
        
        

    async def generate_response(self, message, message_id=None):
        """
        Punto de entrada: recibe un mensaje, le asigna un ID y lo procesa.
        """
        message_id = message_id or str(uuid.uuid4())
        
        # Inicia la tarea de procesamiento en segundo plano
        asyncio.create_task(self.process_agent_response(message, message_id))

    async def process_agent_response(self, agent_input, message_id):
        """
        Ejecuta el agente con el mensaje y envía la respuesta final.
        """
        try:
            await self.send_message_to_client(message_id, "Procesando solicitud", "status")
            
            async with self.lock:
                response_data = await self._run_invoke_with_context(agent_input, message_id)

            await self.send_message_to_client(message_id, response_data.strip().replace("'''html", "").replace("'''", ""), "bot")

        except Exception as e:
            logger.error(f"Error al procesar el mensaje ID {message_id}: {e}", exc_info=True)
            await self.send_message_to_client(message_id, "¡Ups! Algo salió mal.", "bot")

    async def _run_invoke_with_context(self, agent_input, message_id):
        """Helper que establece el contexto para la llamada al agente."""
        token_id = current_message_id_var.set(message_id)
        token_sender = tool_message_sender.set(self.send_message_to_client)
        try:
            return await self.main_agent.run_tool_async(agent_input)
        finally:
            current_message_id_var.reset(token_id)
            tool_message_sender.reset(token_sender)

    async def send_message_to_client(self, message_id, message, sender):
        """Envía un mensaje al cliente a través del WebSocket."""
        if not isinstance(message, str) or not message.strip():
            message = "No he podido procesar tu solicitud. Inténtalo de nuevo."
            sender = "bot"
            logger.warning(f"Intento de enviar mensaje vacío o no-string. ID: {message_id}")

        if not self.consumer.group_name:
            logger.error(f"consumer.group_name no definido. No se puede enviar mensaje ID: {message_id}")
            return

        try:
            await self.consumer.channel_layer.group_send(
                self.consumer.group_name,
                {
                    "type": "send_message",
                    "message_id": message_id,
                    "message": message,
                    "sender": sender,
                },
            )
        except Exception as e:
            logger.error(f"Error en channel_layer.group_send para ID {message_id}: {e}", exc_info=True)