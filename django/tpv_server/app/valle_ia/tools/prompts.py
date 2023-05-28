from typing import List, Union
from langchain.prompts import BaseChatPromptTemplate
from langchain.tools import Tool
from langchain.schema import HumanMessage, AgentAction, AgentFinish
from langchain.agents import AgentOutputParser

import re



agent_promt = """Contesta a las siguientes preguntas lo mejor que puedas, habla como un analista de base de datos. Tienes aceso a las siguientes herramientas:

{tools}

Utiliza el siguiente formato:

Pregunta: la pregunta de entrada que debes responder
Pensamiento: siempre deberías pensar en qué hacer
Acción: la acción a tomar, debería ser una de [{tool_names}]
Parametro: la entrada para la acción
Observación: el resultado de la acción
... (este Pensamiento/Acción/Entrada de Acción/Observación puede repetirse N veces)
Pensamiento: ahora conozco la respuesta final
Respuesta Final: la respuesta final a la pregunta de entrada original

Adelante! Recuerda hablar como un hostelero, utiliza expresiones faciles en la respuesta final.

Pregunta: {input}
{agent_scratchpad}"""


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
            thoughts += f"\nObservación: {observation}\nPensamiento: "
            
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
        # Check if agent should finish
        if "Respuesta Final:" in llm_output:
            return AgentFinish(
                # Return values is generally always a dictionary with a single `output` key
                # It is not recommended to try anything else at the moment :)
                return_values={"output": llm_output.split("Respuesta Final:")[-1].strip()},
                log=llm_output,
            )
        
        # Parse out the action and action input
        regex = r"Acción: (.*?)[\n]*Parametro:[\s]*(.*)"
        match = re.search(regex, llm_output, re.DOTALL)
        
        # If it can't parse the output it raises an error
        # You can add your own logic here to handle errors in a different way i.e. pass to a human, give a canned response
        if not match:
            raise ValueError(f"No se ha podido pasar la salida de la IA: `{llm_output}`")
        action = match.group(1).strip()
        action_input = match.group(2)
        
        # Return the action and action input
        return AgentAction(tool=action, tool_input=action_input.strip(" ").strip('"'), log=llm_output)
    
