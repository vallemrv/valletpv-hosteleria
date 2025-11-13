from typing import List, Dict, Union, Optional
from django.core.exceptions import ObjectDoesNotExist
from langchain_core.tools import tool
from chatbot.utilidades.ws_sender import send_tool_message
from gestion.models.familias import Receptores
from gestion.tools.config_logs import log_debug_chatbot as logger
from django.db.models import Q
from chatbot.decorators.db_connection_manager import db_connection_handler


@tool
@db_connection_handler
def listar_receptores() -> List[Dict[str, Union[int, str, bool]]]:
    """
    Lista todos los receptores disponibles, excluyendo aquellos cuyo nombre sea 'Nulo'.

    Returns:
        List[Dict[str, Union[int, str, bool]]]: Lista de diccionarios con los datos de los receptores
        (id, nombre, nomimp, activo, descripcion).
    """
    send_tool_message("Listando receptores desde la base de datos...")
    logger.debug("Listando receptores desde la base de datos...")
    
    receptores = Receptores.objects.exclude(nombre__iexact="Nulo")
    return [
        {
            "id": receptor.id,
            "nombre": receptor.nombre,
            "nomimp": receptor.nomimp,
            "activo": receptor.activo,
            "descripcion": receptor.descripcion
        }
        for receptor in receptores
    ]


@tool
@db_connection_handler
def crear_receptor(
    nombre: str,
    nomimp: str,
    activo: bool = True,
    descripcion: str = ""
) -> Dict[str, Union[int, str, bool]]:
    """
    Crea un nuevo receptor en la base de datos.

    Args:
        nombre (str): Nombre del receptor.
        nomimp (str): Nombre de impresión del receptor.
        activo (bool, optional): Estado activo del receptor. Por defecto True.
        descripcion (str, optional): Descripción del receptor. Por defecto vacío.

    Returns:
        Dict[str, Union[int, str, bool]]: Diccionario con los datos del receptor creado
        (id, nombre, nomimp, activo, descripcion).
    """
    send_tool_message(f"Creando receptor con nombre '{nombre}'...")
    logger.debug(f"Creando receptor con nombre '{nombre}'...")
    
    receptor = Receptores(
        nombre=nombre,
        nomimp=nomimp,
        activo=activo,
        descripcion=descripcion
    )
    receptor.save()
    
    return {
        "id": receptor.id,
        "nombre": receptor.nombre,
        "nomimp": receptor.nomimp,
        "activo": receptor.activo,
        "descripcion": receptor.descripcion
    }


@tool
@db_connection_handler
def editar_receptor(
    receptor_id: int,
    nombre: Optional[str] = None,
    nomimp: Optional[str] = None,
    activo: Optional[bool] = None,
    descripcion: Optional[str] = None
) -> Dict[str, Union[int, str, bool]]:
    """
    Edita un receptor existente en la base de datos.

    Args:
        receptor_id (int): ID del receptor a editar.
        nombre (Optional[str], optional): Nuevo nombre del receptor.
        nomimp (Optional[str], optional): Nuevo nombre de impresión del receptor.
        activo (Optional[bool], optional): Nuevo estado activo del receptor.
        descripcion (Optional[str], optional): Nueva descripción del receptor.

    Returns:
        Dict[str, Union[int, str, bool]]: Diccionario con los datos actualizados del receptor
        o mensaje de error si no se encuentra.
    """
    try:
        receptor = Receptores.objects.get(id=receptor_id)
        send_tool_message(f"Editando receptor '{receptor.nombre}'...")
        logger.debug(f"Editando receptor '{receptor.nombre}'...")
        
        if nombre is not None:
            receptor.nombre = nombre
        if nomimp is not None:
            receptor.nomimp = nomimp
        if activo is not None:
            receptor.activo = activo
        if descripcion is not None:
            receptor.descripcion = descripcion
            
        receptor.save()
        
        return {
            "id": receptor.id,
            "nombre": receptor.nombre,
            "nomimp": receptor.nomimp,
            "activo": receptor.activo,
            "descripcion": receptor.descripcion
        }
    except ObjectDoesNotExist:
        logger.error(f"Receptor con ID {receptor_id} no encontrado")
        return {"error": f"Receptor con ID {receptor_id} no encontrado"}


@tool
@db_connection_handler
def borrar_receptor(receptor_id: int) -> Dict[str, str]:
    """
    Elimina un receptor de la base de datos.

    Args:
        receptor_id (int): ID del receptor a eliminar.

    Returns:
        Dict[str, str]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        receptor = Receptores.objects.get(id=receptor_id)
        send_tool_message(f"Eliminando receptor '{receptor.nombre}'...")
        logger.debug(f"Eliminando receptor '{receptor.nombre}'...")
        
        receptor.delete()
        return {"success": f"Receptor '{receptor.nombre}' eliminado correctamente"}
    except ObjectDoesNotExist:
        logger.error(f"Receptor con ID {receptor_id} no encontrado")
        return {"error": f"Receptor con ID {receptor_id} no encontrado"}


@tool
@db_connection_handler
def find_receptor_by_name(query: str) -> Union[List[Dict[str, Union[int, str, bool]]], Dict[str, str]]:
    """
    Busca receptores cuyo nombre, nomimp o descripcion coincidan con la expresión regular dada (búsqueda insensible a mayúsculas).

    Args:
        query (str): Expresión regular a buscar en nombre, nomimp o descripcion.

    Returns:
        Union[List[Dict[str, Union[int, str, bool]]], Dict[str, str]]: Lista de receptores encontrados o mensaje de error.
    """
    send_tool_message(f"Buscando receptores que coincidan con la expresión '{query}' (icontains)...")
    logger.debug(f"Buscando receptores que coincidan con la expresión '{query}' (icontains)...")
    receptores = Receptores.objects.filter(
        Q(nombre__icontains=query) | Q(nomimp__icontains=query) | Q(descripcion__icontains=query)
    ).exclude(nombre__iexact="Nulo")
    if not receptores:
        logger.error(f"No se encontraron receptores que coincidan con '{query}'")
        return {"error": f"No se encontraron receptores que coincidan con '{query}'"}
    return [
        {
            "id": receptor.id,
            "nombre": receptor.nombre,
            "nomimp": receptor.nomimp,
            "activo": receptor.activo,
            "descripcion": receptor.descripcion
        }
        for receptor in receptores
    ]


# Lista de herramientas disponibles
tools: List = [
    listar_receptores,
    crear_receptor,
    editar_receptor,
    borrar_receptor,
    find_receptor_by_name,
]