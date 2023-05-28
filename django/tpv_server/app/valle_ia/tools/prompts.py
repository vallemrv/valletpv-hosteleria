from typing import List, Union
from langchain.prompts import BaseChatPromptTemplate
from langchain.tools import Tool
from langchain.schema import HumanMessage, AgentAction, AgentFinish
from langchain.agents import AgentOutputParser

import re


RESPUESTA_FINAL_PHRASE = "Respuesta Final:"
REGEX_PATTERN = r"Herramienta: (.*?)[\n]*Parametro:[\s]*(.*)"
ERROR_OUTPUT_MESSAGE = "Algo ha salido mal y he tendio que suspender el analisis. No dudes en probar otra vez. puede que los servidores de la IA este ocupados."
COMPILED_REGEX = re.compile(REGEX_PATTERN, re.DOTALL)



agent_promt = """Tu mision es responder lo mejor posible. Tienes acceso a las herramientas:
       {tools}

La estructura de la respuesta ha de ser:

Herramienta: Nombre de la herramienta. Tienes que elegir entre [{tool_names}]
Parametro: parametro que toma la herramienta.
 

Respuesta Final:  datos de la base de datos.


{agent_scratchpad}

Pregunta: {input}

"""


# Set up a prompt template
class CustomPromptTemplate(BaseChatPromptTemplate):
    # The template to use
    template: str
    # The list of tools available
    tools: List[Tool]
    
    def format_messages(self, **kwargs) -> str:
        # Get the intermediate steps (AgentAction, Observation tuples)
        
        # Format them in a particular way
        intermediate_steps = kwargs.pop("intermediate_steps")
        thoughts = ""
   
        for action, observation in intermediate_steps: 
            thoughts += action.log
            thoughts += f"\nObservacíon: {observation}\Pensamiento: "
            
        # Set the agent_scratchpad variable to that value
        kwargs["agent_scratchpad"] = thoughts
         # Create a tools variable from the list of tools provided
        kwargs["tools"] = "\n".join([f"{tool.name}: {tool.description}" for tool in self.tools])
        
        # Create a list of tool names for the tools provided
        kwargs["tool_names"] = ", ".join([tool.name for tool in self.tools])
        formatted = self.template.format(**kwargs)
        return [HumanMessage(content=formatted)]
    

def create_customprompt(tools: List[Tool]) -> CustomPromptTemplate: 
    return CustomPromptTemplate(
        template=agent_promt,
        tools=tools,
        # This omits the `agent_scratchpad`, `tools`, and `tool_names` variables because those are generated dynamically
        # This includes the `intermediate_steps` variable because that is needed
        input_variables=["input", "intermediate_steps"]
    )


class CustomOutputParser(AgentOutputParser):
    
   
    def parse(self, llm_output: str) -> Union[AgentAction, AgentFinish]:
        """
        This function parses the output from the 'llm' and returns either an AgentAction or an AgentFinish.
        """
        
        # Check if the agent should finish
        if RESPUESTA_FINAL_PHRASE in llm_output:
            return AgentFinish(
                return_values={"output": llm_output.split(RESPUESTA_FINAL_PHRASE)[-1].strip()},
                log=llm_output,
            )
        
        # Attempt to parse the action and action input
        match = COMPILED_REGEX.search(llm_output)
        
        # If parsing fails, return an error message
        if not match:
            return AgentFinish(
                return_values={"output": ERROR_OUTPUT_MESSAGE},
                log=llm_output,
            )
        
        # Extract action and action input from the match
        action = match.group(1).strip()
        action_input = match.group(2).strip(" ").strip('"')
        
        # Return the action and action input
        return AgentAction(tool=action, tool_input=action_input, log=llm_output)







