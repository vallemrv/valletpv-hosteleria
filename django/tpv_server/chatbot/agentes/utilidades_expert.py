from .expert_agent import ExpertAgent
from ..llm.llm_manager import create_llm, GROQ_OPENAI_GPT_OSS_120B
from ..prompts_system.prompt_utilidades import PROMPT_UTILIDADES
from langchain_core.tools import Tool
from ..tools.comandas.utilidades_funcs import tools as utilidades_tools
from . import AgentePedidosInput

class UtilidadesExpert(ExpertAgent):
    """
    Agente especializado en gestionar utilidades del sistema.
    """

    def __init__(self):
        super().__init__(
            llm=create_llm(GROQ_OPENAI_GPT_OSS_120B, temperature=0.2),
            tools=utilidades_tools,
            system_prompt=PROMPT_UTILIDADES,
            agent_name="UtilidadesAgent",
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
            Agente especializado en gestionar utilidades del sistema.
            Este agente puedes gestionar la memoria de preferencias del camarero,
            gestionar las existencias de teclas y agregar tags a las teclas esto mejora la búsqueda por sinonimos.
            No tiene memoria ni contexto, necesita de todo el contexto posible para que realice la acción.
            '''
        )
