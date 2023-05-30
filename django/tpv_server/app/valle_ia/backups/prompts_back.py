from typing import  Union, List
from langchain.prompts import BaseChatPromptTemplate
from langchain.schema import HumanMessage, AgentAction, AgentFinish
from langchain.agents import AgentOutputParser
from langchain.tools import Tool
import re


ERROR_OUTPUT_MESSAGE = "Algo ha salido mal y he tendio que suspender el analisis. No dudes en probar otra vez. puede que los servidores de la IA este ocupados."

# Set up the prompt with input variables for tools, user input and a scratchpad for the model to record its workings
agent_prompt = """Contesta la siguientes cuestiones lo mejor que puedas. Actua como traductor de texto -> SQL -> texto. Tienes acceso a las siguentes herramientas:

{tools}

Usa el siguiente formato:

Pregunta: la pregunta de entrada que debes contestar.
Reflexión: siempre tienes que pensar sobre lo que hacer.
Herramienta: la herramienta a escoger, tiene que ser una de estas [{tool_names}]
Parametro: el parametro de entrada de la herrameinta
Salida: la salida de la herramienta
... (Estas instrucciones Reflexión/Herramienta/Parametro/Salida puede repetirse N veces)
Reflexión: Ahora se la respuesta final.
Respuesta final: la respuesta final de la pregunta original.

Nota: Recuerda! Explicate como si yo fuera un niño de 10 años.


Pregunta: {input}

{agent_scratchpad}"""


# Set up a prompt template
class CustomPromptTemplate(BaseChatPromptTemplate):
    # The template to use
    template: str
    tools: List[Tool]
    
    def format_messages(self, **kwargs) -> str:
        intermediate_steps = kwargs.pop("intermediate_steps")
        thoughts = ""
        for action, observation in intermediate_steps:
            thoughts += action.log
            thoughts += f"\nSalida: {observation}\nReflexion: "

        # Set the agent_scratchpad variable to that value
        kwargs["agent_scratchpad"] = thoughts

        # Create a tools variable from the list of tools provided
        kwargs["tools"] = "\n".join([f"{tool.name}: {tool.description}" for tool in self.tools])

        kwargs["tool_names"] = ", ".join([tool.name for tool in self.tools])
        formatted = self.template.format(**kwargs)
        
        return [HumanMessage(content=formatted)]
    

class CustomOutputParser(AgentOutputParser):

    iterations: int = 0
    max_iterations: int = 4
    
    def parse(self, llm_output: str) -> Union[AgentAction, AgentFinish]:

        if "Respuesta final:" in llm_output:
            self.iterations = 0
            return AgentFinish(
                # Return values is generally always a dictionary with a single `output` key
                # It is not recommended to try anything else at the moment :)
                return_values={"output": llm_output.split("Final Answer:")[-1].strip()},
                log=llm_output,
            )
        
        # Parse out the action and action input
        regex = r"Herramienta: (.*?)[\n]*Parametro:[\s]*(.*)"
        match = re.search(regex, llm_output, re.DOTALL)
        self.iterations += 1

        # If it can't parse the output it raises an error
        # You can add your own logic here to handle errors in a different way i.e. pass to a human, give a canned response
        if not match or self.iterations >= self.max_iterations:
            self.iterations = 0
            return AgentFinish(
                # Return values is generally always a dictionary with a single `output` key
                # It is not recommended to try anything else at the moment :)
                return_values={"output":"He llegado hasta aqui: \n" + llm_output},
                log=llm_output,
            )
        

        action = match.group(1).strip()
        action_input = match.group(2)
        
        # Return the action and action input
        return AgentAction(tool=action, tool_input=action_input.strip(" ").strip('"'), log=llm_output)
    
        
def create_customprompt(tools: List[Tool]) -> CustomPromptTemplate: 
    return CustomPromptTemplate(
        template=agent_prompt,
        tools=tools,
        input_variables=["input", 'intermediate_steps']
    )       
        
        







