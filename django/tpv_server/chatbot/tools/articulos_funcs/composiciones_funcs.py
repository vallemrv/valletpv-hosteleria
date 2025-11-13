from langchain_core.tools import tool
from gestion.models.composiciones import ComposicionTeclas, LineasCompuestas
from gestion.models.teclados import Teclas
from django.core.exceptions import ObjectDoesNotExist
from chatbot.utilidades.ws_sender import send_tool_message
from chatbot.decorators.db_connection_manager import db_connection_handler


@tool
@db_connection_handler
def crear_composicion_tecla(id_tecla: int, composicion_familias: str, cantidad: int) -> dict:
    """
    Crea una nueva regla de composición para una tecla (ej: para un menú o combo).

    Args:
        id_tecla (int): ID de la tecla principal del combo (ej: "Menú Burger").
        composicion_familias (str): Una cadena de texto con formato de lista JSON que contiene 
                                     los nombres de las familias de los componentes. 
                                     Ejemplo: '["Bebidas", "Guarniciones"]'
        cantidad (int): Número total de componentes que forman el combo.

    Returns:
        dict: Diccionario con los datos de la composición creada o un mensaje de error.
    """
    try:
        send_tool_message("Creando un combo o menú...")
        tecla = Teclas.objects.get(id=id_tecla)
        # Aquí podrías añadir una validación para asegurar que composicion_familias es un JSON válido
        comp = ComposicionTeclas.objects.create(
            tecla=tecla,
            composicion=composicion_familias,
            cantidad=cantidad
        )
        return {"success": True, "composicion": comp.serialize()}
    except ObjectDoesNotExist:
        return {"success": False, "error": "Tecla no encontrada"}
    except Exception as e:
        return {"success": False, "error": str(e)}

@tool
@db_connection_handler
def listar_composiciones() -> list:
    """
    Devuelve una lista con todas las reglas de composición existentes (menús, combos, etc.).

    Esta función devuelve las REGLAS, no las teclas en sí. Cada regla define qué familias
    de productos componen un menú y está asociada a una tecla principal.

    Returns:
        list: Lista de diccionarios, donde cada uno es una composición serializada.
    """
    try:
        send_tool_message("Listando todas las composiciones...")
        composiciones = ComposicionTeclas.objects.all()
        return [c.serialize() for c in composiciones]
    except Exception as e:
        return {"success": False, "error": str(e)}

@tool
@db_connection_handler
def modificar_composicion(id_composicion: int, cantidad: int = None, id_tecla: int = None, composicion_familias: str = None) -> dict:
    """
    Modifica una regla de composición existente.

    Permite cambiar la cantidad de componentes, la tecla principal del combo, 
    o la lista de familias que lo componen.

    Args:
        id_composicion (int): ID de la composición a modificar.
        cantidad (int, optional): La nueva cantidad de componentes.
        id_tecla (int, optional): El ID de la nueva tecla principal para esta regla.
        composicion_familias (str, optional): La nueva lista de familias en formato JSON string. 
                                               Ejemplo: '["Bebidas", "Postres"]'

    Returns:
        dict: La composición modificada o un mensaje de error.
    """
    try:
        send_tool_message("Modificando una composición existente...")
        composicion = ComposicionTeclas.objects.get(id=id_composicion)
        
        if cantidad is not None:
            composicion.cantidad = cantidad
        if id_tecla is not None:
            tecla = Teclas.objects.get(id=id_tecla)
            composicion.tecla = tecla
        if composicion_familias is not None:
            # Aquí también podrías validar el formato JSON
            composicion.composicion = composicion_familias
            
        composicion.save()
        return {"success": True, "composicion": composicion.serialize()}
    except ObjectDoesNotExist:
        return {"success": False, "error": "Composición o tecla no encontrada"}
    except Exception as e:
        return {"success": False, "error": str(e)}

# El set de herramientas final, más limpio y claro:
tools = [
    crear_composicion_tecla,
    listar_composiciones,
    modificar_composicion
]