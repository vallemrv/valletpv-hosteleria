# Primero, asegúrate de tener instalada la librería thefuzz y python-Levenshtein:
# pip install thefuzz
# pip install python-Levenshtein

from thefuzz import process, fuzz
from typing import List, Tuple

def busqueda_difusa(query: str, choices: List[str], threshold: int = 80) -> List[Tuple[str, int]]:
    """
    Realiza una búsqueda difusa para encontrar las mejores coincidencias para una consulta
    dentro de una lista de opciones.

    Esta función es útil para corregir errores tipográficos o encontrar coincidencias
    que no son exactas. Utiliza la librería thefuzz para calcular la similitud
    entre cadenas de texto.

    Args:
        query (str): La cadena de texto que se quiere buscar.
        choices (List[str]): La lista de cadenas de texto donde buscar.
        threshold (int, optional): El umbral de similitud mínimo (de 0 a 100) para
                                   considerar una coincidencia. Por defecto es 80.

    Returns:
        List[Tuple[str, int]]: Una lista de tuplas, donde cada tupla contiene
                               la cadena coincidente y su puntuación de similitud.
                               La lista está ordenada de la mejor a la peor coincidencia.
                               Solo se devuelven las coincidencias que superan el umbral.
    """
    if not query or not choices:
        return []

    # process.extract devuelve una lista de tuplas (cadena, puntuacion)
    # ya ordenadas por puntuación.
    resultados = process.extract(query, choices, limit=5, scorer=fuzz.WRatio)

    # Filtrar los resultados que no superan el umbral
    resultados_filtrados = [
        (match, score) for match, score in resultados if score >= threshold
    ]

    return resultados_filtrados

   