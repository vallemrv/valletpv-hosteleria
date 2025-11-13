from django.core.exceptions import ObjectDoesNotExist, ValidationError
from gestion.models.dispositivos import Dispositivo
from typing import Dict,  Any, Optional
from langchain_core.tools import tool
from chatbot.decorators.db_connection_manager import db_connection_handler

@tool
@db_connection_handler
def listar_dispositivos(activo: Optional[bool] = None) -> Dict[str, Any]:
    """
    Lista todos los dispositivos del sistema.
    
    Args:
        activo: Filtrar por estado activo (True/False). Si es None, lista todos.
    
    Returns:
        Dict con la lista de dispositivos y metadatos
    """
    try:
        if activo is not None:
            dispositivos = Dispositivo.objects.filter(activo=activo)
        else:
            dispositivos = Dispositivo.objects.all()
        
        dispositivos_data = []
        for dispositivo in dispositivos:
            dispositivos_data.append({
                'id': dispositivo.id,
                'uid': dispositivo.uid,
                'descripcion': dispositivo.descripcion,
                'activo': dispositivo.activo,
                'created_at': dispositivo.created_at.isoformat() if dispositivo.created_at else None
            })
        
        return {
            'success': True,
            'data': dispositivos_data,
            'total': len(dispositivos_data),
            'message': f'Se encontraron {len(dispositivos_data)} dispositivos'
        }
    
    except Exception as e:
        return {
            'success': False,
            'error': str(e),
            'message': 'Error al listar los dispositivos'
        }


@tool
@db_connection_handler
def actualizar_dispositivo(uid: str, descripcion: Optional[str] = None, activo: Optional[bool] = None) -> Dict[str, Any]:
    """
    Actualiza la descripción o el estado de un dispositivo existente.
    Para activar un dispositivo usa activo=True, para desactivar usa activo=False.
    El UID no puede ser modificado.
    
    Args:
        uid: UID del dispositivo a actualizar (no modificable)
        descripcion: Nueva descripción para el dispositivo (opcional)
        activo: Nuevo estado (True para activar, False para desactivar) (opcional)
    
    Returns:
        Dict con el resultado de la operación
    """
    # Validar que se proporcione al menos un campo para actualizar
    if descripcion is None and activo is None:
        return {
            'success': False,
            'message': 'Error: Debes proporcionar al menos un campo (descripcion o activo) para actualizar'
        }
    
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
        
        # Solo actualizar campos modificables (descripcion y activo)
        if descripcion is not None:
            dispositivo.descripcion = descripcion.strip()
        
        if activo is not None:
            dispositivo.activo = activo
        
        dispositivo.save()
        
        return {
            'success': True,
            'data': {
                'id': dispositivo.id,
                'uid': dispositivo.uid,
                'descripcion': dispositivo.descripcion,
                'activo': dispositivo.activo,
                'created_at': dispositivo.created_at.isoformat()
            },
            'message': f'Dispositivo {uid} actualizado exitosamente'
        }
    
    except ObjectDoesNotExist:
        return {
            'success': False,
            'message': f'No se encontró el dispositivo con UID: {uid}'
        }
    except Exception as e:
        return {
            'success': False,
            'error': str(e),
            'message': f'Error al actualizar el dispositivo {uid}'
        }

@tool
@db_connection_handler
def eliminar_dispositivo(uid: str) -> Dict[str, Any]:
    """
    Elimina un dispositivo del sistema.
    
    Args:
        uid: UID del dispositivo a eliminar
    
    Returns:
        Dict con el resultado de la operación
    """
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
        dispositivo_info = {
            'uid': dispositivo.uid,
            'descripcion': dispositivo.descripcion
        }
        
        dispositivo.delete()
        
        return {
            'success': True,
            'data': dispositivo_info,
            'message': f'Dispositivo {uid} eliminado exitosamente'
        }
    
    except ObjectDoesNotExist:
        return {
            'success': False,
            'message': f'No se encontró el dispositivo con UID: {uid}'
        }
    except Exception as e:
        return {
            'success': False,
            'error': str(e),
            'message': f'Error al eliminar el dispositivo {uid}'
        }

@tool
@db_connection_handler
def obtener_dispositivo(uid: Optional[str] = None, alias: Optional[str] = None) -> Dict[str, Any]:
    """
    Obtiene la información de un dispositivo específico.
    Puede buscar por UID o por alias (descripción).
    
    Args:
        uid: UID del dispositivo a buscar (opcional)
        alias: Alias/descripción del dispositivo a buscar (opcional)
    
    Returns:
        Dict con la información del dispositivo
    """
    # Validar que se proporcione al menos un parámetro de búsqueda
    if not uid and not alias:
        return {
            'success': False,
            'message': 'Error: Debes proporcionar al menos un parámetro (uid o alias) para buscar el dispositivo'
        }
    
    try:
        dispositivo = None
        
        # Priorizar búsqueda por UID si está presente
        if uid:
            try:
                dispositivo = Dispositivo.objects.get(uid=uid)
            except ObjectDoesNotExist:
                # Si no se encuentra por UID, intentar por alias si está disponible
                if alias:
                    try:
                        dispositivo = Dispositivo.objects.get(descripcion=alias.strip())
                    except ObjectDoesNotExist:
                        pass
        elif alias:
            # Buscar solo por alias si no se proporcionó UID
            try:
                dispositivo = Dispositivo.objects.get(descripcion=alias.strip())
            except ObjectDoesNotExist:
                pass
        
        if not dispositivo:
            search_criteria = []
            if uid:
                search_criteria.append(f"UID: {uid}")
            if alias:
                search_criteria.append(f"alias: {alias}")
            
            return {
                'success': False,
                'message': f'No se encontró el dispositivo con {" o ".join(search_criteria)}'
            }
        
        return {
            'success': True,
            'data': {
                'id': dispositivo.id,
                'uid': dispositivo.uid,
                'descripcion': dispositivo.descripcion,
                'activo': dispositivo.activo,
                'created_at': dispositivo.created_at.isoformat()
            },
            'message': f'Dispositivo encontrado - UID: {dispositivo.uid}, Alias: {dispositivo.descripcion or "Sin alias"}'
        }
    
    except Exception as e:
        return {
            'success': False,
            'error': str(e),
            'message': f'Error al buscar el dispositivo'
        }

# Array de tools para exportar (versión consolidada y mejorada)
tools = [
    listar_dispositivos,
    actualizar_dispositivo,
    eliminar_dispositivo,
    obtener_dispositivo
]
