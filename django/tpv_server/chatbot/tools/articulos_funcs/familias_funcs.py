from typing import List, Dict, Union, Optional
from django.core.exceptions import ObjectDoesNotExist
from langchain_core.tools import tool
from chatbot.utilidades.ws_sender import send_tool_message
from gestion.models.familias import Familias, Receptores
from gestion.tools.config_logs import log_debug_chatbot as logger
from django.db.models import Q
import json
from chatbot.decorators.db_connection_manager import db_connection_handler


@tool
@db_connection_handler
def crear_familia(
    nombre: str,
    receptor_id: int,
    composicion: List[str] = [],
    cantidad: int = 0,
) -> Dict[str, Union[int, str]]:
    """
    Crea una nueva familia en la base de datos y la asocia con un receptor.

    Args:
        nombre (str): Nombre de la familia.
        composicion (List[str]): Lista de nombres de familias para la composición, que se convierte a JSON para almacenamiento (opcional).
        cantidad (int): Número de tapas de la familia (opcional).
        receptor_id (int): ID del receptor asociado a la familia.

    Returns:
        Dict[str, Union[int, str]]: Diccionario con los datos de la familia creada
        (id, nombre, composicion, cantidad, receptor_id) o mensaje de error si el receptor no se encuentra.
    """
    try:
        receptor = Receptores.objects.get(id=receptor_id)
        send_tool_message(f"Creando familia '{nombre}' asociada al receptor '{receptor.nombre}'...")
        logger.debug(f"Creando familia '{nombre}' asociada al receptor '{receptor.nombre}'...")
        
        # Convertir la lista de strings a JSON para almacenamiento
        composicion_str = json.dumps(composicion) if composicion else json.dumps([])
        
        familia = Familias(
            nombre=nombre,
            composicion=composicion_str,
            cantidad=cantidad,
            receptor=receptor
        )
        familia.save()
        
        return {
            "id": familia.id,
            "nombre": familia.nombre,
            "composicion": familia.composicion,
            "cantidad": familia.cantidad,
            "receptor_id": receptor.id
        }
    except ObjectDoesNotExist:
        logger.error(f"Receptor con ID {receptor_id} no encontrado")
        return {"error": f"Receptor con ID {receptor_id} no encontrado"}


@tool
@db_connection_handler
def borrar_familia(familia_id: int) -> Dict[str, str]:
    """
    Elimina una familia existente de la base de datos.

    Args:
        familia_id (int): ID de la familia a eliminar.

    Returns:
        Dict[str, str]: Mensaje de éxito o error según el resultado de la operación.
    """
    try:
        familia = Familias.objects.get(id=familia_id)
        
        # Verificar si existen teclas asociadas a la familia
        if familia.teclas_set.exists():
            return {"error": f"No se puede borrar la familia '{familia.nombre}' porque tiene teclas asociadas"}
        
        send_tool_message(f"Eliminando familia '{familia.nombre}'...")
        
        familia.delete()
        return {"success": f"Familia '{familia.nombre}' eliminada correctamente"}
    except ObjectDoesNotExist:
        return {"error": f"Familia con ID {familia_id} no encontrada"}


@tool
@db_connection_handler
def modificar_familia(
    familia_id: int,
    nombre: Optional[str] = None,
    composicion: Optional[List[str]] = None,
    cantidad: Optional[int] = None,
    receptor_id: Optional[int] = None
) -> Dict[str, Union[int, str]]:
    """
    Modifica los atributos de una familia existente en la base de datos.
    Solo puede modificar los campos de una familia unica. Para multiples familias llamar
    a esta función varias veces con los IDs correspondientes.
    
    Args:
        familia_id (int): ID de la familia a modificar.
        nombre (Optional[str], optional): Nuevo nombre de la familia.
        composicion (Optional[List[str]], optional): Nueva lista de nombres de familias para la composición,
            que se convierte a JSON para almacenamiento.
        cantidad (Optional[int], optional): Nueva cantidad de tapas.
        receptor_id (Optional[int], optional): Nuevo ID del receptor asociado.

    Returns:
        Dict[str, Union[int, str]]: Diccionario con los datos actualizados de la familia
        (id, nombre, composicion, cantidad, receptor_id) o mensaje de error si la familia o receptor no se encuentran.
    """
    try:
        familia = Familias.objects.get(id=familia_id)
        nombre_anterior = familia.nombre
        send_tool_message(f"Modificando familia '{familia.nombre}'...")
        logger.debug(f"Modifying familia '{familia.nombre}'...")
        
        # Validar composición si se está modificando
        if composicion is not None:
            familias_no_existentes = []
            for nombre_familia in composicion:
                if not Familias.objects.filter(nombre=nombre_familia).exists():
                    familias_no_existentes.append(nombre_familia)
            
            if familias_no_existentes:
                error_msg = f"Las siguientes familias no existen: {', '.join(familias_no_existentes)}"
                logger.error(error_msg)
                send_tool_message(error_msg)
                return {"error": error_msg}
        
        # Guardar el nombre anterior para actualizar composiciones si es necesario
        actualizar_composiciones = False
        
        if nombre is not None and nombre != nombre_anterior:
            logger.debug(f"Cambiando nombre de familia de '{nombre_anterior}' a '{nombre}'")
            familia.nombre = nombre
            actualizar_composiciones = True
            
        if composicion is not None:
            # Convertir la lista de strings a JSON para almacenamiento (ya validada arriba)
            familia.composicion = json.dumps(composicion) if composicion else json.dumps([])
            
        if cantidad is not None:
            familia.cantidad = cantidad
            
        if receptor_id is not None:
            try:
                receptor = Receptores.objects.get(id=receptor_id)
                familia.receptor = receptor
            except ObjectDoesNotExist:
                logger.error(f"Receptor con ID {receptor_id} no encontrado")
                return {"error": f"Receptor con ID {receptor_id} no encontrado"}
                
        familia.save()
        
        # Actualizar composiciones de otras familias si se cambió el nombre
        if actualizar_composiciones:
            logger.debug(f"Buscando familias con '{nombre_anterior}' en su composición para actualizar")
            familias_con_composicion = Familias.objects.filter(
                Q(composicion__icontains=nombre_anterior) & ~Q(id=familia_id)
            )
            
            familias_actualizadas = 0
            for fam in familias_con_composicion:
                if fam.composicion:
                    try:
                        # Cargar la composición como JSON
                        elementos_composicion = json.loads(fam.composicion)
                        elementos_actualizados = [
                            nombre if elem == nombre_anterior else elem 
                            for elem in elementos_composicion
                        ]
                        
                        # Solo actualizar si hubo cambios
                        if elementos_actualizados != elementos_composicion:
                            fam.composicion = json.dumps(elementos_actualizados)
                            fam.save()
                            familias_actualizadas += 1
                            logger.debug(f"Actualizada composición de familia '{fam.nombre}': {fam.composicion}")
                    except json.JSONDecodeError as e:
                        logger.error(f"Error decodificando JSON en composición de familia '{fam.nombre}': {str(e)}")
                        continue
            
            if familias_actualizadas > 0:
                send_tool_message(f"Actualizadas {familias_actualizadas} familias que tenían '{nombre_anterior}' en su composición")
                logger.info(f"Actualizadas {familias_actualizadas} familias que tenían '{nombre_anterior}' en su composición")
        
        return {
            "id": familia.id,
            "nombre": familia.nombre,
            "composicion": familia.composicion,
            "cantidad": familia.cantidad,
            "receptor_id": familia.receptor.id
        }
    except ObjectDoesNotExist:
        logger.error(f"Familia con ID {familia_id} no encontrada")
        return {"error": f"Familia con ID {familia_id} no encontrada"}


@tool
@db_connection_handler
def listar_todas_familias() -> List[Dict[str, Union[int, str]]]:
    """
    Lista todas las familias disponibles en la base de datos.

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los datos de las familias
        (id, nombre, composicion, cantidad, receptor_id).
    """
    send_tool_message("Listando todas las familias disponibles...")
    logger.debug("Listando todas las familias disponibles...")
    
    familias = Familias.objects.all()
    return [
        {
            "id": familia.id,
            "nombre": familia.nombre,
            "composicion": familia.composicion,
            "cantidad": familia.cantidad,
            "receptor_id": familia.receptor.id
        }
        for familia in familias
    ]


@tool
@db_connection_handler
def listar_teclas_por_familia(familia_id: int) -> Union[List[Dict[str, Union[int, str, float]]], Dict[str, str]]:
    """
    Lista todas las teclas asociadas a una familia específica.

    Args:
        familia_id (int): ID de la familia cuyas teclas se desean listar.

    Returns:
        Union[List[Dict[str, Union[int, str, float]]], Dict[str, str]]: Lista de diccionarios con los datos de las teclas
        (id, nombre, p1, p2, descripcion_r, descripcion_t) o mensaje de error si la familia no se encuentra.
    """
    try:
        familia = Familias.objects.get(id=familia_id)
        send_tool_message(f"Listando teclas asociadas a la familia '{familia.nombre}'...")
        logger.debug(f"Listando teclas asociadas a la familia '{familia.nombre}'...")
        
        teclas = familia.teclas_set.all()
        return [
            {
                "id": tecla.id,
                "nombre": tecla.nombre,
                "p1": float(tecla.p1),
                "p2": float(tecla.p2),
                "descripcion_r": tecla.descripcion_r,
                "descripcion_t": tecla.descripcion_t
            }
            for tecla in teclas
        ]
    except ObjectDoesNotExist:
        logger.error(f"Familia con ID {familia_id} no encontrada")
        return {"error": f"Familia con ID {familia_id} no encontrada"}


@tool
@db_connection_handler
def find_familia_by_name(query: str) -> Union[List[Dict[str, Union[int, str]]], Dict[str, str]]:
    """
    Busca familias cuyo nombre coincida con la expresión regular dada (búsqueda insensible a mayúsculas).

    Args:
        query (str): Expresión regular a buscar en el nombre de la familia.

    Returns:
        Union[List[Dict[str, Union[int, str]]], Dict[str, str]]: Lista de familias encontradas o mensaje de error.
    """
    send_tool_message(f"Buscando familias que coincidan con la expresión '{query}' (icontains)...")
    logger.debug(f"Buscando familias que coincidan con la expresión '{query}' (icontains)...")

    while len(query) >= 3:
        familias = Familias.objects.filter(nombre__icontains=query)
        if familias:
            return [
                {
                    "id": familia.id,
                    "nombre": familia.nombre,
                    "composicion": familia.composicion,
                    "cantidad": familia.cantidad,
                    "receptor_id": familia.receptor.id
                }
                for familia in familias
            ]
        query = query[:-3]  # Reducir la longitud de la query en 3 caracteres
    
    return {"error": "No se encontraron familias que coincidan con la consulta reducida"}


@tool
@db_connection_handler
def listar_familias_por_receptor(receptor_id: int) -> Union[List[Dict[str, Union[int, str]]], Dict[str, str]]:
    """
    Lista todas las familias asociadas a un receptor específico.

    Args:
        receptor_id (int): ID del receptor cuyas familias se desean listar.

    Returns:
        Union[List[Dict[str, Union[int, str]]], Dict[str, str]]: Lista de diccionarios con los datos de las familias
        (id, nombre, composicion, cantidad, receptor_id) o mensaje de error si el receptor no se encuentra.
    """
    try:
        receptor = Receptores.objects.get(id=receptor_id)
        send_tool_message(f"Listando familias asociadas al receptor '{receptor.nombre}'...")
        logger.debug(f"Listando familias asociadas al receptor '{receptor.nombre}'...")
        familias = Familias.objects.filter(receptor=receptor)
        return [
            {
                "id": familia.id,
                "nombre": familia.nombre,
                "composicion": familia.composicion,
                "cantidad": familia.cantidad,
                "receptor_id": receptor.id
            }
            for familia in familias
        ]
    except ObjectDoesNotExist:
        logger.error(f"Receptor con ID {receptor_id} no encontrado")
        return {"error": f"Receptor con ID {receptor_id} no encontrado"}


@tool
@db_connection_handler
def agregar_familia_a_composicion(
    familia_id: int,
    nombre_familia_agregar: str
) -> Dict[str, Union[int, str, List[str]]]:
    """
    Agrega una familia a la composición de otra familia existente.

    Args:
        familia_id (int): ID de la familia a la que se agregará la composición.
        nombre_familia_agregar (str): Nombre de la familia a agregar en la composición.

    Returns:
        Dict[str, Union[int, str, List[str]]]: Diccionario con los datos actualizados de la familia
        incluyendo la composición como lista, o mensaje de error.
    """
    try:
        familia = Familias.objects.get(id=familia_id)
        send_tool_message(f"Agregando familia '{nombre_familia_agregar}' a la composición de '{familia.nombre}'...")
        logger.debug(f"Agregando familia '{nombre_familia_agregar}' a la composición de '{familia.nombre}'...")
        
        # Verificar que la familia a agregar existe
        if not Familias.objects.filter(nombre=nombre_familia_agregar).exists():
            logger.warning(f"La familia '{nombre_familia_agregar}' no existe en la base de datos")
            return {"error": f"La familia '{nombre_familia_agregar}' no existe"}
        
        # Convertir la composición actual a lista
        composicion_actual = []
        if familia.composicion:
            try:
                composicion_actual = json.loads(familia.composicion)
                if not isinstance(composicion_actual, list):
                    logger.error(f"La composición no es una lista válida: {familia.composicion}")
                    composicion_actual = []
            except json.JSONDecodeError as e:
                logger.error(f"Error decodificando JSON de composición: {str(e)}")
                composicion_actual = []
        
        # Verificar si la familia ya está en la composición
        if nombre_familia_agregar in composicion_actual:
            logger.info(f"La familia '{nombre_familia_agregar}' ya está en la composición")
            return {"info": f"La familia '{nombre_familia_agregar}' ya está en la composición de '{familia.nombre}'"}
        
        # Agregar la nueva familia a la composición
        composicion_actual.append(nombre_familia_agregar)
        
        # Convertir de vuelta a JSON para almacenamiento
        familia.composicion = json.dumps(composicion_actual)
        familia.save()
        
        logger.info(f"Familia '{nombre_familia_agregar}' agregada exitosamente a la composición")
        
        return {
            "id": familia.id,
            "nombre": familia.nombre,
            "composicion": familia.composicion,
            "composicion_lista": composicion_actual,
            "cantidad": familia.cantidad,
            "receptor_id": familia.receptor.id
        }
        
    except ObjectDoesNotExist:
        logger.error(f"Familia con ID {familia_id} no encontrada")
        return {"error": f"Familia con ID {familia_id} no encontrada"}
    except Exception as e:
        logger.error(f"Error en agregar_familia_a_composicion(): {str(e)}", exc_info=True)
        return {"error": f"Error interno: {str(e)}"}


@tool
@db_connection_handler
def quitar_familia_de_composicion(
    familia_id: int,
    nombre_familia_quitar: str
) -> Dict[str, Union[int, str, List[str]]]:
    """
    Elimina una familia de la composición de otra familia existente.

    Args:
        familia_id (int): ID de la familia de la que se quitará la composición.
        nombre_familia_quitar (str): Nombre de la familia a quitar de la composición.

    Returns:
        Dict[str, Union[int, str, List[str]]]: Diccionario con los datos actualizados de la familia
        incluyendo la composición como lista, o mensaje de error.
    """
    try:
        familia = Familias.objects.get(id=familia_id)
        send_tool_message(f"Quitando familia '{nombre_familia_quitar}' de la composición de '{familia.nombre}'...")
        logger.debug(f"Quitando familia '{nombre_familia_quitar}' de la composición de '{familia.nombre}'...")
        
        # Convertir la composición actual a lista
        composicion_actual = []
        if familia.composicion:
            try:
                composicion_actual = json.loads(familia.composicion)
                if not isinstance(composicion_actual, list):
                    logger.error(f"La composición no es una lista válida: {familia.composicion}")
                    composicion_actual = []
            except json.JSONDecodeError as e:
                logger.error(f"Error decodificando JSON de composición: {str(e)}")
                composicion_actual = []
        
        # Verificar si la familia está en la composición
        if nombre_familia_quitar not in composicion_actual:
            logger.info(f"La familia '{nombre_familia_quitar}' no está en la composición")
            return {"info": f"La familia '{nombre_familia_quitar}' no está en la composición de '{familia.nombre}'"}
        
        # Quitar la familia de la composición
        composicion_actual = [elem for elem in composicion_actual if elem != nombre_familia_quitar]
        
        # Convertir de vuelta a JSON para almacenamiento
        familia.composicion = json.dumps(composicion_actual)
        familia.save()
        
        logger.info(f"Familia '{nombre_familia_quitar}' quitada exitosamente de la composición")
        
        return {
            "id": familia.id,
            "nombre": familia.nombre,
            "composicion": familia.composicion,
            "composicion_lista": composicion_actual,
            "cantidad": familia.cantidad,
            "receptor_id": familia.receptor.id
        }
        
    except ObjectDoesNotExist:
        logger.error(f"Familia con ID {familia_id} no encontrada")
        return {"error": f"Familia con ID {familia_id} no encontrada"}
    except Exception as e:
        logger.error(f"Error en quitar_familia_de_composicion(): {str(e)}", exc_info=True)
        return {"error": f"Error interno: {str(e)}"}


# Lista de herramientas disponibles
tools: List = [
    crear_familia,
    borrar_familia,
    modificar_familia,
    listar_todas_familias,
    listar_teclas_por_familia,
    find_familia_by_name,
    listar_familias_por_receptor,
    agregar_familia_a_composicion,
    quitar_familia_de_composicion,
]