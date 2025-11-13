from langchain_core.tools import tool
from gestion.models.mesas import Mesas, Zonas, Mesaszona
from chatbot.utilidades.ws_sender import send_tool_message
from typing import List, Dict
from chatbot.decorators.db_connection_manager import db_connection_handler
import re


@tool
@db_connection_handler
def add_mesa(mesas_data: List[Dict]):
    """
    Añade una o múltiples mesas al sistema.

    Args:
        mesas_data: Lista de diccionarios con datos de mesas.
        Cada diccionario debe contener:
            - nombre (str): Nombre de la mesa
            - orden (int): Orden de la mesa

    Returns:
        list: Lista con la información de las mesas creadas.
    """
    if isinstance(mesas_data, dict):
        mesas_data = [mesas_data]
    
    send_tool_message(f"Añadiendo {len(mesas_data)} mesas")
    created_mesas = []
    for mesa in mesas_data:
        nueva_mesa = Mesas.objects.create(
            nombre=mesa['nombre'],
            orden=mesa['orden']
        )
        created_mesas.append(nueva_mesa.serialize())
    return created_mesas

@tool
@db_connection_handler
def update_mesa(mesas_data: List[Dict]):
    """
    Modifica una o múltiples mesas existentes.

    Args:
        mesas_data: Lista de diccionarios con datos de mesas.
        Cada diccionario debe contener:
            - id (int): ID de la mesa a modificar
            - nombre (str, opcional): Nuevo nombre de la mesa
            - orden (int, opcional): Nuevo orden de la mesa

    Returns:
        list: Lista con la información de las mesas actualizadas.
    """
    if isinstance(mesas_data, dict):
        mesas_data = [mesas_data]
    
    send_tool_message(f"Actualizando {len(mesas_data)} mesas")
    updated_mesas = []
    for mesa in mesas_data:
        mesa_obj = Mesas.objects.get(pk=mesa['id'])
        if 'nombre' in mesa:
            mesa_obj.nombre = mesa['nombre']
        if 'orden' in mesa:
            mesa_obj.orden = mesa['orden']
        mesa_obj.save()
        updated_mesas.append(mesa_obj.serialize())
    return updated_mesas

@tool
@db_connection_handler
def delete_mesa(mesa_ids: List[int]):
    """
    Elimina una o múltiples mesas del sistema.
    Necestita confirmación del usuario. Asegurate de que quiere borrar la mesa y no
    cerrar la mesa abierta.

    Args:
        mesa_ids: Lista de IDs de mesas a eliminar.

    Returns:
        dict: Mensaje de confirmación con el número de mesas eliminadas.
    """
    if isinstance(mesa_ids, int):
        mesa_ids = [mesa_ids]
    
    send_tool_message(f"Eliminando {len(mesa_ids)} mesas")
    deleted_count = Mesas.objects.filter(pk__in=mesa_ids).delete()[0]
    return {"message": f"Se eliminaron {deleted_count} mesas."}

@tool
@db_connection_handler
def add_zona(zonas_data: List[Dict]):
    """
    Añade una o múltiples zonas al sistema.

    Args:
        zonas_data: Lista de diccionarios con datos de zonas.
        Cada diccionario debe contener:
            - nombre (str): Nombre de la zona
            - tarifa (int): Tarifa asociada a la zona
            - rgb (str): Color RGB de la zona

    Returns:
        list: Lista con la información de las zonas creadas.
    """
    if isinstance(zonas_data, dict):
        zonas_data = [zonas_data]
    
    send_tool_message(f"Añadiendo {len(zonas_data)} zonas")
    created_zonas = []
    for zona in zonas_data:
        nueva_zona = Zonas.objects.create(
            nombre=zona['nombre'],
            tarifa=zona['tarifa'],
            rgb=zona['rgb']
        )
        created_zonas.append({
            "ID": nueva_zona.id,
            "Nombre": nueva_zona.nombre,
            "Tarifa": nueva_zona.tarifa,
            "RGB": nueva_zona.rgb
        })
    return created_zonas

@tool
@db_connection_handler
def update_zona(zonas_data: List[Dict]):
    """
    Modifica una o múltiples zonas existentes.

    Args:
        zonas_data: Lista de diccionarios con datos de zonas.
        Cada diccionario debe contener:
            - id (int): ID de la zona a modificar
            - nombre (str, opcional): Nuevo nombre de la zona
            - tarifa (int, opcional): Nueva tarifa de la zona
            - rgb (str, opcional): Nuevo color RGB de la zona

    Returns:
        list: Lista con la información de las zonas actualizadas.
    """
    if isinstance(zonas_data, dict):
        zonas_data = [zonas_data]
    
    send_tool_message(f"Actualizando {len(zonas_data)} zonas")
    updated_zonas = []
    for zona in zonas_data:
        zona_obj = Zonas.objects.get(pk=zona['id'])
        if 'nombre' in zona:
            zona_obj.nombre = zona['nombre']
        if 'tarifa' in zona:
            zona_obj.tarifa = zona['tarifa']
        if 'rgb' in zona:
            zona_obj.rgb = zona['rgb']
        zona_obj.save()
        updated_zonas.append({
            "ID": zona_obj.id,
            "Nombre": zona_obj.nombre,
            "Tarifa": zona_obj.tarifa,
            "RGB": zona_obj.rgb
        })
    return updated_zonas

@tool
@db_connection_handler
def delete_zona(zona_id: int):
    """
    Elimina una zona del sistema.

    Args:
        zona_id (int): ID de la zona a eliminar.

    Returns:
        str: Mensaje de confirmación.
    """
    Zonas.objects.filter(pk=zona_id).delete()
    return f"Zona con ID {zona_id} eliminada."

@tool
@db_connection_handler
def associate_mesa_to_zona(associations: List[Dict], delete_previous: bool = True):
    """
    Asocia una o múltiples mesas a zonas, con opción de eliminar las asociaciones anteriores.

    Args:
        associations: Lista de diccionarios con datos de asociaciones.
        Cada diccionario debe contener:
            - mesa_id (int): ID de la mesa a asociar
            - zona_id (int): ID de la zona a la que se asociará la mesa
        delete_previous (bool): Si True, elimina las asociaciones anteriores. Por defecto True.

    Returns:
        dict: Diccionario con el resultado de la operación que contiene:
            - success (bool): True si la operación fue exitosa
            - message (str): Mensaje descriptivo del resultado
            - updated_mesas (list): Lista con la información de las mesas actualizadas
            - errors (list): Lista de errores encontrados, si los hay
    """
    if isinstance(associations, dict):
        associations = [associations]
    
    send_tool_message(f"Asociando {len(associations)} mesas a zonas")
    updated_mesas = []
    errors = []
    success_count = 0
    
    for i, assoc in enumerate(associations):
        try:
            # Validar que los campos requeridos estén presentes
            if 'mesa_id' not in assoc:
                errors.append(f"Asociación {i+1}: Falta el campo 'mesa_id'")
                continue
            if 'zona_id' not in assoc:
                errors.append(f"Asociación {i+1}: Falta el campo 'zona_id'")
                continue
            
            mesa_id = assoc['mesa_id']
            zona_id = assoc['zona_id']
            
            # Validar que la mesa existe
            try:
                mesa = Mesas.objects.get(pk=mesa_id)
            except Mesas.DoesNotExist:
                errors.append(f"Asociación {i+1}: No se encontró la mesa con ID {mesa_id}")
                continue
            
            # Validar que la zona existe
            try:
                zona = Zonas.objects.get(pk=zona_id)
            except Zonas.DoesNotExist:
                errors.append(f"Asociación {i+1}: No se encontró la zona con ID {zona_id}")
                continue
            
            # Verificar si ya existe una asociación
            existing_association = Mesaszona.objects.filter(mesa_id=mesa_id).first()
            if existing_association and not delete_previous:
                errors.append(f"Asociación {i+1}: La mesa '{mesa.nombre}' (ID: {mesa_id}) ya está asociada a la zona '{existing_association.zona.nombre}' (ID: {existing_association.zona.id}). Use delete_previous=True para reemplazar la asociación.")
                continue
            
            # Eliminar asociaciones anteriores solo si delete_previous es True
            if delete_previous:
                deleted_count = Mesaszona.objects.filter(mesa_id=mesa_id).delete()[0]
                if deleted_count > 0:
                    send_tool_message(f"Eliminada asociación anterior de mesa '{mesa.nombre}'")
            
            # Crear nueva asociación
            Mesaszona.objects.create(mesa=mesa, zona=zona)
            updated_mesas.append(mesa.serialize())
            success_count += 1
            send_tool_message(f"Mesa '{mesa.nombre}' asociada exitosamente a zona '{zona.nombre}'")
            
        except Exception as e:
            errors.append(f"Asociación {i+1}: Error inesperado - {str(e)}")
    
    # Preparar respuesta
    total_associations = len(associations)
    is_success = success_count > 0 and len(errors) == 0
    
    if is_success:
        message = f"Todas las {success_count} asociaciones se crearon exitosamente."
    elif success_count > 0:
        message = f"Se completaron {success_count} de {total_associations} asociaciones. {len(errors)} fallaron."
    else:
        message = f"No se pudo crear ninguna asociación. Todas las {total_associations} operaciones fallaron."
    
    return {
        "success": is_success,
        "message": message,
        "updated_mesas": updated_mesas,
        "errors": errors,
        "statistics": {
            "total_requested": total_associations,
            "successful": success_count,
            "failed": len(errors)
        }
    }

@tool
@db_connection_handler
def list_zonas():
    """
    Lista todas las zonas disponibles en el sistema.
    Util para cuando no se encuetra el nombre de la zona.
    Returns:
        list: Lista de diccionarios con la información de cada zona.
        Cada diccionario contiene:
            - ID: Identificador único de la zona
            - Nombre: Nombre de la zona
            - Tarifa: Tarifa asociada a la zona
            - RGB: Color RGB de la zona
    """
    send_tool_message("Listando todas las zonas")
    zonas = Zonas.objects.all()
    return [{"ID": zona.id, "Nombre": zona.nombre, "Tarifa": zona.tarifa, "RGB": zona.rgb} for zona in zonas]

@tool
@db_connection_handler
def list_mesas():
    """
    Lista todas las mesas disponibles en el sistema.
    Util para cuando no se encuetra el nombre de la mesa.
    Returns:
        list: Lista de diccionarios con la información de cada mesa.
        La información incluye todos los detalles serializados de la mesa.
    """
    send_tool_message("Listando todas las mesas")
    mesas = Mesas.objects.all()
    return [mesa.serialize() for mesa in mesas]

@tool
@db_connection_handler
def list_mesas_by_zona(zona_id: int):
    """
    Lista todas las mesas que pertenecen a una zona específica.

    Args:
        zona_id (int): ID de la zona de la cual se quieren listar las mesas.

    Returns:
        list: Lista de diccionarios con la información de cada mesa en la zona.
        La información incluye todos los detalles serializados de las mesas.
    """
    send_tool_message(f"Listando mesas de la zona ID: {zona_id}")
    mesas_zona = Mesaszona.objects.filter(zona_id=zona_id)
    return [mesa_zona.mesa.serialize() for mesa_zona in mesas_zona]

@tool
@db_connection_handler
def get_zona_id(nombre: str):
    """
    Obtiene las coincidencias de zona usando una expresión regular en su nombre.
    Utili para buscar el id de la zona.
    NOTA: Cuando de vuelva mas de una zona, se creativo y develve la que se te ha pedido solo.
          esta herramienta busca que el nombre contenga el str proporcionado.
    Args:
        nombre (str): Expresión regular para buscar zonas.

    Returns:
        list: Lista de diccionarios con el ID y nombre de cada zona que coincida.
    """
    zonas = Zonas.objects.filter(nombre__icontains=nombre)  # Se utiliza icontains para una búsqueda más flexible
    result = [{"ID": zona.id, "Nombre": zona.nombre} for zona in zonas]
    return result if result else list_zonas.invoke({})


def _normalizar_nombre_mesa(nombre: str) -> str:
    """Función de ayuda para normalizar consistentemente los nombres de las mesas."""
    
    palabras_numericas = {
        'uno': '1', 'dos': '2', 'tres': '3', 'cuatro': '4', 'cinco': '5',
        'seis': '6', 'siete': '7', 'ocho': '8', 'nueve': '9', 'diez': '10'
    }
    
    nombre_limpio = nombre.lower().strip()
    
    # --- INICIO DE LA CORRECCIÓN FONÉTICA ---
    # Reemplazamos todas las 'v' por 'b' para unificar.
    nombre_limpio = nombre_limpio.replace('v', 'b')
    # --- FIN DE LA CORRECCIÓN FONÉTICA ---
    
    # Convertir palabras numéricas a dígitos
    palabras = nombre_limpio.split()
    palabras_convertidas = [palabras_numericas.get(p, p) for p in palabras]
    nombre_limpio = ''.join(palabras_convertidas) # Unir sin espacios
    
    # Definir prefijos sin espacio
    prefijos_a_quitar = ['mesa', 'barra', 'terraza', 't-']
    
    # Quitar los prefijos de la cadena ya sin espacios
    for prefijo in prefijos_a_quitar:
        if nombre_limpio.startswith(prefijo):
            nombre_limpio = nombre_limpio.replace(prefijo, '', 1)
            break
            
    return nombre_limpio.strip()


@tool
@db_connection_handler
def get_mesa_id(nombre: str) -> List[Dict]:
    """
    Encuentra el ID de una mesa de forma precisa, normalizando la entrada y los
    nombres de las mesas para asegurar una coincidencia robusta.
    Prioriza las coincidencias exactas sobre las normalizadas.
    """
    # 1. Normalizar el término de búsqueda usando la función de ayuda
    termino_busqueda_normalizado = _normalizar_nombre_mesa(nombre)
    
    # 2. Obtener todas las mesas y buscar la mejor coincidencia en Python
    todas_las_mesas = Mesas.objects.prefetch_related('mesaszona_set__zona').all()
    
    coincidencias_exactas = []
    coincidencias_normalizadas = []

    for mesa in todas_las_mesas:
        nombre_mesa_db_raw = mesa.nombre.lower()
        
        # Prioridad 1: Coincidencia exacta del nombre original
        if nombre_mesa_db_raw == nombre.lower().strip():
            coincidencias_exactas.append(mesa)
            continue

        # Prioridad 2: Coincidencia normalizada
        nombre_mesa_db_normalizado = _normalizar_nombre_mesa(mesa.nombre)
        if nombre_mesa_db_normalizado == termino_busqueda_normalizado:
            coincidencias_normalizadas.append(mesa)

    # 3. Devolver el mejor resultado encontrado
    mesas_a_devolver = coincidencias_exactas if coincidencias_exactas else coincidencias_normalizadas
    
    if not mesas_a_devolver:
        return []

    # Formatear el resultado final
    result = []
    for mesa in mesas_a_devolver:
        tarifa = 1
        mesaszona = mesa.mesaszona_set.first()
        if mesaszona and mesaszona.zona:
            tarifa = mesaszona.zona.tarifa
        
        result.append({
            "ID": mesa.id,
            "Nombre": mesa.nombre,
            "Tarifa": int(tarifa)
        })
        
    return result


@tool
@db_connection_handler
def reorder_mesas(mesa_orders: List[Dict]):
    """
    Cambia el orden de múltiples mesas actualizando el campo 'orden' en la tabla Mesaszona.

    Args:
        mesa_orders: Lista de diccionarios con datos de orden.
        Cada diccionario debe contener:
            - id_mesa (int): ID de la mesa cuyo orden se va a cambiar
            - orden (int): Nuevo valor de orden para la mesa en Mesaszona

    Returns:
        list: Lista con la información de las asociaciones mesa-zona actualizadas.
    """
    send_tool_message(f"Reordenando {len(mesa_orders)} mesas en Mesaszona")
    updated_mesaszona = []
    
    for mesa_order in mesa_orders:
        id_mesa = mesa_order['id_mesa']
        nuevo_orden = mesa_order['orden']
        
        # Buscar y actualizar el orden en Mesaszona
        try:
            mesa = Mesas.objects.get(id=id_mesa)
            mesa.orden = nuevo_orden
            mesa.save()
            updated_mesaszona.append(
                mesa.serialize()
            )
        except Mesaszona.DoesNotExist:
            send_tool_message(f"No se encontró asociación para mesa ID: {id_mesa}")
    
    return updated_mesaszona

# Lista de herramientas
tools = [
        add_mesa, 
        update_mesa, 
        delete_mesa, 
        add_zona, 
        update_zona, 
        delete_zona, 
        associate_mesa_to_zona,
        reorder_mesas,
        list_zonas,
        list_mesas,
        list_mesas_by_zona,
        get_zona_id,
        get_mesa_id
    ]
