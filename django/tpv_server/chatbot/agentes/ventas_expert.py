from .expert_agent import ExpertAgent
from ..llm.llm_manager import create_llm, GROQ_OPENAI_GPT_OSS_120B
from ..prompts_system.prompt_ventas import PROMPT_VENTAS
from langchain_core.tools import Tool
from ..tools.comandas.ventas_funcs import tools as tools_ventas_funcs
from . import AgentePedidosInput

class VentasExpert(ExpertAgent):
    """
    Agente especializado en obtener información de ventas.
    """

    def __init__(self):
        super().__init__(
            llm=create_llm(GROQ_OPENAI_GPT_OSS_120B, temperature=0),
            tools=tools_ventas_funcs,
            system_prompt=PROMPT_VENTAS,
            agent_name="VentasAgent",
            max_iterations=5,
        )

    def asTool(self):
        """
        Devuelve las herramientas del agente como una lista.
        """
        return Tool(
            name=self.agent_name,
            func=None,
            coroutine=self.run_tool_async,
            args_schema=AgentePedidosInput,
            description='''
            Agente especializado en obtener información de ventas.
            Puedes consular los pedidos o la cuenta de nuna mesa.
            No tiene memoria ni contexto, necesita de todo el contexto posible para que realice la acción
            '''
        )
