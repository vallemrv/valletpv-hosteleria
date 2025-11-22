from typing import List, Dict, Union, Optional
from django.core.exceptions import ObjectDoesNotExist
from langchain_core.tools import tool
from chatbot.utilidades.ws_sender import send_tool_message
from gestion.models.familias import Secciones
from gestion.models.teclados import ICON_CHOICES, SeccionesCom, Teclascom, Teclas, Teclaseccion
from gestion.tools.config_logs import log_debug_chatbot as logger
from chatbot.decorators.db_connection_manager import db_connection_handler

@tool
@db_connection_handler
def crear_seccion_global(
    nombre: str,
    rgb: str
) -> Dict[str, Union[int, str]]:
    """
    Crea una nueva sección en la base de datos para la organización de artículos.

    Args:
        nombre (str): Nombre de la sección.
        rgb (str): Color de la sección en formato RGB (por ejemplo, "255,0,0") sin espacios ni ningun signo.
     
    Returns:
        Dict[str, Union[int, str]]: Diccionario con los detalles de la sección creada
        (id, nombre, rgb).
    """
    send_tool_message(f"Creando sección '{nombre}' con color RGB '{rgb}'...")
    
    seccion = Secciones(nombre=nombre, rgb=rgb)
    seccion.save()
    
    return {
        "id": seccion.id,
        "nombre": seccion.nombre,
        "rgb": seccion.rgb,  
    }


@tool
@db_connection_handler
def borrar_seccion(seccion_id: int) -> Dict[str, str]:
    """
    Elimina una sección existente de la base de datos.

    Args:
        seccion_id (int): ID de la sección a eliminar.

    Returns:
        Dict[str, str]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        seccion = Secciones.objects.get(id=seccion_id)
        send_tool_message(f"Eliminando sección '{seccion.nombre}'...")
        
        seccion.delete()
        return {"success": f"Sección '{seccion.nombre}' eliminada correctamente"}
    except ObjectDoesNotExist:
        logger.error(f"Sección con ID {seccion_id} no encontrada")
        return {"error": f"Sección con ID {seccion_id} no encontrada"}


@tool
@db_connection_handler
def modificar_seccion(
    seccion_id: int,
    nombre: Optional[str] = None,
    rgb: Optional[str] = None,
) -> Dict[str, Union[int, str]]:
    """
    Modifica los atributos de una sección existente en la base de datos.

    Args:
        seccion_id (int): ID de la sección a modificar.
        nombre (Optional[str], optional): Nuevo nombre de la sección.
        rgb (Optional[str], optional): Nuevo color en formato RGB (por ejemplo, "0,255,0") sin espacios ni ningun signo.
      
    Returns:
        Dict[str, Union[int, str]]: Diccionario con los datos actualizados de la sección
        (id, nombre, rgb) o mensaje de error si la sección no se encuentra.
    """
    try:
        seccion = Secciones.objects.get(id=seccion_id)
        send_tool_message(f"Modificando sección '{seccion.nombre}'...")
        
        if nombre is not None:
            seccion.nombre = nombre
        if rgb is not None:
            seccion.rgb = rgb
       
            
        seccion.save()
        
        return {
            "id": seccion.id,
            "nombre": seccion.nombre,
            "rgb": seccion.rgb,
           
        }
    except ObjectDoesNotExist:
        logger.error(f"Sección con ID {seccion_id} no encontrada")
        return {"error": f"Sección con ID {seccion_id} no encontrada"}


@tool
@db_connection_handler
def listar_secciones_globales() -> List[Dict[str, Union[int, str]]]:
    """
    Lista todas las secciones globales disponibles en la base de datos, utilizadas para la organización de artículos (no visibles en el TPV).

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los datos de las secciones
        (id, nombre, rgb).
    """
    send_tool_message("Listando todas las secciones disponibles...")
    
    secciones = Secciones.objects.all()
    return [
        {
            "id": seccion.id,
            "nombre": seccion.nombre,
            "rgb": seccion.rgb,
            
        }
        for seccion in secciones
    ]


@tool
@db_connection_handler
def listar_iconos_secciones_comanda() -> List[Dict[str, str]]:
    """
    Obtiene la lista de iconos disponibles para las secciones de comandas visibles en el TPV.

    Returns:
        List[Dict[str, str]]: Lista de diccionarios con la clave y el nombre de cada icono disponible.
    """
    send_tool_message("Listando iconos disponibles para las secciones de comandas...")
    
    return [name for  name in dict(ICON_CHOICES).values()]


@tool
@db_connection_handler
def modificar_icono_seccion(
    seccion_id: int,
    nuevo_icono: str
) -> Dict[str, Union[int, str]]:
    """
    Modifica el icono de una sección de comandas visible en el TPV.

    Args:
        seccion_id (int): ID de la sección de comandas a modificar.
        nuevo_icono (str): Nombre del icono a usar (debe corresponder a un value en ICON_CHOICES).

    Returns:
        Dict[str, Union[int, str]]: Diccionario con los datos actualizados de la sección
        (id, nombre, icono) o mensaje de error si la sección no se encuentra.
    """
    try:
        seccion = SeccionesCom.objects.get(id=seccion_id)
        send_tool_message(f"Modificando icono de la sección '{seccion.nombre}' a '{nuevo_icono}'...")
        
        # Buscar la key correspondiente al value en ICON_CHOICES
        icon_dict = dict(ICON_CHOICES)
        icon_key = None
        for key, value in icon_dict.items():
            if value.lower() == nuevo_icono.lower():
                icon_key = key
                break
        
        if icon_key is None:
            logger.error(f"Icono '{nuevo_icono}' no válido")
            return {"error": f"Icono '{nuevo_icono}' no está en la lista de iconos disponibles"}
            
        seccion.icono = icon_key
        seccion.save()
        
        return {
            "id": seccion.id,
            "nombre": seccion.nombre,
            "icono": seccion.get_icono_display()  # Devuelve el nombre del icono
        }
    except ObjectDoesNotExist:
        logger.error(f"Sección de comanda con ID {seccion_id} no encontrada")
        return {"error": f"Sección de comanda con ID {seccion_id} no encontrada"}


@tool
@db_connection_handler
def listar_secciones_comanda() -> List[Dict[str, Union[int, str]]]:
    """
    Lista todas las secciones de comandas visibles en el TPV, incluyendo solo nombre e icono.

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los datos de las secciones
        (id, nombre, icono).
    """
    send_tool_message("Listando nombres e iconos de las secciones de comandas...")
    
    secciones = SeccionesCom.objects.all()
    return [
        {
            "id": seccion.id,
            "nombre": seccion.nombre,
            "icono": seccion.get_icono_display()  # Devuelve el nombre del icono
        }
        for seccion in secciones
    ]


@tool
@db_connection_handler
def asociar_tecla_a_seccion(
    tecla_ids: List[int],
    seccion_id: int
) -> Dict[str, Union[str, List[str]]]:
    """
    Asocia una tecla con una sección (Seccion Global) en la tabla Teclaseccion.
    Esta herramienta borra las asociaciones anteriores de la tecla con la sección antes de crear una nueva asociación.
    
    Args:
        tecla_ids (List[int]): Lista de IDs de las teclas a asociar.
        seccion_id (int): ID de la sección a asociar.

    Returns:
        Dict[str, Union[str, List[str]]]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        seccion = Secciones.objects.get(id=seccion_id)
        send_tool_message(f"Asociando teclas a la sección '{seccion.nombre}'...")
        
        success_teclas = []
        for tecla_id in tecla_ids:
            try:
                tecla = Teclas.objects.get(id=tecla_id)
                # Eliminar teclas anteriores asociadas a la sección
                Teclaseccion.objects.filter(tecla=tecla).delete()
                Teclaseccion.objects.create(tecla=tecla, seccion=seccion)
                success_teclas.append(tecla.nombre)
            except Teclas.DoesNotExist:
                logger.error(f"Tecla con ID {tecla_id} no encontrada")
        
        return {"success": f"Teclas asociadas correctamente a la sección '{seccion.nombre}'", "teclas": success_teclas}
    except Secciones.DoesNotExist:
        logger.error(f"Sección con ID {seccion_id} no encontrada")
        return {"error": f"Sección con ID {seccion_id} no encontrada"}


@tool
@db_connection_handler
def asociar_tecla_a_seccion_comanda(
    tecla_ids: List[int],
    seccion_com_id: int
) -> Dict[str, Union[str, List[str]]]:
    """
    Asocia una tecla con una sección de comandas (Seccion que se muetra en el TPV), verificando que la sección no tenga más de 18 teclas.

    Args:
        tecla_ids (List[int]): Lista de IDs de las teclas a asociar.
        seccion_com_id (int): ID de la sección de comandas a asociar.

    Returns:
        Dict[str, Union[str, List[str]]]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        seccion_com = SeccionesCom.objects.get(id=seccion_com_id)
        send_tool_message(f"Asociando teclas a la sección de comanda '{seccion_com.nombre}'...")
        
        # Verificar si la sección ya tiene 18 teclas
        if seccion_com.teclascom_set.count() + len(tecla_ids) > 18:
            logger.error(f"Sección de comanda '{seccion_com.nombre}' excederá el límite de 18 teclas asociadas")
            return {"error": f"Sección de comanda '{seccion_com.nombre}' excederá el límite de 18 teclas asociadas"}

        success_teclas = []
        for tecla_id in tecla_ids:
            try:
                tecla = Teclas.objects.get(id=tecla_id)
                # Eliminar teclas anteriores asociadas a la sección
                Teclascom.objects.filter(tecla=tecla).delete()
                Teclascom.objects.create(tecla=tecla, seccion=seccion_com)
                success_teclas.append(tecla.nombre)
            except Teclas.DoesNotExist:
                logger.error(f"Tecla con ID {tecla_id} no encontrada")
        
        return {"success": f"Teclas asociadas correctamente a la sección de comanda '{seccion_com.nombre}'", "teclas": success_teclas}
    except SeccionesCom.DoesNotExist:
        logger.error(f"Sección de comanda con ID {seccion_com_id} no encontrada")
        return {"error": f"Sección de comanda con ID {seccion_com_id} no encontrada"}


@tool
@db_connection_handler
def quitar_asociacion_tecla_a_seccion(tecla_id: int) -> Dict[str, str]:
    """
    Elimina la asociación de una tecla con cualquier sección en la tabla Teclaseccion.

    Args:
        tecla_id (int): ID de la tecla cuya asociación se quiere eliminar.

    Returns:
        Dict[str, str]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        tecla = Teclas.objects.get(id=tecla_id)
        send_tool_message(f"Quitando asociación de la tecla '{tecla.nombre}' con su sección...")
        
        deleted_count = Teclaseccion.objects.filter(tecla=tecla).delete()[0]
        if deleted_count > 0:
            return {"success": f"Asociación de la tecla '{tecla.nombre}' con su sección eliminada correctamente"}
        else:
            return {"success": f"La tecla '{tecla.nombre}' no estaba asociada a ninguna sección"}
            
    except Teclas.DoesNotExist:
        logger.error(f"Tecla con ID {tecla_id} no encontrada")
        return {"error": f"Tecla con ID {tecla_id} no encontrada"}


@tool
@db_connection_handler
def quitar_asociacion_tecla_a_seccion_comanda(tecla_id: int) -> Dict[str, str]:
    """
    Elimina la asociación de una tecla con cualquier sección de comandas.

    Args:
        tecla_id (int): ID de la tecla cuya asociación se quiere eliminar.

    Returns:
        Dict[str, str]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        tecla = Teclas.objects.get(id=tecla_id)
        send_tool_message(f"Quitando asociación de la tecla '{tecla.nombre}' con su sección de comanda...")
        
        deleted_count = Teclascom.objects.filter(tecla=tecla).delete()[0]
        if deleted_count > 0:
            return {"success": f"Asociación de la tecla '{tecla.nombre}' con su sección de comanda eliminada correctamente"}
        else:
            return {"success": f"La tecla '{tecla.nombre}' no estaba asociada a ninguna sección de comanda"}
            
    except Teclas.DoesNotExist:
        logger.error(f"Tecla con ID {tecla_id} no encontrada")
        return {"error": f"Tecla con ID {tecla_id} no encontrada"}


@tool
@db_connection_handler
def find_seccion_by_name(query: str) -> Union[List[Dict[str, Union[int, str]]], Dict[str, str]]:
    """
    Busca secciones por nombre usando una expresión regular insensible a mayúsculas.
    es util para encontrar ids, nombres, colores.
    Args:
        query (str): Expresión regular para buscar en los nombres.

    Returns:
        Union[List[Dict[str, Union[int, str]]], Dict[str, str]]: Lista de secciones encontradas (id, nombre, color) 
        o mensaje de error si no hay coincidencias.
    """
    send_tool_message(f"Buscando secciones que coincidan con la expresión '{query}' (icontains)...")

    while len(query) >= 3:
        secciones = Secciones.objects.filter(nombre__icontains=query)
        if secciones:
            return [
                {
                    "id": seccion.id,
                    "nombre": seccion.nombre,
                    "rgb": seccion.rgb
                }
                for seccion in secciones
            ]
        query = query[:-3]  # Reducir el filtro en 3 caracteres
    
    return {"error": "No se encontraron secciones que coincidan con el filtro proporcionado, incluso después de reducirlo."}


@tool
@db_connection_handler
def find_seccion_comanda_by_name(query: str) -> Union[List[Dict[str, Union[int, str]]], Dict[str, str]]:
    """
    Busca secciones de comanda por nombre usando una expresión regular insensible a mayúsculas.
    Util para encontrar ids, nombres y iconos de las secciones de comanda.
    
    Args:
        query (str): Expresión regular para buscar en los nombres.

    Returns:
        Union[List[Dict[str, Union[int, str]]], Dict[str, str]]: Lista de secciones encontradas (id, nombre, icono) 
        o mensaje de error si no hay coincidencias.
    """
    send_tool_message(f"Buscando secciones de comanda que coincidan con la expresión '{query}' (icontains)...")

    while len(query) >= 3:
        secciones = SeccionesCom.objects.filter(nombre__icontains=query)
        if secciones:
            return [
                {
                    "id": seccion.id,
                    "nombre": seccion.nombre,
                    "icono": seccion.icono
                }
                for seccion in secciones
            ]
        query = query[:-3]  # Reducir el filtro en 3 caracteres
    
    return {"error": "No se encontraron secciones de comanda que coincidan con el filtro proporcionado, incluso después de reducirlo."}


# Lista de herramientas disponibles
tools: List = [
    crear_seccion_global,
    borrar_seccion,
    modificar_seccion,
    listar_secciones_globales,
    listar_iconos_secciones_comanda,
    modificar_icono_seccion,
    listar_secciones_comanda,
    asociar_tecla_a_seccion,
    asociar_tecla_a_seccion_comanda,
    quitar_asociacion_tecla_a_seccion,
    quitar_asociacion_tecla_a_seccion_comanda,
    find_seccion_by_name,
    find_seccion_comanda_by_name,
]