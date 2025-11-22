# -*- coding: utf-8 -*-
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.prompts import MessagesPlaceholder
from gestion.tools.config_logs import log_debug_chatbot as logger


class ExpertAgent:
    def __init__(self, llm, tools, system_prompt, agent_name, 
                 callbacks=None, 
                 max_iterations=10):
        """
        Inicializa un ExpertAgent para procesar texto y utilizar herramientas.
        

        Args:
            llm: Instancia de un modelo de lenguaje (ej. ChatGoogleGenerativeAI).
            tools: Lista de herramientas para el agente.
            system_prompt: Prompt del sistema para guiar el comportamiento del agente.
            agent_name: Nombre del agente (por ejemplo, 'TextAgent').
            callbacks: Lista de callbacks para logging y comunicación.
            max_iterations: Máximo número de iteraciones para el AgentExecutor.
        """
        self.tools = tools
        if not self.tools:
            logger.error(f"No se encontraron herramientas para {agent_name} en el registry.")
            raise ValueError(f"No se proporcionaron herramientas al agente {agent_name}.")
        
        self.llm = llm
        self.agent_name = agent_name

        # Definir el prompt sin historial de chat
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            ("user", "{input}"),
            MessagesPlaceholder(variable_name="agent_scratchpad"),
        ])

        # Versión simplificada sin AgentExecutor para compatibilidad con LangChain 1.0+
        self.llm = llm
        self.tools = tools
        self.prompt = prompt
        self.max_iterations = max_iterations
        self.callbacks = callbacks if callbacks else []
    
    async def run_tool_async(self, query: str) -> str:
        """
        Procesa una consulta de texto.

        Args:
            query (str): Mensaje de texto.

        Returns:
            str: Respuesta del modelo o mensaje de error.
        """
        try:
            if not isinstance(query, str):
                raise ValueError("La entrada para TextAgent debe ser una cadena de texto.")

            # Versión simplificada: usar el LLM directamente
            try:
                response = await self.llm.ainvoke(self.prompt.format(input=query, agent_scratchpad=""))
                output = response.content if hasattr(response, 'content') else str(response)
            except Exception as llm_error:
                logger.error(f"Error en LLM: {llm_error}")
                output = "Lo siento, no puedo procesar la solicitud en este momento."
            return output
            
        except Exception as e:
            error_msg = f"Hubo un error al contactar con el agente {self.agent_name}: {str(e)}"
            logger.error(error_msg, exc_info=True)
            return error_msg
        
