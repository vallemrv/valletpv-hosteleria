# -*- coding: utf-8 -#
import os
from django.conf import settings
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from langgraph.prebuilt import create_react_agent
from gestion.tools.config_logs import log_debug_chatbot as logger
from ..interfaces.transcriptor_audio import TranscriptorDeAudio


class MainAgent:
    def __init__(self, llm, tools, system_prompt, agent_name,
                 transcriptor: TranscriptorDeAudio = None,
                 callbacks=None,
                 max_iterations=10,
                 memoria_dinamica: str = None,
                 has_historical_memory: bool = True):

        self.tools = tools
        if not self.tools:
            raise ValueError(f"No se proporcionaron herramientas al agente {agent_name}.")

        self.llm = llm
        self.agent_name = agent_name
        self.transcriptor = transcriptor
        self.memoria_dinamica = memoria_dinamica
        self.has_historical_memory = has_historical_memory
        
        # En LangGraph la memoria se maneja con la lista de mensajes
        # No necesitamos ConversationBufferWindowMemory
        self.message_history = []  # Lista simple de mensajes para memoria
        self.max_history = 8  # Máximo de intercambios a recordar

        # Construcción del prompt del sistema para LangGraph
        self.system_prompt = system_prompt
        if self.memoria_dinamica:
            self.system_prompt += "\n\n--- INSTRUCCIONES EXTRA ---\n{preferencias_dinamicas}"

        # LangGraph usa create_react_agent que es más simple
        # El prompt del sistema se pasa como SystemMessage en los mensajes, no en create_react_agent
        self.agent_executor = create_react_agent(
            model=self.llm,
            tools=self.tools
        )
        
        self.max_iterations = max_iterations
        self.callbacks = callbacks if callbacks else []

    async def run_tool_async(self, query: HumanMessage) -> str:
        try:
            text_content = await self._extract_text_from_query(query)
            if not text_content:
                return "No se pudo extraer contenido válido del mensaje."

            # LangGraph trabaja con mensajes, no con diccionarios
            # Siempre empezamos con el system message
            messages = [SystemMessage(content=self.system_prompt)]
            
            # Si hay memoria activa, añadir el historial (últimos max_history mensajes)
            if self.has_historical_memory and self.message_history:
                messages.extend(self.message_history[-self.max_history*2:])  # *2 porque son pares user/assistant
            
            # Añadir el mensaje actual del usuario
            user_message = HumanMessage(content=text_content)
            messages.append(user_message)
            
            # Invocar el agente con el estado de mensajes
            response = await self.agent_executor.ainvoke(
                {"messages": messages},
                config={"callbacks": self.callbacks} if self.callbacks else {}
            )
            
            # Extraer la respuesta del último mensaje
            if "messages" in response and len(response["messages"]) > 0:
                last_message = response["messages"][-1]
                output = last_message.content if hasattr(last_message, 'content') else str(last_message)
                
                # Guardar en memoria simple
                if self.has_historical_memory:
                    self.message_history.append(user_message)
                    self.message_history.append(AIMessage(content=output))
            else:
                output = "El agente no devolvió una respuesta clara."

            return output

        except Exception as e:
            error_msg = f"Hubo un error al contactar con el agente {self.agent_name}: {str(e)}"
            logger.error(error_msg, exc_info=True)
            return error_msg

    async def _extract_text_from_query(self, query: HumanMessage) -> str:
        if isinstance(query.content, str):
            return query.content

        if isinstance(query.content, list) and len(query.content) > 0:
            text_parts = []
            audio_part = None
            image_part = None

            for part in query.content:
                if isinstance(part, dict):
                    mime_type = part.get("mime_type", "")
                    if mime_type.startswith("audio/"):
                        audio_part = part
                    elif mime_type.startswith("image/"):
                        image_part = part
                    elif part.get("type") == "message":
                        text_parts.append(part.get("text", ""))

            if audio_part:
                audio_bytes = audio_part.get("data")
                mime_type = audio_part.get("mime_type")
                if self.transcriptor and audio_bytes:
                    logger.info(f"[{self.agent_name}] Audio detectado. Transcribiendo...")
                    return await self.transcriptor.transcribir(audio_bytes, mime_type)
                else:
                    logger.warning(f"[{self.agent_name}] Se encontró audio pero no hay transcriptor.")
                    return "Error: No se pudo procesar el audio."

            final_text = " ".join(text_parts)
            if image_part:
                logger.info(f"[{self.agent_name}] Imagen detectada. Obteniendo descripción...")
                image_message = HumanMessage(content=[image_part])
                response = await self.llm.ainvoke([image_message])
                image_description = response.content.strip()
                final_text += f"\n[Descripción de la imagen: {image_description}]"

            return final_text.strip() if final_text else None

        logger.warning(f"Formato de contenido no soportado: {query.content}")
        return None

    def _load_dinamic_instructions(self, file_name: str) -> str:
        try:
            memoria_dir = os.path.join(settings.MEDIA_ROOT, "memoria")
            archivo_memoria = os.path.join(memoria_dir, file_name)

            if os.path.exists(archivo_memoria):
                with open(archivo_memoria, 'r', encoding='utf-8') as f:
                    content = f.read().strip()
                    return content if content else "Instrucciones vacías."
            return "No existen instrucciones para este agente."
        except Exception as e:
            logger.error(f"Error cargando instrucciones del agente: {e}", exc_info=True)
            return f"Error cargando instrucciones: {e}"
