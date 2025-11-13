# -*- coding: utf-8 -*-
from django.db.models import  Value, CharField, Q
from django.db.models.functions import Concat
from langchain_core.tools import tool
from django.db.models import ProtectedError 
from typing import Optional, List, Dict, Union, Any
from chatbot.utilidades.ws_sender import send_tool_message
from gestion.models.pedidos import Pedidos
from gestion.models.camareros import Camareros, PERMISOS_CHOICES
from django.db.models import ProtectedError
from gestion.tools.config_logs import log_debug_chatbot as logger
from chatbot.decorators.db_connection_manager import db_connection_handler
from asgiref.sync import sync_to_async

import json


@tool
@db_connection_handler
@sync_to_async  
def find_waiters(
    name_query: Optional[str] = None,
    status: Optional[str] = None,
    has_permissions: Optional[bool] = None
) -> Union[List[Dict[str, Any]], str]:
    """
    Busca y devuelve una lista de camareros según varios criterios.
    - Para buscar por nombre/apellidos, usa 'name_query'.
    - Para filtrar por estado, usa 'status'. Valores válidos: 'activo', 'inactivo', 'autorizado', 'no_autorizado'.
    - Para buscar solo camareros con permisos, usa 'has_permissions=True'.
    Si no se proporciona ningún filtro, devuelve todos los camareros (activos, inactivos, autorizados y no autorizados).

    Args:
        name_query (Optional[str]): Parte del nombre y/o apellidos a buscar (mín 3 caracteres).
        status (Optional[str]): Filtra por estado. Puede ser 'activo', 'inactivo', 'autorizado', o 'no_autorizado'.
        has_permissions (Optional[bool]): Si es True, solo devuelve camareros con permisos asignados.

    Returns:
        Union[List[Dict[str, Any]], str]: Lista de camareros o un mensaje si no hay resultados.
    """
    send_tool_message(f"Buscando camareros con filtros: nombre='{name_query}', estado='{status}', con_permisos='{has_permissions}'")
    
    queryset = Camareros.objects.all()

    if status:
        if status == 'activo':
            queryset = queryset.filter(activo=True)
        elif status == 'inactivo':
            queryset = queryset.filter(activo=False)
        elif status == 'autorizado':
            queryset = queryset.filter(autorizado=True, activo=True)
        elif status == 'no_autorizado':
            queryset = queryset.filter(autorizado=False, activo=True)
    # Removido el filtro por defecto para incluir todos los camareros (activos, inactivos, autorizados y no autorizados)

    if name_query:
        if len(name_query) < 3:
            return "La búsqueda por nombre requiere al menos 3 caracteres."

        # Creamos el campo 'nombre_completo' para poder buscar en él
        queryset = queryset.annotate(
            nombre_completo=Concat('nombre', Value(' '), 'apellidos', output_field=CharField())
        )

        # Usamos Q objects para buscar si el texto está en el nombre, O en los apellidos, O en el nombre completo
        queryset = queryset.filter(
            Q(nombre__icontains=name_query) |
            Q(apellidos__icontains=name_query) |
            Q(nombre_completo__icontains=name_query)
        )

    if has_permissions is True:
        queryset = queryset.exclude(permisos__in=["", "[]", None])
    
    if not queryset.exists():
        return "No se encontraron camareros que coincidan con los criterios de búsqueda."

    try:
        # Devolvemos un conjunto de campos consistente
        return [{
            "id": c.id, "nombre": c.nombre, "apellidos": c.apellidos,
            "activo": c.activo, "autorizado": c.autorizado,
            "permisos": c.permisos
        } for c in queryset]
    except Exception as e:
        return f"Ocurrió un error al buscar camareros: {str(e)}"
    

@tool
@db_connection_handler
@sync_to_async 
def update_waiters(updates: List[Dict[str, Any]]) -> Dict[str, list]:
    """
    Actualiza múltiples campos de uno o más camareros en una sola operación.
    Para cada camarero en la lista 'updates', puedes modificar cualquier campo.
    Los campos no proporcionados no se modificarán.

    Args:
        updates (List[Dict[str, Any]]): Una lista de diccionarios. Cada diccionario debe tener 'waiter_id'
            y puede tener cualquiera de las siguientes claves opcionales:
            - 'nombre' (str): Nuevo nombre.
            - 'apellidos' (str): Nuevos apellidos.
            - 'activo' (bool): True para contratar, False para despedir.
            - 'autorizado' (bool): True para autorizar turno, False para dar libre.
            - 'permisos' (List[str]): REEMPLAZA la lista de permisos actual. Una lista vacía `[]` elimina todos.
            - 'clear_password' (bool): Si es True, borra la contraseña del camarero.

    Returns:
        Dict[str, list]: Un diccionario con un resumen de los resultados y los errores.
    """
    results, errors = [], []
    if not updates:
        return {"results": [], "errors": ["La lista de actualizaciones está vacía."]}
    
    send_tool_message(f"Procesando {len(updates)} actualizaciones de camareros.")

    for update_data in updates:
        waiter_id = update_data.get('waiter_id')
        if not waiter_id:
            errors.append({"error": "Falta 'waiter_id' en un objeto de la lista.", "data": update_data})
            continue

        try:
            camarero = Camareros.objects.get(id=waiter_id)
            updated_fields = []

            # Actualizar campos simples
            if 'nombre' in update_data and update_data['nombre']:
                camarero.nombre = update_data['nombre']
                updated_fields.append('nombre')
            
            if 'apellidos' in update_data and update_data['apellidos']:
                camarero.apellidos = update_data['apellidos']
                updated_fields.append('apellidos')

            if 'activo' in update_data:
                camarero.activo = update_data['activo']
                # Regla de negocio: si se desactiva, también se desautoriza.
                if not camarero.activo:
                    camarero.autorizado = False
                updated_fields.append('activo')
                
            if 'autorizado' in update_data:
                # Se puede DESAUTORIZAR en cualquier momento.
                if update_data['autorizado'] is False:
                    camarero.autorizado = False
                    # Evita añadir el campo dos veces si ya se cambió en el bloque 'activo'
                    if 'autorizado' not in updated_fields:
                        updated_fields.append('autorizado')
                
                # Solo se puede AUTORIZAR si el camarero está activo.
                elif update_data['autorizado'] is True:
                    # La comprobación clave: el camarero debe estar activo.
                    if camarero.activo:
                        camarero.autorizado = True
                        if 'autorizado' not in updated_fields:
                            updated_fields.append('autorizado')
                    else:
                        # Este sí es un error real y justificado.
                        errors.append({"waiter_id": waiter_id, "error": "Acción no permitida: No se puede autorizar a un camarero que está inactivo."})

            # Actualizar permisos (reemplaza los existentes)
            if 'permisos' in update_data:
                permisos_a_asignar = update_data['permisos']
                # Aquí podrías añadir una validación contra PERMISOS_CHOICES si quieres
                camarero.permisos = json.dumps(permisos_a_asignar, ensure_ascii=False)
                updated_fields.append('permisos')

            # Borrar contraseña
            if update_data.get('clear_password') is True:
                if hasattr(camarero, 'pass_field'):
                    camarero.pass_field = ""
                    updated_fields.append('pass_field')

            if updated_fields:
                camarero.save()
                results.append({"waiter_id": waiter_id, "status": "actualizado", "fields_changed": updated_fields})
            else:
                results.append({"waiter_id": waiter_id, "status": "sin_cambios"})

        except Camareros.DoesNotExist:
            errors.append({"waiter_id": waiter_id, "error": "Camarero no encontrado."})
        except Exception as e:
            errors.append({"waiter_id": waiter_id, "error": f"Error inesperado: {str(e)}"})

    return {"results": results, "errors": errors}



@tool
@db_connection_handler
@sync_to_async 
def get_available_permissions() -> list:
    """Devuelve una lista de todos los permisos disponibles definidos en el sistema."""
    try:
      # Asegúrate que PERMISOS_CHOICES sea accesible aquí
      send_tool_message("Obteniendo lista de permisos disponibles")
      return PERMISOS_CHOICES
    except (NameError, ImportError):
      return ["Error: La lista de permisos (PERMISOS_CHOICES) no está disponible."]
    except Exception as e:
      return f"Error obteniendo permisos: {str(e)}"

@tool
@db_connection_handler
def create_waiter(nombre: str, apellidos: str) -> dict | str:
    """
    Crea un nuevo camarero (activo=1, autorizado=0).
    Args:
        nombre (str): Nombre del camarero (no vacío).
        apellidos (str): Apellidos del camarero (no vacío).
    Returns:
    Dict[str, Union[int, str, List[str], bool]]: Diccionario con los datos del camarero creado si es exitoso
                                            (id, nombre, apellidos, permisos, activo, autorizado).
                                            O un diccionario con una clave 'error' si falla.
                                            Ej: {'error': 'El nombre y los apellidos no pueden estar vacíos.'}
    """
    

    if not nombre or not apellidos:
        return "El nombre y los apellidos no pueden estar vacíos para crear un camarero."
    try:
        send_tool_message(f"Creando nuevo camarero: {nombre} {apellidos}")
        nuevo_camarero = Camareros.objects.create(
            nombre=nombre, apellidos=apellidos, permisos=json.dumps([]), activo=1, autorizado=1
        )
        
        return {
            "id": nuevo_camarero.id, "nombre": nuevo_camarero.nombre, "apellidos": nuevo_camarero.apellidos,
            "permisos": [], "activo": nuevo_camarero.activo, "autorizado": nuevo_camarero.autorizado
        }
    except Exception as e:
        return f"Error al crear el camarero: {str(e)}"


# --- Herramienta de Borrado (Peligrosa - Versión Selectiva) ---
@tool
@db_connection_handler
@sync_to_async 
def delete_inactive_waiters() -> str:
    """
    Intenta eliminar permanentemente de la base de datos a todos los camareros marcados como inactivos (activo=0)

    Returns:
        str: Un mensaje indicando cuántos camareros fueron eliminados y cuántos no pudieron ser borrados por tener ventas asociadas.
    """
    try:
        send_tool_message("Eliminando camareros inactivos")
        camareros_inactivos = Camareros.objects.filter(activo=0)
        total = camareros_inactivos.count()
        logger.info(f"Encontrados {total} camareros inactivos para procesar")
        eliminados = 0
        no_eliminados = 0

        for camarero in camareros_inactivos:
            logger.info(f"Procesando camarero ID {camarero.id}: {camarero.nombre} {camarero.apellidos}")
            if Pedidos.objects.filter(camarero_id=camarero.id).exists():
                logger.info(f"No se puede eliminar el camarero ID {camarero.id} porque tiene ventas asociadas.")
                no_eliminados += 1
                continue
            else:
                try:
                    camarero.delete()
                    eliminados += 1
                    logger.info(f"Camarero ID {camarero.id} eliminado correctamente.")
                except ProtectedError:
                    logger.error(f"Error al eliminar el camarero ID {camarero.id}: Tiene ventas asociadas.")
                    no_eliminados += 1
                except Exception as e:
                    logger.error(f"Error inesperado al eliminar el camarero ID {camarero.id}: {str(e)}")
                    no_eliminados += 1

        logger.info(f"Proceso completado. Eliminados: {eliminados}, No eliminados: {no_eliminados}")
        if total == 0:
            return "No se encontraron camareros inactivos para eliminar."
        if eliminados == 0 and no_eliminados == total:
            return "No se pudieron eliminar camareros inactivos porque todos tienen ventas asociadas."
        if eliminados > 0 and no_eliminados == 0:
            return f"Se eliminaron {eliminados} camareros inactivos sin problemas."
        
        return f"Se eliminaron {eliminados} camareros inactivos. {no_eliminados} camareros no se pudieron eliminar porque tienen ventas asociadas."
    except Exception as e:
        logger.error(f"Error crítico en delete_inactive_waiters: {str(e)}")
        return f"Ocurrió un error al intentar eliminar camareros inactivos: {str(e)}"


# --- Lista Final de Herramientas ---
tools = [
    find_waiters,             # Reemplaza 5 herramientas de búsqueda
    update_waiters,           # Reemplaza 5 herramientas de modificación
    create_waiter,            # Se mantiene por ser una acción única
    get_available_permissions,# Se mantiene como utilidad
    delete_inactive_waiters,  # Se mantiene por ser una operación peligrosa y de borrado masivo
]