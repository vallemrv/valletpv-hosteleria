from typing import List, Dict, Union, Optional
from django.db.models import Q, Value
from langchain_core.tools import tool

from django.db.models.functions import Concat
from chatbot.utilidades.ws_sender import send_tool_message
from gestion.models.teclados import Teclas, Sugerencias, Teclascom, TeclasAgotadas
from gestion.models.familias import Familias
from gestion.tools.config_logs import log_debug_chatbot as logger
from chatbot.decorators.db_connection_manager import db_connection_handler
from comunicacion.tools import comunicar_cambios_devices
from datetime import date

# Importar herramientas de embedding para mantener ChromaDB actualizado
from ..comandas.embeding_teclas_y_mesas import (
    actualizar_o_crear_registros_chromadb,
    eliminar_registros_chromadb
)


def _format_success(data: Dict = None, message: str = None) -> Dict:
    """Formato estándar para respuestas exitosas."""
    result = {"status": "success"}
    if message:
        result["message"] = message
    if data:
        result.update(data)
    return result


def _format_error(message: str) -> Dict:
    """Formato estándar para respuestas de error."""
    return {"status": "error", "message": message}


def _tecla_to_dict(tecla) -> Dict[str, Union[int, str, float, List]]:
    """
    Convierte una instancia de Teclas en un diccionario con estructura consistente.
    Para teclas de tipo CM incluye las subteclas anidadas.
    """
    try:
        data = tecla.serialize() if hasattr(tecla, 'serialize') else {
            "id": tecla.id,
            "nombre": tecla.nombre,
            "p1": float(tecla.p1),
            "p2": float(tecla.p2),
            "familia_id": tecla.familia_id,
            "tipo": tecla.tipo,
            "tag": tecla.tag,
            "descripcion_r": tecla.descripcion_r,
            "descripcion_t": tecla.descripcion_t
        }
        
        # Si es tecla compuesta (CM), incluir subteclas
        if tecla.tipo == "CM":
            subteclas = tecla.subteclas.all()
            data["subteclas"] = [_tecla_to_dict(subtecla) for subtecla in subteclas]
        
        return data
    except Exception as e:
        logger.error(f"Error serializando tecla {tecla.id}: {str(e)}")
        return {}
    

# --- FUNCIÓN DE FILTRADO DE TEXTO MOVIDA AQUÍ PARA EVITAR IMPORTACIÓN CIRCULAR ---
def _construir_filtro_texto(query, texto_busqueda: str):
    """
    Construye y aplica un filtro de búsqueda por texto a un queryset.

    Esta función está optimizada para palabras compuestas (ej: "tercio alhambra").
    Requiere que TODAS las palabras clave de la búsqueda existan en el registro,
    pero permite que cada palabra esté en un campo diferente (nombre, tag, etc.).
    Esto asegura que una tecla con nombre="Tercio Especial" y tag="cerveza alhambra"
    sea encontrada al buscar "tercio alhambra".
    """
    send_tool_message(f"Iniciando búsqueda por texto: '{texto_busqueda}'")
    
    # Palabras comunes a ignorar para no distorsionar la búsqueda
    stop_words = {'de', 'la', 'el', 'los', 'las', 'un', 'una', 'con', 'a', 'y', 'o', 'para', '?', '¡', '!', ',', '.'}

    # Limpiamos y separamos el texto en palabras clave
    palabras_clave = [word for word in texto_busqueda.lower().split() if word not in stop_words and len(word) >= 2]

    if not palabras_clave:
        return query # No hay palabras válidas para buscar

    send_tool_message(f"Buscando por palabras clave: {', '.join(palabras_clave)}")
    
    # Se crea un filtro base que requiere que TODAS las palabras clave estén presentes (lógica AND)
    filtros_and = Q()
    
    for palabra in palabras_clave:
        # Condición para buscar en la descripción dinámica solo si la real está vacía
        cond_desc_vacia = Q(descripcion_r__isnull=True) | Q(descripcion_r='')
        
        # Cada palabra clave puede estar en CUALQUIERA de estos campos (lógica OR)
        filtro_por_palabra = (
            Q(nombre__icontains=palabra) |
            Q(tag__icontains=palabra) |
            Q(descripcion_r__icontains=palabra) |
            (cond_desc_vacia & Q(descripcion_dinamica__icontains=palabra))
        )
        
        # Se añade la condición para esta palabra al filtro general con un AND
        filtros_and &= filtro_por_palabra
    
    return query.filter(filtros_and)

      


# --- FUNCIÓN PRINCIPAL (AHORA MÁS LIMPIA) ---

@tool
@db_connection_handler
def buscar_teclas(
    ids: Optional[List[int]] = None,
    texto_busqueda: Optional[str] = None,
    familia_id: Optional[int] = None,
    seccion_id: Optional[int] = None,
    seccion_com_id: Optional[int] = None,
    sin_seccion: bool = False,
    precio_minimo: Optional[float] = None,
    precio_maximo: Optional[float] = None,
    tipo_precio: str = "p1",
    tipo_tecla: Optional[str] = None,
) -> List[Dict[str, Union[int, str, float, List]]]:
    """
    Busca teclas con filtros combinables. Si se busca por texto, puede
    encontrar teclas por su `descripcion_r` o, si esta está vacía, por una
    descripción dinámica formada por 'nombre del padre + nombre de la tecla'.
    La búsqueda de texto está optimizada para encontrar palabras compuestas
    a través de diferentes campos.
    """
    try:
        send_tool_message("Buscando teclas con los filtros especificados...")
        
        query = Teclas.objects.select_related('parent_tecla').annotate(
            descripcion_dinamica=Concat(
                'parent_tecla__nombre', Value(' '), 'nombre'
            )
        )
        
        # --- LÓGICA DE BÚSQUEDA POR TEXTO (AHORA LLAMA A LA FUNCIÓN HELPER) ---
        if texto_busqueda and len(texto_busqueda) >= 3:
            query = _construir_filtro_texto(query, texto_busqueda)

        # --- RESTO DE FILTROS ---
        if ids:
            query = query.filter(id__in=ids)

        if familia_id:
            try:
                Familias.objects.get(id=familia_id)
                query = query.filter(familia_id=familia_id)
            except Familias.DoesNotExist:
                return [_format_error(f"Familia con ID {familia_id} no encontrada")]
                
        if seccion_id:
            query = query.filter(teclasecciones__seccion_id=seccion_id)
            
        if seccion_com_id:
            query = query.filter(teclascomanda__seccion_id=seccion_com_id).order_by("-teclascomanda__orden")
            
        if sin_seccion:
            query = query.exclude(teclasecciones__isnull=False)
            
        if precio_minimo is not None:
            campo_precio = f"{tipo_precio}__gte"
            query = query.filter(**{campo_precio: precio_minimo})
            
        if precio_maximo is not None:
            campo_precio = f"{tipo_precio}__lte"
            query = query.filter(**{campo_precio: precio_maximo})
            
        if tipo_tecla:
            query = query.filter(tipo=tipo_tecla.upper())
            
        # --- ORDENACIÓN Y SERIALIZACIÓN ---
        if seccion_com_id:
            query = query.distinct()
        else:
            query = query.order_by('-orden', 'nombre').distinct()
        
        if not query.exists():
            send_tool_message("No se encontraron teclas que coincidan con todos los filtros.")
            return []

        results = []
        for tecla in query:
            try:
                data = tecla.serialize()
                results.append(data)
            except Exception as e:
                logger.error(f"Error serializando tecla {tecla.id}: {str(e)}")
                continue
        
        send_tool_message(f"Encontradas {len(results)} teclas")
        logger.debug(f"Búsqueda completada: {len(results)} teclas encontradas")
        return results
        
    except Exception as e:
        error_msg = f"Error buscando teclas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]
    


@tool
@db_connection_handler
def gestionar_teclas(
    operacion: str,
    teclas_data: List[Dict] = None
) -> List[Dict[str, Union[str, int]]]:
    """
    Gestiona teclas de forma unificada: crear, modificar o eliminar múltiples teclas.
    
    Args:
        operacion: Tipo de operación ("crear", "modificar", "eliminar")
        teclas_data: Lista de datos de teclas según la operación:
            - Para "crear": [{"nombre": "...", "p1": 0.0, "p2": 0.0, "familia": 1, "tipo": "SP", "tag": "...", "descripcion_r": "...", "descripcion_t": "..."}]
            - Para "modificar": [{"tecla_id": 1, "nombre": "...", "p1": 10.0, "tag": "nuevo_tag", ...}]
            - Para "eliminar": [{"tecla_id": 1}, {"tecla_id": 2}, ...]
            
        Campos disponibles para crear/modificar:
            - nombre: Nombre de la tecla (requerido para crear)
            - p1: Precio tarifa 1 (float, por defecto 0.0)
            - p2: Precio tarifa 2 (float, por defecto 0.0)
            - familia: ID de la familia (int, requerido para crear)
            - tipo: Tipo de tecla ("SP" o "CM", por defecto "SP")
            - tag: Etiqueta/tag de la tecla (string, por defecto "")
            - descripcion_r: Descripción para receptor (string, por defecto "")
            - descripcion_t: Descripción para ticket (string, por defecto "")
    
    Returns:
        Lista de resultados para cada operación con formato estándar
    """
    if not teclas_data:
        return [_format_error("No se proporcionaron datos de teclas")]
        
    if operacion not in ["crear", "modificar", "eliminar"]:
        return [_format_error("Operación debe ser 'crear', 'modificar' o 'eliminar'")]
    
    results = []
    
    for data in teclas_data:
        try:
            if operacion == "crear":
                results.append(_crear_tecla(data))
            elif operacion == "modificar":
                results.append(_modificar_tecla(data))
            elif operacion == "eliminar":
                results.append(_eliminar_tecla(data))
                
        except Exception as e:
            logger.error(f"Error en operación {operacion}: {str(e)}", exc_info=True)
            results.append(_format_error(f"Error en operación: {str(e)}"))
    
    return results


def _crear_tecla(data: Dict) -> Dict:
    """Función auxiliar para crear una tecla."""
    nombre = data.get("nombre")
    familia_id = data.get("familia")
    
    if not nombre or familia_id is None:
        return _format_error(f"Datos incompletos: nombre={nombre}, familia={familia_id}")
    
    try:
        tecla = Teclas(
            nombre=nombre,
            p1=data.get("p1", 0.0),
            p2=data.get("p2", 0.0),
            familia_id=familia_id,
            tipo=data.get("tipo", "SP"),
            tag=data.get("tag", ""),
            descripcion_r=data.get("descripcion_r", ""),
            descripcion_t=data.get("descripcion_t", "")
        )
        tecla.save()
        
        # Actualizar ChromaDB si es una tecla de producto (tipo SP)
        if tecla.tipo == "SP":
            try:
                actualizar_o_crear_registros_chromadb.func([{
                    "coleccion": "teclas",
                    "id_elemento": tecla.id
                }])
                logger.info(f"ChromaDB actualizado para nueva tecla ID {tecla.id}")
            except Exception as e:
                logger.warning(f"Error actualizando ChromaDB para tecla ID {tecla.id}: {e}")
        
        send_tool_message(f"Tecla '{nombre}' creada exitosamente")
        return _format_success({"id": tecla.id, "nombre": tecla.nombre}, f"Tecla '{nombre}' creada")
        
    except Exception as e:
        error_msg = f"Error creando tecla '{nombre}': {str(e)}"
        logger.error(error_msg)
        return _format_error(error_msg)


def _modificar_tecla(data: Dict) -> Dict:
    """Función auxiliar para modificar una tecla."""
    tecla_id = data.get("tecla_id")
    if not tecla_id:
        return _format_error("ID de tecla no proporcionado")
    
    try:
        tecla = Teclas.objects.get(id=tecla_id)
        nombre_original = tecla.nombre
        
        # Aplicar modificaciones
        for campo in ["nombre", "p1", "p2", "familia", "tipo", "tag", "descripcion_r", "descripcion_t"]:
            if campo in data and data[campo] is not None:
                if campo == "familia":
                    setattr(tecla, "familia_id", data[campo])
                else:
                    setattr(tecla, campo, data[campo])
        
        tecla.save()
        
        # Actualizar ChromaDB si es una tecla de producto (tipo SP)
        if tecla.tipo == "SP":
            try:
                actualizar_o_crear_registros_chromadb.func([{
                    "coleccion": "teclas",
                    "id_elemento": tecla.id
                }])
                logger.info(f"ChromaDB actualizado para tecla modificada ID {tecla.id}")
            except Exception as e:
                logger.warning(f"Error actualizando ChromaDB para tecla ID {tecla.id}: {e}")
        
        send_tool_message(f"Tecla '{nombre_original}' modificada exitosamente")
        return _format_success({"id": tecla.id, "nombre": tecla.nombre}, f"Tecla modificada")
        
    except Teclas.DoesNotExist:
        return _format_error(f"Tecla con ID {tecla_id} no encontrada")
    except Exception as e:
        error_msg = f"Error modificando tecla ID {tecla_id}: {str(e)}"
        logger.error(error_msg)
        return _format_error(error_msg)


def _eliminar_tecla(data: Dict) -> Dict:
    """Función auxiliar para eliminar una tecla."""
    tecla_id = data.get("tecla_id")
    if not tecla_id:
        return _format_error("ID de tecla no proporcionado")
    
    try:
        tecla = Teclas.objects.get(id=tecla_id)
        nombre = tecla.nombre
        es_producto = tecla.tipo == "SP"
        
        # Eliminar de ChromaDB antes de eliminar de la base de datos
        if es_producto:
            try:
                eliminar_registros_chromadb.func([{
                    "coleccion": "teclas",
                    "id_elemento": tecla_id
                }])
                logger.info(f"Tecla ID {tecla_id} eliminada de ChromaDB")
            except Exception as e:
                logger.warning(f"Error eliminando de ChromaDB tecla ID {tecla_id}: {e}")
        
        tecla.delete()
        send_tool_message(f"Tecla '{nombre}' eliminada exitosamente")
        return _format_success(message=f"Tecla '{nombre}' eliminada correctamente")
        
    except Teclas.DoesNotExist:
        return _format_error(f"Tecla con ID {tecla_id} no encontrada")
    except Exception as e:
        error_msg = f"Error eliminando tecla ID {tecla_id}: {str(e)}"
        logger.error(error_msg)
        return _format_error(error_msg)


@tool
@db_connection_handler
def gestionar_relaciones_teclas(
    operacion: str,
    tecla_padre_id: Optional[int] = None,
    tecla_hija_id: Optional[int] = None
) -> Dict[str, Union[str, int]]:
    """
    Gestiona relaciones padre-hija entre teclas.
    
    Args:
        operacion: "asociar" para crear relación, "quitar" para eliminar relación
        tecla_padre_id: ID de la tecla padre (requerido para "asociar")
        tecla_hija_id: ID de la tecla hija (requerido para ambas operaciones)
    
    Returns:
        Resultado de la operación con formato estándar
    """
    if operacion not in ["asociar", "quitar"]:
        return _format_error("Operación debe ser 'asociar' o 'quitar'")
    
    if not tecla_hija_id:
        return _format_error("ID de tecla hija es requerido")
    
    try:
        if operacion == "asociar":
            return _asociar_teclas(tecla_padre_id, tecla_hija_id)
        else:  # quitar
            return _quitar_relacion_tecla(tecla_hija_id)
            
    except Exception as e:
        error_msg = f"Error gestionando relación de teclas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return _format_error(error_msg)


def _asociar_teclas(tecla_padre_id: int, tecla_hija_id: int) -> Dict:
    """Función auxiliar para asociar teclas."""
    if not tecla_padre_id:
        return _format_error("ID de tecla padre es requerido para asociar")
    
    if tecla_padre_id == tecla_hija_id:
        return _format_error("Una tecla no puede ser padre de sí misma")
    
    try:
        tecla_padre = Teclas.objects.get(id=tecla_padre_id)
        tecla_hija = Teclas.objects.get(id=tecla_hija_id)
        
        # Verificar relación circular
        if tecla_padre.parent_tecla_id == tecla_hija_id:
            return _format_error("No se puede crear relación circular")
        
        # Cambiar tipo de padre a CM si es necesario
        if tecla_padre.tipo != "CM":
            tecla_padre.tipo = "CM"
            tecla_padre.save()
            send_tool_message(f"Tecla padre '{tecla_padre.nombre}' cambiada a tipo 'CM'")
        
        # Establecer relación
        tecla_hija.parent_tecla = tecla_padre
        tecla_hija.save()
        
        message = f"Tecla '{tecla_hija.nombre}' asociada como subtecla de '{tecla_padre.nombre}'"
        send_tool_message(message)
        
        return _format_success({
            "tecla_padre_id": tecla_padre.id,
            "tecla_padre_nombre": tecla_padre.nombre,
            "tecla_hija_id": tecla_hija.id,
            "tecla_hija_nombre": tecla_hija.nombre
        }, message)
        
    except Teclas.DoesNotExist:
        return _format_error("Una de las teclas especificadas no existe")


def _quitar_relacion_tecla(tecla_hija_id: int) -> Dict:
    """Función auxiliar para quitar relación padre-hija."""
    try:
        tecla_hija = Teclas.objects.get(id=tecla_hija_id)
        
        if not tecla_hija.parent_tecla:
            return _format_error(f"La tecla '{tecla_hija.nombre}' no tiene tecla padre")
        
        tecla_padre = tecla_hija.parent_tecla
        nombre_padre = tecla_padre.nombre
        
        # Quitar relación
        tecla_hija.parent_tecla = None
        tecla_hija.save()
        
        # Cambiar tipo de padre si no tiene más hijas
        if tecla_padre.subteclas.count() == 0 and tecla_padre.tipo == "CM":
            tecla_padre.tipo = "SP"
            tecla_padre.save()
            send_tool_message(f"Tecla padre '{nombre_padre}' cambiada a tipo 'SP'")
        
        message = f"Relación eliminada: '{tecla_hija.nombre}' ya no es subtecla de '{nombre_padre}'"
        send_tool_message(message)
        
        return _format_success({
            "tecla_hija_id": tecla_hija.id,
            "tecla_hija_nombre": tecla_hija.nombre,
            "tecla_padre_nombre": nombre_padre
        }, message)
        
    except Teclas.DoesNotExist:
        return _format_error(f"Tecla con ID {tecla_hija_id} no encontrada")


@tool
@db_connection_handler
def gestionar_sugerencias(
    operacion: str,
    sugerencias_data: List[Dict] = None,
    tecla_id: Optional[int] = None
) -> List[Dict[str, Union[str, int]]]:
    """
    Recuerda pedir confirmacion antes de <<BORRAR>> una sugerecia
    Gestiona sugerencias de forma unificada: crear, modificar, eliminar o listar.
    
    Args:
        operacion: "crear", "modificar", "eliminar", "listar" o "eliminar_por_tecla"
        sugerencias_data: Datos según operación:
            - crear: [{"tecla_id": 1, "sugerencia": "texto", "incremento": 0.5}]
            - modificar: [{"sugerencia_id": 1, "nueva_sugerencia": "texto", "incremento": 1.0}]
            - eliminar: [{"sugerencia_id": 1}, {"sugerencia_id": 2}]
            - listar: [{"sugerencia_id": 1}] o None para todas
        tecla_id: Para operaciones específicas de una tecla
    
    Returns:
        Lista de resultados con formato estándar
    """
    try:
        if operacion == "listar":
            return _listar_sugerencias(sugerencias_data, tecla_id)
        elif operacion == "eliminar_por_tecla":
            if not tecla_id:
                return [_format_error("tecla_id requerido para eliminar_por_tecla")]
            return [_eliminar_sugerencias_por_tecla(tecla_id)]
        
        if not sugerencias_data:
            return [_format_error("sugerencias_data requerido para esta operación")]
        
        results = []
        for data in sugerencias_data:
            if operacion == "crear":
                results.append(_crear_sugerencia(data))
            elif operacion == "modificar":
                results.append(_modificar_sugerencia(data))
            elif operacion == "eliminar":
                results.append(_eliminar_sugerencia(data))
        
        return results
        
    except Exception as e:
        error_msg = f"Error gestionando sugerencias: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]


def _crear_sugerencia(data: Dict) -> Dict:
    """Función auxiliar para crear sugerencia."""
    tecla_id = data.get("tecla_id")
    texto = data.get("sugerencia")
    
    if not tecla_id or not texto:
        return _format_error("tecla_id y sugerencia son requeridos")
    
    try:
        tecla = Teclas.objects.get(id=tecla_id)
        incremento = data.get("incremento", 0.0)
        
        sugerencia = Sugerencias(
            tecla=tecla,
            sugerencia=texto,
            incremento=incremento
        )
        sugerencia.save()
        
        return _format_success(
            {"id": sugerencia.id, "sugerencia": sugerencia.sugerencia},
            f"Sugerencia '{texto}' creada para tecla '{tecla.nombre}'"
        )
    except Teclas.DoesNotExist:
        return _format_error(f"Tecla con ID {tecla_id} no encontrada")
    except Exception as e:
        return _format_error(f"Error creando sugerencia: {str(e)}")


def _modificar_sugerencia(data: Dict) -> Dict:
    """Función auxiliar para modificar sugerencia."""
    sugerencia_id = data.get("sugerencia_id")
    if not sugerencia_id:
        return _format_error("sugerencia_id requerido")
    
    try:
        sugerencia = Sugerencias.objects.get(id=sugerencia_id)
        
        if "nueva_sugerencia" in data:
            sugerencia.sugerencia = data["nueva_sugerencia"]
        if "incremento" in data:
            sugerencia.incremento = data["incremento"]
        
        sugerencia.save()
        
        return _format_success(
            {"id": sugerencia.id, "sugerencia": sugerencia.sugerencia},
            "Sugerencia modificada"
        )
    except Sugerencias.DoesNotExist:
        return _format_error(f"Sugerencia con ID {sugerencia_id} no encontrada")
    except Exception as e:
        return _format_error(f"Error modificando sugerencia: {str(e)}")


def _eliminar_sugerencia(data: Dict) -> Dict:
    """Función auxiliar para eliminar sugerencia."""
    sugerencia_id = data.get("sugerencia_id")
    if not sugerencia_id:
        return _format_error("sugerencia_id requerido")
    
    try:
        sugerencia = Sugerencias.objects.get(id=sugerencia_id)
        
        sugerencia.delete()
        return _format_success(message=f"Sugerencia con ID {sugerencia_id} eliminada")
    except Sugerencias.DoesNotExist:
        return _format_error(f"Sugerencia con ID {sugerencia_id} no encontrada")
    except Exception as e:
        return _format_error(f"Error eliminando sugerencia: {str(e)}")


def _listar_sugerencias(sugerencias_data: List[Dict] = None, tecla_id: int = None) -> List[Dict]:
    """Función auxiliar para listar sugerencias."""
    try:
        if tecla_id:
            sugerencias = Sugerencias.objects.filter(tecla_id=tecla_id)
        elif sugerencias_data:
            ids = [data.get("sugerencia_id") for data in sugerencias_data if data.get("sugerencia_id")]
            sugerencias = Sugerencias.objects.filter(id__in=ids)
        else:
            sugerencias = Sugerencias.objects.all()
        
        return list(sugerencias.values())
    except Exception as e:
        logger.error(f"Error listando sugerencias: {str(e)}")
        return [_format_error(f"Error listando sugerencias: {str(e)}")]


def _eliminar_sugerencias_por_tecla(tecla_id: int) -> Dict:
    """Función auxiliar para eliminar todas las sugerencias de una tecla."""
    try:
        count = Sugerencias.objects.filter(tecla_id=tecla_id).count()
        if count == 0:
            return _format_success(message=f"No hay sugerencias para eliminar en tecla ID {tecla_id}")
        
        Sugerencias.objects.filter(tecla_id=tecla_id).delete()
        return _format_success(message=f"Eliminadas {count} sugerencias de la tecla ID {tecla_id}")
    except Exception as e:
        return _format_error(f"Error eliminando sugerencias: {str(e)}")

# Añade esta nueva herramienta a tu fichero de herramientas

@tool
@db_connection_handler
def buscar_subteclas(
    tecla_padre_id: int
) -> List[Dict[str, Union[int, str, float]]]:
    """
    Busca y lista las subteclas (hijas directas) de una tecla padre específica.

    Args:
        tecla_padre_id: El ID exacto de la tecla padre.
    
    Returns:
        Una lista de diccionarios con los datos de las subteclas encontradas.
    """
    try:
        send_tool_message(f"Buscando tecla padre con ID {tecla_padre_id}...")
        
        # Buscar la tecla padre por ID
        try:
            tecla_padre = Teclas.objects.get(id=tecla_padre_id)
        except Teclas.DoesNotExist:
            return [_format_error(f"No se encontró tecla padre con ID {tecla_padre_id}")]
        
        send_tool_message(f"Buscando subteclas para '{tecla_padre.nombre}'...")
        
        # Buscar todas las teclas cuyo padre sea el encontrado
        subteclas_query = Teclas.objects.filter(parent_tecla=tecla_padre)
        
        results = [_tecla_to_dict(subtecla) for subtecla in subteclas_query]
        
        send_tool_message(f"Encontradas {len(results)} subteclas para '{tecla_padre.nombre}'")
        return results

    except Exception as e:
        error_msg = f"Error buscando subteclas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]

@tool
@db_connection_handler
def obtener_info_tecla(
    tecla_id: int
) -> Dict[str, Union[str, int, bool, None, float]]:
    """
    Obtiene información detallada de una tecla específica por su ID.
    Indica si es simple o compuesta y proporciona datos del padre, sección, comanda, familia y precios.
    
    Args:
        tecla_id: ID de la tecla a consultar
    
    Returns:
        Diccionario con información de la tecla:
        - id: ID de la tecla
        - nombre: Nombre de la tecla
        - tipo: Tipo de tecla ("SP" para simple, "CM" para compuesta)
        - descripcion_r: Descripción para receptor sin componer la real.
        - descripcion_t: Descripción para ticket sin componer la real.
        - padre_id: ID de la tecla padre (None si no tiene)
        - padre_nombre: Nombre de la tecla padre (None si no tiene)
        - num_subteclas: Número de subteclas si es compuesta (0 si es simple)
        - orden: Orden de la tecla
        - p1: Precio tarifa 1
        - p2: Precio tarifa 2
        - familia_id: ID de la familia
        - familia_nombre: Nombre de la familia
        - seccion_id: ID de la sección (None si no tiene)
        - seccion_nombre: Nombre de la sección (None si no tiene)
        - seccioncom_id: ID de la sección comanda (None si no tiene)
        - seccioncom_nombre: Nombre de la sección comanda (None si no tiene)
        - tag: Etiquetas de la tecla para la búsqueda
    """
    try:
        send_tool_message(f"Obteniendo información de la tecla ID {tecla_id}...")
        
        # Buscar la tecla por ID con relaciones
        try:
            tecla = Teclas.objects.select_related('familia', 'parent_tecla').prefetch_related(
                'teclasecciones__seccion',
                'teclascomanda__seccion'
            ).get(id=tecla_id)
        except Teclas.DoesNotExist:
            return _format_error(f"No se encontró tecla con ID {tecla_id}")
        
        tiene_padre = tecla.parent_tecla is not None
        padre_id = tecla.parent_tecla.id if tiene_padre else None
        padre_nombre = tecla.parent_tecla.nombre if tiene_padre else None
        
        # Contar subteclas si es compuesta
        num_subteclas = 0
        es_compuesta = tecla.tipo == "CM"
        if es_compuesta:
            num_subteclas = tecla.subteclas.count()
        
        # Información de familia
        familia_id = tecla.familia.id
        familia_nombre = tecla.familia.nombre
        
        # Información de sección
        teclaseccion = tecla.teclasecciones.first()
        seccion_id = teclaseccion.seccion.id if teclaseccion else None
        seccion_nombre = teclaseccion.seccion.nombre if teclaseccion else None
        
        # Información de sección comanda
        teclascom = tecla.teclascomanda.first()
        seccioncom_id = teclascom.seccion.id if teclascom else None
        seccioncom_nombre = teclascom.seccion.nombre if teclascom else None
        orden = teclascom.orden if teclascom else tecla.orden
        
        resultado = {
            "status": "success",
            "id": tecla.id,
            "nombre": tecla.nombre,
            "tipo": tecla.tipo,
            "descripcion_r": tecla.descripcion_r,
            "descripcion_t": tecla.descripcion_t,
            "tag": tecla.tag,   
            "orden": orden,
            "padre_id": padre_id,
            "padre_nombre": padre_nombre,
            "num_subteclas": num_subteclas,
            "p1": float(tecla.p1),
            "p2": float(tecla.p2),
            "familia_id": familia_id,
            "familia_nombre": familia_nombre,
            "seccion_id": seccion_id,
            "seccion_nombre": seccion_nombre,
            "seccioncom_id": seccioncom_id,
            "seccioncom_nombre": seccioncom_nombre
        }
       
        send_tool_message(f"Información de tecla ID {tecla_id} obtenida exitosamente")
        
        return resultado
        
    except Exception as e:
        error_msg = f"Error obteniendo información de tecla ID {tecla_id}: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return _format_error(error_msg)


@tool
@db_connection_handler
def obtener_info_lotes_sugerencias() -> Dict[str, Union[int, str]]:
    """
    Obtiene información sobre la cantidad de lotes de 100 sugerencias disponibles en la base de datos.
    
    Args:
        tecla_id: Opcional. Si se proporciona, cuenta solo las sugerencias de esa tecla.
    
    Returns:
        Diccionario con información sobre los lotes:
        - total_sugerencias: Número total de sugerencias
        - tamano_lote: Tamaño de cada lote (100)
        - total_lotes: Número total de lotes completos
        - sugerencias_en_ultimo_lote: Número de sugerencias en el último lote (si no está completo)
        - mensaje: Descripción del resultado
    """
    try:
        send_tool_message("Calculando información de lotes de sugerencias...")
        
        # Contar sugerencias
        query = Sugerencias.objects.all()
        
        total_sugerencias = query.count()
        tamano_lote = 100
        
        if total_sugerencias == 0:
            return {
                "status": "success",
                "total_sugerencias": 0,
                "tamano_lote": tamano_lote,
                "total_lotes": 0,
                "sugerencias_en_ultimo_lote": 0,
                "mensaje": "No hay sugerencias en la base de datos"
            }
        
        total_lotes = (total_sugerencias + tamano_lote - 1) // tamano_lote  # División hacia arriba
        sugerencias_en_ultimo_lote = total_sugerencias % tamano_lote
        if sugerencias_en_ultimo_lote == 0:
            sugerencias_en_ultimo_lote = tamano_lote
        
        mensaje = (
            f"Hay {total_sugerencias} sugerencias. "
            f"Se pueden dividir en {total_lotes} lotes de {tamano_lote} sugerencias. "
            f"El último lote contiene {sugerencias_en_ultimo_lote} sugerencias. "
            f"Los lotes están ordenados por nombre de tecla para agrupar sugerencias relacionadas."
        )
        
        send_tool_message(mensaje)
        
        return {
            "status": "success",
            "total_sugerencias": total_sugerencias,
            "tamano_lote": tamano_lote,
            "total_lotes": total_lotes,
            "sugerencias_en_ultimo_lote": sugerencias_en_ultimo_lote,
            "mensaje": mensaje
        }
        
    except Exception as e:
        error_msg = f"Error obteniendo información de lotes: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return _format_error(error_msg)


@tool
@db_connection_handler
def get_lote_sugerencias(numero_lote: int) -> Union[List[Dict], Dict]:
    """
    Consulta un lote paginado de 100 sugerencias para que sean revisadas.

    Usa esta herramienta para obtener una lista de sugerencias. La herramienta devuelve una lista
    plana de datos. Al recibir el resultado, debes agrupar las sugerencias por el campo 
    'tecla_nombre' y presentar el resultado final agrupado al usuario para una fácil revisión.

    Args:
        numero_lote (int): El número de la página o lote que se desea obtener (empezando desde 1).

    Returns:
        (list): Una lista plana de diccionarios. Cada diccionario representa una sugerencia
                y contiene los siguientes campos:
                - 'id' (int): El ID único de la sugerencia.
                - 'sugerencia' (str): El texto de la sugerencia.
                - 'incremento' (float): Un valor que indica la frecuencia de uso.
                - 'tecla_id' (int): El ID de la tecla asociada.
                - 'tecla_nombre' (str): El nombre de la tecla asociada.
        (dict): Si el 'numero_lote' no es válido, devuelve un diccionario con un mensaje de error.
    """
    try:
        send_tool_message(f"Obteniendo lote {numero_lote} de sugerencias...")
        
        # Validar que el número de lote sea positivo
        if numero_lote < 1:
            return _format_error("El número de lote debe ser mayor o igual a 1")
        
        # Obtener información total primero
        query = Sugerencias.objects.select_related('tecla')
        
        total_sugerencias = query.count()
        tamano_lote = 100
        
        
        total_lotes = (total_sugerencias + tamano_lote - 1) // tamano_lote
        
        # Validar que el lote solicitado existe
        if numero_lote > total_lotes:
            mensaje_error = (
                f"({total_sugerencias} sugerencias en total). "
                f"Puedes solicitar lotes del 1 al {total_lotes}."
            )
            return {
                "status": "error",
                "message": mensaje_error,
                "total_lotes": total_lotes,
                "total_sugerencias": total_sugerencias,
                "lote_solicitado": numero_lote
            }
        
        # Calcular el rango del lote
        inicio = (numero_lote - 1) * tamano_lote
        fin = inicio + tamano_lote
        
        # Obtener las sugerencias del lote específico ordenadas por nombre de tecla y luego por sugerencia
        sugerencias_lote = query.order_by('tecla__nombre', 'sugerencia', 'id')[inicio:fin]
        
        # Formatear los datos
        resultado = [
            {
                "id": s.id,
                "sugerencia": s.sugerencia,
                "incremento": float(s.incremento),
                "tecla_id": s.tecla.id,
                "tecla_nombre": s.tecla.descripcion_r or s.tecla.nombre
            }
            for s in sugerencias_lote
        ]
        
        send_tool_message(
            f"Hay {total_sugerencias} sugerencias ordenadas por tecla. "
            f"Lote {numero_lote} obtenido: {len(resultado)} sugerencias "
            f"(desde la {inicio + 1} hasta la {inicio + len(resultado)})"
        )
        
        return resultado
        
    except Exception as e:
        error_msg = f"Error obteniendo lote {numero_lote}: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return _format_error(error_msg)


@tool
@db_connection_handler
def buscar_sugerencias_con_incremento(incremento_minimo: float = 0.01) -> List[Dict[str, Union[int, str, float]]]:
    """
    Consulta sugerencias activas, filtrando las que superan un umbral de uso ('incremento').

    Usa esta herramienta para obtener una lista de las sugerencias más utilizadas. La herramienta 
    devuelve una lista plana de datos. Al recibir el resultado, tu tarea es agrupar las sugerencias 
    por el campo 'tecla_nombre' y presentar el informe final de forma agrupada al usuario.

    Args:
        incremento_minimo (float, optional): Filtra las sugerencias para mostrar solo aquellas
                                            con un incremento superior a este valor. 
                                            Por defecto es 0.01 para excluir las de uso nulo.

    Returns:
        (list): Una lista plana de diccionarios. Cada diccionario es una sugerencia y contiene:
                - 'id' (int): El ID de la sugerencia.
                - 'sugerencia' (str): El texto de la sugerencia.
                - 'incremento' (float): La frecuencia de uso.
                - 'tecla_id' (int): El ID de la tecla asociada.
                - 'tecla_nombre' (str): El nombre de la tecla asociada.
    """
    try:
        send_tool_message(f"Buscando sugerencias con incremento >= {incremento_minimo}...")
        
        # Buscar sugerencias con incremento mayor al mínimo especificado
        query = Sugerencias.objects.select_related('tecla').filter(
            incremento__gte=incremento_minimo
        ).order_by('-incremento', 'tecla__nombre', 'sugerencia')
        
        total_encontradas = query.count()
        
        if total_encontradas == 0:
            send_tool_message(f"No se encontraron sugerencias con incremento >= {incremento_minimo}")
            return []
        
        # Formatear los datos
        resultados = [
            {
                "id": s.id,
                "sugerencia": s.sugerencia,
                "incremento": float(s.incremento),
                "tecla_id": s.tecla.id,
                "tecla_nombre": s.tecla.descripcion_r or s.tecla.nombre
            }
            for s in query
        ]
        
        send_tool_message(f"Encontradas {total_encontradas} sugerencias con incremento >= {incremento_minimo}")
        
        return resultados
        
    except Exception as e:
        error_msg = f"Error buscando sugerencias con incremento: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]


@tool
@db_connection_handler
def obtener_sugerencias_por_teclas(teclas_ids: List[int]) -> List[Dict[str, Union[int, str, float]]]:
    """
    Obtiene todas las sugerencias de una o varias teclas específicas.
    
    Args:
        teclas_ids: Lista de IDs de teclas de las cuales obtener las sugerencias
    
    Returns:
        Lista de diccionarios con todas las sugerencias de las teclas especificadas:
        - id: ID de la sugerencia
        - sugerencia: Texto de la sugerencia
        - incremento: Valor del incremento
        - tecla_id: ID de la tecla asociada
        - tecla_nombre: Nombre de la tecla asociada
    """
    try:
        if not teclas_ids:
            return [_format_error("Se requiere una lista de IDs de teclas")]
        
        send_tool_message(f"Buscando sugerencias para {len(teclas_ids)} teclas...")
        
        # Buscar sugerencias de las teclas especificadas
        query = Sugerencias.objects.select_related('tecla').filter(
            tecla_id__in=teclas_ids
        ).order_by('tecla__nombre', 'sugerencia')
        
        total_encontradas = query.count()
        
        if total_encontradas == 0:
            send_tool_message(f"No se encontraron sugerencias para las teclas especificadas: {teclas_ids}")
            return []
        
        # Verificar qué teclas existen y cuáles no
        teclas_existentes = Teclas.objects.filter(id__in=teclas_ids).values_list('id', flat=True)
        teclas_no_encontradas = set(teclas_ids) - set(teclas_existentes)
        
        if teclas_no_encontradas:
            send_tool_message(f"Advertencia: Las siguientes teclas no existen: {list(teclas_no_encontradas)}")
        
        # Formatear los datos
        resultados = [
            {
                "id": s.id,
                "sugerencia": s.sugerencia,
                "incremento": float(s.incremento),
                "tecla_id": s.tecla.id,
                "tecla_nombre": s.tecla.descripcion_r or s.tecla.nombre
            }
            for s in query
        ]
        
        # Contar sugerencias por tecla para el reporte
        teclas_con_sugerencias = set(r["tecla_id"] for r in resultados)
        teclas_sin_sugerencias = set(teclas_existentes) - teclas_con_sugerencias
        
        mensaje = f"Encontradas {total_encontradas} sugerencias"
        if teclas_sin_sugerencias:
            mensaje += f". Teclas sin sugerencias: {list(teclas_sin_sugerencias)}"
        
        send_tool_message(mensaje)
        
        return resultados
        
    except Exception as e:
        error_msg = f"Error obteniendo sugerencias por teclas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]



@tool
@db_connection_handler
def ordenar_teclas(
    teclas_orden: List[Dict[str, int]]
) -> List[Dict[str, Union[str, int]]]:
    """
    Asigna un orden específico a múltiples teclas.
    
    Si una tecla tiene una relación con seccioncom (Teclascom), el orden se guarda tanto 
    en seccioncom.orden como en tecla.orden. Si no tiene seccioncom, solo se guarda en tecla.orden.
    
    Args:
        teclas_orden: Lista de diccionarios con la estructura:
            [{"tecla_id": 1, "orden": 5}, {"tecla_id": 2, "orden": 10}, ...]
    
    Returns:
        Lista de resultados para cada tecla procesada con formato:
        - status: "success" o "error"
        - message: Mensaje descriptivo del resultado
        - tecla_id: ID de la tecla procesada
        - tecla_nombre: Nombre de la tecla
        - orden_asignado: Valor del orden asignado
        - tiene_seccioncom: Boolean indicando si la tecla tiene seccioncom
    """
    try:
        if not teclas_orden:
            return [_format_error("No se proporcionaron datos de teclas para ordenar")]
        
        results = []
        
        for item in teclas_orden:
            tecla_id = item.get("tecla_id")
            nuevo_orden = item.get("orden")
            
            if not tecla_id or nuevo_orden is None:
                results.append(_format_error(f"Datos incompletos: tecla_id={tecla_id}, orden={nuevo_orden}"))
                continue
            
            try:
                # Buscar la tecla
                tecla = Teclas.objects.get(id=tecla_id)
                
                # Verificar si tiene relación con seccioncom
                teclascom = Teclascom.objects.filter(tecla=tecla).first()
                
                if teclascom:
                    # Si tiene seccioncom, actualizar tanto seccioncom.orden como tecla.orden
                    teclascom.orden = nuevo_orden
                    teclascom.save()
                    
                    tecla.orden = nuevo_orden
                    tecla.save()
                    
                    message = f"Orden {nuevo_orden} asignado a tecla '{tecla.nombre}' (actualizado en seccioncom y tecla)"
                    tiene_seccioncom = True
                    
                else:
                    # Si no tiene seccioncom, solo actualizar tecla.orden
                    tecla.orden = nuevo_orden
                    tecla.save()
                    
                    message = f"Orden {nuevo_orden} asignado a tecla '{tecla.nombre}' (solo en tecla)"
                    tiene_seccioncom = False
                
                send_tool_message(message)
                
                results.append({
                    "status": "success",
                    "message": message,
                    "tecla_id": tecla.id,
                    "tecla_nombre": tecla.nombre,
                    "orden_asignado": nuevo_orden,
                    "tiene_seccioncom": tiene_seccioncom
                })
                
            except Teclas.DoesNotExist:
                error_msg = f"Tecla con ID {tecla_id} no encontrada"
                results.append({
                    "status": "error",
                    "message": error_msg,
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "orden_asignado": None,
                    "tiene_seccioncom": None
                })
                
            except Exception as e:
                error_msg = f"Error procesando tecla ID {tecla_id}: {str(e)}"
                logger.error(error_msg, exc_info=True)
                results.append({
                    "status": "error",
                    "message": error_msg,
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "orden_asignado": None,
                    "tiene_seccioncom": None
                })
        
        # Mensaje resumen
        exitosas = len([r for r in results if r.get("status") == "success"])
        send_tool_message(f"Operación completada: {exitosas}/{len(results)} teclas ordenadas exitosamente")
        
        return results
        
    except Exception as e:
        error_msg = f"Error general ordenando teclas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]


@tool
@db_connection_handler
def gestionar_existencias_teclas(
    operacion: str,
    teclas_data: List[Dict[str, int]]
) -> List[Dict[str, Union[str, int]]]:
    """
    Gestiona el bloqueo y desbloqueo de teclas por falta de existencias.
    
    Esta herramienta permite activar o desactivar teclas registrándolas en la tabla 
    TeclasAgotadas para bloquearlas o eliminándolas de esa tabla para desbloquearlas.
    
    Args:
        operacion: "bloquear" para marcar teclas como agotadas, "desbloquear" para quitarlas
        teclas_data: Lista de diccionarios con la estructura:
            [{"tecla_id": 1}, {"tecla_id": 2}, ...]
    
    Returns:
        Lista de resultados para cada tecla procesada con formato:
        - status: "success" o "error"
        - message: Mensaje descriptivo del resultado
        - tecla_id: ID de la tecla procesada
        - tecla_nombre: Nombre de la tecla
        - operacion: Operación realizada ("bloquear" o "desbloquear")
        - bloqueada: Boolean indicando el estado final de bloqueo
    """
    try:
        if operacion not in ["bloquear", "desbloquear"]:
            return [_format_error("Operación debe ser 'bloquear' o 'desbloquear'")]
        
        if not teclas_data:
            return [_format_error("No se proporcionaron datos de teclas")]
        
        results = []
        
        for item in teclas_data:
            tecla_id = item.get("tecla_id")
            
            if not tecla_id:
                results.append(_format_error("tecla_id es requerido"))
                continue
            
            try:
                # Verificar que la tecla existe
                tecla = Teclas.objects.get(id=tecla_id)
                
                if operacion == "bloquear":
                    result = _bloquear_tecla(tecla)
                else:  # desbloquear
                    result = _desbloquear_tecla(tecla)
                
                results.append(result)
                
            except Teclas.DoesNotExist:
                error_msg = f"Tecla con ID {tecla_id} no encontrada"
                results.append({
                    "status": "error",
                    "message": error_msg,
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "operacion": operacion,
                    "bloqueada": None
                })
                
            except Exception as e:
                error_msg = f"Error procesando tecla ID {tecla_id}: {str(e)}"
                logger.error(error_msg, exc_info=True)
                results.append({
                    "status": "error",
                    "message": error_msg,
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "operacion": operacion,
                    "bloqueada": None
                })
        
        # Mensaje resumen
        exitosas = len([r for r in results if r.get("status") == "success"])
        send_tool_message(f"Operación completada: {exitosas}/{len(results)} teclas procesadas exitosamente")
        
        return results
        
    except Exception as e:
        error_msg = f"Error general gestionando existencias: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]


def _bloquear_tecla(tecla: Teclas) -> Dict:
    """Función auxiliar para bloquear una tecla por falta de existencias."""
    try:
        # Verificar si ya está bloqueada
        ya_bloqueada = TeclasAgotadas.objects.filter(tecla_id=tecla.id)
        
        if ya_bloqueada and ya_bloqueada.exists():
            ya_bloqueada.delete()  # Eliminar registro anterior si existe
            
        
        # Eliminar registros anteriores si existen y crear nuevo registro
        TeclasAgotadas.objects.filter(tecla_id=tecla.id).delete()
        registro = TeclasAgotadas(tecla=tecla, fecha=date.today().strftime("%Y/%m/%d"))
        registro.save()
        
        # Comunicar cambios a devices
        comunicar_cambios_devices("md", "teclas", tecla.serialize())
        
        message = f"Tecla '{tecla.nombre}' bloqueada por falta de existencias"
        send_tool_message(message)
        
        return {
            "status": "success",
            "message": message,
            "tecla_id": tecla.id,
            "tecla_nombre": tecla.nombre,
            "operacion": "bloquear",
            "bloqueada": True
        }
        
    except Exception as e:
        error_msg = f"Error bloqueando tecla '{tecla.nombre}': {str(e)}"
        logger.error(error_msg, exc_info=True)
        return {
            "status": "error",
            "message": error_msg,
            "tecla_id": tecla.id,
            "tecla_nombre": tecla.nombre,
            "operacion": "bloquear",
            "bloqueada": None
        }


def _desbloquear_tecla(tecla: Teclas) -> Dict:
    """Función auxiliar para desbloquear una tecla restaurando existencias."""
    try:
        # Verificar si está bloqueada
        registros_bloqueados = TeclasAgotadas.objects.filter(tecla_id=tecla.id)
        
        if not registros_bloqueados.exists():
            message = f"Tecla '{tecla.nombre}' no estaba bloqueada"
            send_tool_message(message)
            return {
                "status": "success",
                "message": message,
                "tecla_id": tecla.id,
                "tecla_nombre": tecla.nombre,
                "operacion": "desbloquear",
                "bloqueada": False
            }
        
        # Eliminar todos los registros de bloqueo
        count_eliminados = registros_bloqueados.delete()
        
        # Comunicar cambios a devices
        comunicar_cambios_devices("md", "teclas", tecla.serialize())
        
        message = f"Tecla '{tecla.nombre}' desbloqueada - existencias restauradas (eliminados {count_eliminados} registros)"
        send_tool_message(message)
        
        return {
            "status": "success",
            "message": message,
            "tecla_id": tecla.id,
            "tecla_nombre": tecla.nombre,
            "operacion": "desbloquear",
            "bloqueada": False
        }
        
    except Exception as e:
        error_msg = f"Error desbloqueando tecla '{tecla.nombre}': {str(e)}"
        logger.error(error_msg, exc_info=True)
        return {
            "status": "error",
            "message": error_msg,
            "tecla_id": tecla.id,
            "tecla_nombre": tecla.nombre,
            "operacion": "desbloquear",
            "bloqueada": None
        }


@tool
@db_connection_handler
def consultar_estado_existencias_teclas(
    teclas_ids: Optional[List[int]] = None,
    solo_fecha_hoy: bool = True
) -> List[Dict[str, Union[str, int, bool]]]:
    """
    Consulta solo las teclas que están bloqueadas por falta de existencias.
    
    Args:
        teclas_ids: Lista opcional de IDs de teclas a consultar. 
                   Si no se proporciona, consulta todas las teclas bloqueadas.
        solo_fecha_hoy: Si True, filtra solo las teclas bloqueadas hoy.
                       Si False, muestra todas las teclas bloqueadas.
    
    Returns:
        Lista de diccionarios con información solo de las teclas bloqueadas:
        - tecla_id: ID de la tecla
        - tecla_nombre: Nombre de la tecla
        - bloqueada: Siempre True (solo devuelve bloqueadas)
        - fecha_bloqueo: Fecha del bloqueo
        - mensaje: Descripción del estado
        - es_bloqueo_hoy: Boolean indicando si fue bloqueada hoy
    """
    try:
        fecha_hoy = date.today().strftime("%Y/%m/%d")
        
        # Construir query base
        query = TeclasAgotadas.objects.select_related('tecla')
        
        if teclas_ids:
            # Consultar teclas específicas que estén bloqueadas
            query = query.filter(tecla_id__in=teclas_ids)
            mensaje_inicial = f"Consultando teclas bloqueadas entre {len(teclas_ids)} teclas específicas"
        else:
            # Consultar todas las teclas bloqueadas
            mensaje_inicial = "Consultando todas las teclas bloqueadas"
        
        if solo_fecha_hoy:
            # Filtrar solo las bloqueadas hoy
            query = query.filter(fecha=fecha_hoy)
            mensaje_inicial += f" (solo bloqueadas hoy: {fecha_hoy})"
        
        send_tool_message(mensaje_inicial + "...")
        
        teclas_agotadas = query.all()
        results = []
        
        for registro in teclas_agotadas:
            es_bloqueo_hoy = registro.fecha == fecha_hoy
            results.append({
                "tecla_id": registro.tecla.id,
                "tecla_nombre": registro.tecla.nombre,
                "bloqueada": True,
                "fecha_bloqueo": registro.fecha,
                "es_bloqueo_hoy": es_bloqueo_hoy,
                "mensaje": f"Bloqueada desde {registro.fecha}" + (" (HOY)" if es_bloqueo_hoy else "")
            })
        
        # Estadísticas
        total_bloqueadas = len(results)
        
        if total_bloqueadas == 0:
            if solo_fecha_hoy:
                send_tool_message(f"No se encontraron teclas bloqueadas hoy ({fecha_hoy})")
            else:
                send_tool_message("No se encontraron teclas bloqueadas")
        else:
            if solo_fecha_hoy:
                send_tool_message(f"Encontradas {total_bloqueadas} teclas bloqueadas hoy ({fecha_hoy})")
            else:
                bloqueadas_hoy = len([r for r in results if r["es_bloqueo_hoy"]])
                send_tool_message(f"Encontradas {total_bloqueadas} teclas bloqueadas (de las cuales {bloqueadas_hoy} fueron bloqueadas hoy)")
        
        return results
        
    except Exception as e:
        error_msg = f"Error consultando teclas bloqueadas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return [_format_error(error_msg)]


@tool
@db_connection_handler
def limpiar_espacios_sugerencias() -> Dict[str, Union[str, int]]:
    """
    HERRAMIENTA TEMPORAL: Limpia espacios en blanco al inicio y final de todas las sugerencias.
    
    Esta herramienta recorre todas las sugerencias de la base de datos, aplica strip() 
    a cada texto de sugerencia para eliminar espacios en blanco al inicio y final,
    y guarda los cambios en la base de datos.
    
    ADVERTENCIA: Esta herramienta modifica datos en la base de datos. Úsala con precaución.
    
    Returns:
        Diccionario con el resultado de la operación:
        - status: "success" o "error"
        - message: Mensaje descriptivo del resultado
        - total_procesadas: Total de sugerencias procesadas
        - total_modificadas: Total de sugerencias que fueron modificadas
        - sugerencias_modificadas: Lista con ejemplos de las modificaciones realizadas
    """
    try:
        send_tool_message("🧹 INICIANDO LIMPIEZA TEMPORAL DE ESPACIOS EN SUGERENCIAS...\n⚠️ ADVERTENCIA: Esta operación modificará datos en la base de datos")
        
        # Obtener todas las sugerencias
        todas_sugerencias = Sugerencias.objects.all()
        total_procesadas = todas_sugerencias.count()
        
        if total_procesadas == 0:
            return {
                "status": "success",
                "message": "No hay sugerencias para procesar",
                "total_procesadas": 0,
                "total_modificadas": 0,
                "sugerencias_modificadas": []
            }
        
        send_tool_message(f"📊 Procesando {total_procesadas} sugerencias...")
        
        modificadas = 0
        ejemplos_modificaciones = []
        
        for sugerencia in todas_sugerencias:
            try:
                texto_original = sugerencia.sugerencia
                
                if texto_original is None:
                    continue
                
                # Aplicar strip() para limpiar espacios
                texto_limpio = texto_original.strip()
                
                # Solo actualizar si hay diferencia
                if texto_original != texto_limpio:
                    sugerencia.sugerencia = texto_limpio
                    sugerencia.save()
                    
                    modificadas += 1
                    
                    # Guardar ejemplo para el reporte (máximo 10 ejemplos)
                    if len(ejemplos_modificaciones) < 10:
                        ejemplos_modificaciones.append({
                            "id": sugerencia.id,
                            "tecla_id": sugerencia.tecla.id if sugerencia.tecla else None,
                            "tecla_nombre": sugerencia.tecla.nombre if sugerencia.tecla else "Sin tecla",
                            "antes": repr(texto_original),  # repr() para mostrar espacios
                            "despues": repr(texto_limpio)
                        })
                
            except Exception as e:
                logger.warning(f"Error procesando sugerencia ID {sugerencia.id}: {e}")
                continue
        
        # Mensaje de resultado
        if modificadas > 0:
            mensaje = f"✅ Limpieza completada: {modificadas} de {total_procesadas} sugerencias fueron modificadas"
            send_tool_message(mensaje)
            
            # Mostrar ejemplos
            if ejemplos_modificaciones:
                send_tool_message("📝 Ejemplos de modificaciones realizadas:")
                for ejemplo in ejemplos_modificaciones:
                    send_tool_message(
                        f"  • ID {ejemplo['id']} ({ejemplo['tecla_nombre']}): "
                        f"{ejemplo['antes']} → {ejemplo['despues']}"
                    )
            
            return {
                "status": "success",
                "message": mensaje,
                "total_procesadas": total_procesadas,
                "total_modificadas": modificadas,
                "sugerencias_modificadas": ejemplos_modificaciones
            }
        else:
            mensaje = f"✅ Proceso completado: Ninguna de las {total_procesadas} sugerencias necesitaba limpieza"
            send_tool_message(mensaje)
            
            return {
                "status": "success",
                "message": mensaje,
                "total_procesadas": total_procesadas,
                "total_modificadas": 0,
                "sugerencias_modificadas": []
            }
        
    except Exception as e:
        error_msg = f"❌ Error limpiando espacios en sugerencias: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(error_msg)
        
        return {
            "status": "error",
            "message": error_msg,
            "total_procesadas": 0,
            "total_modificadas": 0,
            "sugerencias_modificadas": []
        }


@tool
@db_connection_handler
def listar_sugerencias_con_limite(
    limite: int = 50,
    offset: int = 0,
    ordenar_por: str = "id",
    direccion: str = "asc",
    tecla_id: Optional[int] = None,
    incremento_minimo: Optional[float] = None,
    buscar_sugerencia: Optional[str] = None
) -> Dict[str, Union[List[Dict], int, str]]:
    """
    Lista sugerencias con paginación y filtros opcionales.
    
    Args:
        limite: Número máximo de sugerencias a devolver (por defecto 50)
        offset: Número de registros a saltar para paginación (por defecto 0)
        ordenar_por: Campo por el que ordenar ("tecla_nombre", "sugerencia", "incremento", "id")
        direccion: Dirección del ordenamiento ("asc" o "desc")
        tecla_id: Opcional. Filtrar por ID de tecla específica
        incremento_minimo: Opcional. Filtrar por incremento mínimo
        buscar_sugerencia: Opcional. Buscar texto en el campo sugerencia usando icontains (insensible a mayúsculas)

    Returns:
        Diccionario con:
        - sugerencias: Lista de sugerencias encontradas
        - total: Total de sugerencias que coinciden con los filtros
        - limite: Límite usado
        - offset: Offset usado
        - tiene_mas: Boolean indicando si hay más resultados
        - pagina_actual: Número de página actual (basado en limite/offset)
        - total_paginas: Total de páginas disponibles
    """
    try:
        if limite <= 0:
            return _format_error("El límite debe ser mayor a 0")
        
        if limite > 1000:
            return _format_error("El límite máximo permitido es 1000")
        
        if offset < 0:
            return _format_error("El offset debe ser mayor o igual a 0")
        
        if ordenar_por not in ["tecla_nombre", "sugerencia", "incremento", "id"]:
            return _format_error("ordenar_por debe ser: 'tecla_nombre', 'sugerencia', 'incremento' o 'id'")
        
        if direccion not in ["asc", "desc"]:
            return _format_error("direccion debe ser 'asc' o 'desc'")
        
        send_tool_message(f"Listando sugerencias con límite {limite}, offset {offset}")
        
        # Construir query base
        query = Sugerencias.objects.select_related('tecla')
        
        # Aplicar filtros opcionales
        if tecla_id:
            query = query.filter(tecla_id=tecla_id)
            send_tool_message(f"Filtrando por tecla ID {tecla_id}")
        
        if incremento_minimo is not None:
            query = query.filter(incremento__gte=incremento_minimo)
            send_tool_message(f"Filtrando por incremento >= {incremento_minimo}")
        
        if buscar_sugerencia:
            query = query.filter(sugerencia__icontains=buscar_sugerencia)
            send_tool_message(f"Buscando sugerencias que contengan: '{buscar_sugerencia}' (insensible a mayúsculas)")
        
        # Contar total antes de aplicar límite
        total = query.count()
        
        # Aplicar ordenamiento
        if ordenar_por == "tecla_nombre":
            orden = "tecla__nombre" if direccion == "asc" else "-tecla__nombre"
        elif ordenar_por == "sugerencia":
            orden = "sugerencia" if direccion == "asc" else "-sugerencia"
        elif ordenar_por == "incremento":
            orden = "incremento" if direccion == "asc" else "-incremento"
        else:  # id
            orden = "id" if direccion == "asc" else "-id"
        
        # Aplicar ordenamiento secundario para consistencia
        if ordenar_por != "id":
            query = query.order_by(orden, "id")
        else:
            query = query.order_by(orden)
        
        # Aplicar paginación
        sugerencias = query[offset:offset + limite]
        
        # Formatear resultados
        resultados = [
            {
                "id": s.id,
                "sugerencia": s.sugerencia,
                "incremento": float(s.incremento),
                "tecla_id": s.tecla.id,
                "tecla_nombre": s.tecla.descripcion_r or s.tecla.nombre
            }
            for s in sugerencias
        ]
        
        # Calcular información de paginación
        pagina_actual = (offset // limite) + 1
        total_paginas = (total + limite - 1) // limite if total > 0 else 0
        tiene_mas = offset + limite < total
        
        resultado = {
            "status": "success",
            "sugerencias": resultados,
            "total": total,
            "limite": limite,
            "offset": offset,
            "tiene_mas": tiene_mas,
            "pagina_actual": pagina_actual,
            "total_paginas": total_paginas,
            "resultados_en_pagina": len(resultados)
        }
        
        mensaje_filtros = ""
        if tecla_id:
            mensaje_filtros += f" (tecla ID {tecla_id})"
        if incremento_minimo is not None:
            mensaje_filtros += f" (incremento >= {incremento_minimo})"
        if buscar_sugerencia:
            mensaje_filtros += f" (contiene '{buscar_sugerencia}')"
        
        send_tool_message(
            f"Listadas {len(resultados)} de {total} sugerencias totales{mensaje_filtros}. "
            f"Página {pagina_actual} de {total_paginas}"
        )
        
        return resultado
        
    except Exception as e:
        error_msg = f"Error listando sugerencias con límite: {str(e)}"
        logger.error(error_msg, exc_info=True)
        return _format_error(error_msg)



# Lista de herramientas consolidadas
tools: List = [
    buscar_teclas,
    gestionar_teclas,
    gestionar_relaciones_teclas,
    gestionar_sugerencias,
    buscar_subteclas,
    obtener_info_tecla,
    obtener_info_lotes_sugerencias,
    get_lote_sugerencias,
    buscar_sugerencias_con_incremento,
    obtener_sugerencias_por_teclas,
    ordenar_teclas,
    gestionar_existencias_teclas,
    consultar_estado_existencias_teclas,
    listar_sugerencias_con_limite,
   
]