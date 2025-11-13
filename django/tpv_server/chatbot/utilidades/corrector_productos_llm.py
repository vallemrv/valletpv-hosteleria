from ..llm.llm_manager import create_llm, GROQ_OPENAI_GPT_OSS_120B
from typing import List

async def corregir_producto_con_llm(query: str, teclas: List[str]) -> str:
    """
    Utiliza un LLM para corregir la consulta de un usuario y seleccionar el producto más adecuado de una lista de teclas.

    Args:
        query (str): La consulta del usuario.
        teclas (List[str]): La lista de nombres de teclas disponibles.

    Returns:
        str: El nombre de la tecla más adecuada según el LLM.
    """
    prompt = f"""
    Dada la siguiente consulta de un usuario y una lista de productos disponibles, discrimina que es producto y que no y selecciona el producto más adecuado de la lista. 
    Responde con la consulta bien escrita y corregida.

    ejemplo:
        input -> Mesa 1, Balantec con Coca-Cola.
        output -> Mesa 1, ballantines con cola.


    Consulta: "{query}"
    Productos disponibles: {', '.join(teclas)}

    Producto seleccionado:
    """
    llm_groq = create_llm(GROQ_OPENAI_GPT_OSS_120B, temperature=0.2, max_tokens=500, timeout=15)

    response = llm_groq.invoke(prompt)
    return response.content.strip()
