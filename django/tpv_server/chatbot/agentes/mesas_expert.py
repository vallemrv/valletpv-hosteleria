from .expert_agent import ExpertAgent
from ..llm.llm_manager import create_llm, GROQ_OPENAI_GPT_OSS_120B
from ..prompts_system.prompt_mesas import PROMPT_MESAS
from langchain_core.tools import Tool
from ..tools.comandas.utilidades_mesas_funcs import (
    mover_lineas_entre_mesas,
    mover_mesa_completa,
)
from ..tools.comandas.pedidos_funcs import get_mesa_id, buscar_por_similitud_tpv
from . import AgentePedidosInput

class MesasExpert(ExpertAgent):
    """
    Agente especializado en gestionar mesas.
    """

    def __init__(self):
        super().__init__(
            llm=create_llm(GROQ_OPENAI_GPT_OSS_120B, temperature=0),
            tools=[
                mover_lineas_entre_mesas,
                mover_mesa_completa,
                get_mesa_id,
                buscar_por_similitud_tpv,
            ],
            system_prompt=PROMPT_MESAS,
            agent_name="MesasAgent",
            max_iterations=5,
        )

    def asTool(self):
        """
        Devuelve las herramientas del agente como una lista.
        """
        return Tool(
            name=self.agent_name,
            func=None,
            args_schema=AgentePedidosInput,
            coroutine=self.run_tool_async,
            description='''
            Agente especializado en gestionar mesas.
            puedes mover lineas entre mesas, mover mesas o juntar mesas o mover lineas.
            No tiene memoria ni contexto necesita de todo el contexto posible para que realize la accion.
            
            Este agente no PUEDE:
              - Mover algunas lineas parciales, solo puede mover lineas por nombre (obligatorio)
              - No puede mover linea 2, la mitad, un tercio (esto es nombre de articulo), etc.
        
              
            args:
                query: str
             '''
        )
