from typing import List, Tuple, Any, Union
from langchain.schema import AgentAction, AgentFinish
from langchain.agents import BaseSingleActionAgent
from .base import get_tipo_consulta
from langchain.chat_models import ChatOpenAI
from langchain.schema import (
    HumanMessage,
    SystemMessage
)
import os


class SQLAgent(BaseSingleActionAgent):
    """Fake Custom Agent."""
    
    @property
    def input_keys(self):
        return ["input"]
    
    def plan(
        self, intermediate_steps: List[Tuple[AgentAction, str]], **kwargs: Any
    ) -> Union[AgentAction, AgentFinish]:
        """Given input, decided what to do.

        Args:
            intermediate_steps: Steps the LLM has taken to date,
                along with observations
            **kwargs: User inputs.

        Returns:
            Action specifying what tool to use.
        """

        
        if "Output" in kwargs["input"]:
            llm = ChatOpenAI(openai_api_key=os.environ.get("API_KEY"), temperature=0)
            messages =[
                SystemMessage(content="Eres una IA experta en traducir texto a SQL:"),
                HumanMessage(content=kwargs["input"])
            ]
            output = llm(messages=messages)
            print(output.content)
            
            tipo_consulta = get_tipo_consulta(output.content)
            if  tipo_consulta == 'select':
                return AgentAction(tool="query_sql", tool_input=output.content, log="Ejecutando sql")
            elif tipo_consulta in ['update', 'insert', 'delete']:
                return AgentAction(tool="exec_sql", tool_input=output.content, log="Ejecutando sql")
            else:
                return AgentFinish({"output":kwargs["input"]}, log="")
            
        else:
            return AgentFinish({"output":kwargs["input"]}, log="")

    async def aplan(
        self, intermediate_steps: List[Tuple[AgentAction, str]], **kwargs: Any
    ) -> Union[AgentAction, AgentFinish]:
        """Given input, decided what to do.

        Args:
            intermediate_steps: Steps the LLM has taken to date,
                along with observations
            **kwargs: User inputs.

        Returns:
            Action specifying what tool to use.
        """
        return AgentAction(tool="Search", tool_input=kwargs["input"], log="")