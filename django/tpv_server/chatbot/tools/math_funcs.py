from langchain_core.tools import tool
from chatbot.utilidades.ws_sender import send_tool_message  # Añadir este import

@tool
def sumar(a: float, b: float) -> float:
    """Devuelve la suma de dos números."""
    send_tool_message(f"Sumando {a} + {b}")
    return a + b

@tool
def restar(a: float, b: float) -> float:
    """Devuelve la resta de dos números."""
    send_tool_message(f"Restando {a} - {b}")
    return a - b

@tool
def multiplicar(a: float, b: float) -> float:
    """Devuelve la multiplicación de dos números."""
    send_tool_message(f"Multiplicando {a} * {b}")
    return a * b

tools = [sumar, restar, multiplicar]
