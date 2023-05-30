from typing import  Union, Any
from langchain.prompts import BaseChatPromptTemplate
from langchain.schema import HumanMessage, AgentAction, AgentFinish
from langchain.agents import AgentOutputParser
from .base import ejecutar_sql

ERROR_OUTPUT_MESSAGE = "Algo ha salido mal y he tendio que suspender el analisis. No dudes en probar otra vez. puede que los servidores de la IA este ocupados."

agent_prompt = """Eres un experto en convertir texto a SQL.
    
    {contexto}

    Pregunta:{input}
    
"""


# Set up a prompt template
class CustomPromptTemplate(BaseChatPromptTemplate):
    # The template to use
    template: str
    rt: Any = None
    
    def format_messages(self, **kwargs) -> str:
        docs = self.rt.get_relevant_documents(kwargs["input"])
        text_relevant = "Aqui tienes un poco de contexto:\n\n"
        for d in docs:
            text_relevant += d.page_content
        kwargs['contexto'] = text_relevant
        formatted = self.template.format(**kwargs)
        
        return [HumanMessage(content=formatted)]
    

def create_customprompt(rt: Any) -> CustomPromptTemplate: 
    return CustomPromptTemplate(
        template=agent_prompt,
        rt=rt,
        input_variables=["input", 'intermediate_steps']
    )


class CustomOutputParser(AgentOutputParser):
    
    def parse(self, llm_output: str) -> Union[AgentAction, AgentFinish]:
        rs = ejecutar_sql(llm_output.replace("Parametro:", ""))
        
        # If parsing fails, return an error message
        if not rs:
            return AgentFinish(
                return_values={"output": [ERROR_OUTPUT_MESSAGE]},
                log=llm_output,
            )
        
        # Return the action and action input
        return AgentFinish(return_values={'output':rs}, log=llm_output)

        
        
        
        







