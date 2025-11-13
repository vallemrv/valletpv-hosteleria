from .expert_agent import ExpertAgent
from ..llm.llm_manager import create_llm, GROQ_OPENAI_GPT_OSS_120B
from ..prompts_system.prompt_pedidos import PROMPT_PEDIDOS
from langchain_core.tools import Tool
from . import AgentePedidosInput
from ..tools.comandas.pedidos_funcs import tools as all_tools

from ..utilidades.corrector_productos_llm import corregir_producto_con_llm
from ..utilidades.lista_teclas import get_nombres_teclas_unicos_async

class PedidosExpert(ExpertAgent):
    """
    Agente especializado en gestionar pedidos.
    Este agente utiliza herramientas específicas para interactuar con el sistema de pedidos.
    """

    def __init__(self):
        super().__init__(
            llm=create_llm(GROQ_OPENAI_GPT_OSS_120B, temperature=0),
            tools=all_tools,
            system_prompt=PROMPT_PEDIDOS,
            agent_name="PedidosAgent",
            max_iterations=5,
        )


    def asTool(self):
        """
        Devuelve las herramientas del agente como una lista.
        Esto permite que el agente sea utilizado en otros contextos donde se requieran herramientas.
        """
        return Tool(
            name=self.agent_name,
            func=None,
            coroutine=self.run_tool_async,
            args_schema=AgentePedidosInput,
            description="""
            Agente especializado en hacer pedidos.
            Necesita todo el contexto posible para realizar la acción.
            Incluido si hay alguna preferencia o configuración personalizada.
            
            Datos obligatorios:
            - Pedido: El pedido que se desea realizar.
            - Mesa: El nombre de la mesa asociada al pedido.

            args:
                query: str
            """
        )
        