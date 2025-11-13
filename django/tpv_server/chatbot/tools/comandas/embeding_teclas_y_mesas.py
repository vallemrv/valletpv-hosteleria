import chromadb
from sentence_transformers import SentenceTransformer
from langchain_core.tools import tool
from typing import List, Dict, Any
from gestion.tools.config_logs import log_debug_chatbot as logger

from chatbot.decorators.db_connection_manager import db_connection_handler
from gestion.models.mesas import Mesas
from gestion.models.teclados import Teclas, Sugerencias, TeclasAgotadas
import os
from django.conf import settings
from chatbot.utilidades.ws_sender import send_tool_message
from datetime import date

import traceback


# --- 1. Inicialización de componentes clave ---

# Carga el modelo para crear embeddings. Se descarga la primera vez.
# Es buena idea tenerlo fuera de la herramienta para no cargarlo cada vez que se llama.
embedding_model = SentenceTransformer('all-MiniLM-L6-v2')

# --- Configuración de la ruta para ChromaDB ---
# Construir la ruta dentro de la carpeta 'media' de Django
db_folder_path = os.path.join(settings.MEDIA_ROOT, 'db')

# Crear la carpeta 'db' si no existe
if not os.path.exists(db_folder_path):
    os.makedirs(db_folder_path)

# Ruta final para la base de datos de ChromaDB. ChromaDB creará un directorio aquí.
chroma_db_path = os.path.join(db_folder_path, 'valleTPV_chroma_db')

# Inicializar el cliente persistente de ChromaDB
client = chromadb.PersistentClient(path=chroma_db_path)


@tool
@db_connection_handler
def crear_y_poblar_base_de_datos_tpv(colecciones: List[str] = None) -> str:
    """
    Carga datos relevantes desde la base de datos de Django y los utiliza para crear o actualizar 
    una base de datos vectorial (ChromaDB) para búsquedas semánticas. Permite elegir qué colecciones poblar.

    Esta herramienta es fundamental para inicializar o sincronizar el sistema de búsqueda inteligente.
    Recorre los modelos de `Mesas` y `Teclas`, genera embeddings
    (representaciones numéricas) para cada uno y los almacena en las colecciones correspondientes.
    Esto permite realizar búsquedas por significado, como "tráeme algo de picar salado".

    IMPORTANTE: Esta función borra completamente las colecciones seleccionadas antes de poblarlas,
    asegurando que no haya datos antiguos o duplicados.

    Args:
        colecciones (List[str], optional): Lista de colecciones a poblar. Valores válidos: 
                                          'mesas', 'teclas'. Si no se especifica,
                                          se poblarán todas las colecciones.

    Returns:
        str: Un mensaje de confirmación indicando el número total de elementos añadidos,
             o un mensaje de error si algo falla durante el proceso.
    """
    send_tool_message("Iniciando la creación y población de la base de datos vectorial...")
    
    # Si no se especifican colecciones, poblar todas
    if colecciones is None:
        colecciones = ["mesas", "teclas"]  # Cambiado 'productos' a 'teclas' y eliminado 'extras'.
    
    # Validar que las colecciones sean válidas
    colecciones_validas = ["mesas", "teclas"]  # Cambiado 'productos' a 'teclas' y eliminado 'extras'.
    colecciones_invalidas = [c for c in colecciones if c not in colecciones_validas]
    if colecciones_invalidas:
        error_msg = f"Colecciones inválidas: {colecciones_invalidas}. Use: {colecciones_validas}"
        send_tool_message(error_msg)
        return error_msg
    
    try:
        # --- 1. Eliminar y recrear las colecciones seleccionadas ---
        send_tool_message(f"Limpiando colecciones: {colecciones}...")
        
        for coleccion_nombre in colecciones:
            try:
                # Intentar eliminar la colección si existe
                client.delete_collection(name=coleccion_nombre)
                send_tool_message(f"Colección '{coleccion_nombre}' eliminada.")
            except Exception as e:
                # Si no existe, no es un error
                pass
        
        # --- 2. Crear las colecciones limpias ---
        send_tool_message("Creando colecciones limpias...")
        collections_dict = {}
        if "mesas" in colecciones:
            collections_dict["mesas"] = client.create_collection(name="mesas")
        if "teclas" in colecciones:
            collections_dict["teclas"] = client.create_collection(name="teclas")

        # --- 3. Cargar y procesar MESAS ---
        if "mesas" in colecciones:
            mesas_collection = collections_dict["mesas"]
            mesas_qs = Mesas.objects.prefetch_related('mesaszona_set__zona').all()
            count_mesas = mesas_qs.count()
            index = 0
            ratio = 0
            for mesa in mesas_qs:
                index += 1
                if ratio <= 0:
                    send_tool_message(f"Procesando mesa {index} de {count_mesas}...")
                    ratio = 20
                else:
                    ratio -= 1
                # Obtener la tarifa de la zona. Una mesa puede estar en una zona.
                tarifa = 1  # Tarifa por defecto si no tiene zona
                mesaszona = mesa.mesaszona_set.first()
                if mesaszona and mesaszona.zona:
                    tarifa = mesaszona.zona.tarifa

                metadata = {"id": mesa.id, "nombre": mesa.nombre, "tarifa": int(tarifa)}
                embedding = embedding_model.encode(mesa.nombre).tolist()
                mesas_collection.add(ids=[str(mesa.id)], embeddings=[embedding], metadatas=metadata)

        # --- 4. Cargar y procesar TECLAS (antes 'productos') ---
        if "teclas" in colecciones:
            teclas_collection = collections_dict["teclas"]
            teclas_qs = Teclas.objects.filter(tipo="SP").select_related('parent_tecla')
            count_teclas = teclas_qs.count()
            index = 0
            ratio = 0
            for tecla in teclas_qs:
                index += 1
                if ratio <= 0:
                    send_tool_message(f"Procesando tecla {index} de {count_teclas}...")
                    ratio = 20
                else:
                    ratio -= 1
                
                # Limpiar datos para evitar nulos
                nombre_limpio = tecla.nombre or ""
                descripcion_limpia = tecla.descripcion_r or ""
                descripcion_t_limpia = tecla.descripcion_t or ""
                tag_limpio = tecla.tag or ""
                p1_limpio = float(tecla.p1 or 0.0)
                p2_limpio = float(tecla.p2 or 0.0)

                # Crear descripción según las reglas especificadas
                if not descripcion_limpia:
                    if tecla.parent_tecla:
                        nombre_padre = tecla.parent_tecla.nombre or ""
                        descripcion_final = f"{nombre_padre} {nombre_limpio}".strip()
                    else:
                        descripcion_final = nombre_limpio
                else:
                    descripcion_final = descripcion_limpia

                if not descripcion_t_limpia:
                    if tecla.parent_tecla:
                        descripcion_t_final = tecla.parent_tecla.nombre or ""
                    else:
                        descripcion_t_final = nombre_limpio
                else:
                    descripcion_t_final = descripcion_t_limpia

                # Crear contenido para embedding
                content = f"Tecla: {descripcion_final}. Tags: {tag_limpio}"
                metadata = {
                    "id": tecla.id,
                    "p1": p1_limpio,
                    "p2": p2_limpio,
                    "descripcion": descripcion_final,
                    "descripcion_t": descripcion_t_final,
                    "tag": tag_limpio,
                }
                embedding = embedding_model.encode(content).tolist()
                teclas_collection.add(ids=[str(tecla.id)], embeddings=[embedding], metadatas=metadata)

        # --- 6. Mensaje de éxito ---
        elementos_procesados = []
        if "mesas" in colecciones:
            mesas_count = Mesas.objects.count()
            elementos_procesados.append(f"{mesas_count} mesas")
        if "teclas" in colecciones:
            teclas_count = Teclas.objects.filter(tipo="SP").count()
            elementos_procesados.append(f"{teclas_count} teclas")

        mensaje_final = (
            f"¡Éxito! Base de datos poblada con: {', '.join(elementos_procesados)}. "
            f"Colecciones procesadas: {', '.join(colecciones)}"
        )
        send_tool_message(mensaje_final)
        return mensaje_final

    except Exception as e:
        error_message = f"Ocurrió un error: {e}"
        send_tool_message(error_message)
        logger.error(f"{error_message}\n{traceback.format_exc()}")
        return error_message



@tool
def buscar_por_similitud_tpv(
    colecciones: List[str],
    texto_busqueda: str,
    n_resultados: int = 5
) -> Dict[str, Any]:
    """
    Busca elementos en una o varias colecciones específicas ('mesas', 'teclas') de la base de datos vectorial
    utilizando búsqueda por similitud semántica.

    Es ideal para encontrar items cuando el usuario describe lo que quiere en lugar de usar un nombre exacto.
    Por ejemplo, buscar "algo para picar salado" en la colección 'teclas' podría devolver "Patatas Fritas".
    Para teclas, también verifica si están agotadas en la fecha actual. 

    Nota: No utilizar con una palabra sola sino con frases (Tercio alhambra, Cocacola zero, Cocacola normal, Refrescos de cola, etc.)
    Args:
        colecciones (List[str]): La lista de nombres de las colecciones donde buscar. Valores válidos: 'mesas', 'teclas'.
        texto_busqueda (str): La descripción o el texto a buscar.
        n_resultados (int): El número máximo de resultados a devolver por cada colección. Por defecto es 5.

    Returns:
        Dict[str, Any]: Un diccionario donde cada clave es el nombre de una colección y el valor es una lista
                        de los metadatos de los elementos más similares encontrados en esa colección.
                        Incluye una clave 'errores' si alguna colección no es válida o falla.
                        Para teclas, incluye también las sugerencias/extras con incremento > 0 y el campo
                        'agotada_hoy' que indica si la tecla está agotada en la fecha actual.
    """
    resultados_finales = {
        "resultados": {},
        "errores": []
    }
    send_tool_message(f"Buscando '{texto_busqueda}'...")

    try:
        # Crear el embedding una sola vez para todas las búsquedas
        query_embedding = embedding_model.encode(texto_busqueda).tolist()

        for coleccion in colecciones:
            if coleccion not in ["mesas", "teclas"]:
                resultados_finales["errores"].append(f"La colección '{coleccion}' no es válida. Use 'mesas' o 'teclas'.")
                continue

            try:
                target_collection = client.get_collection(name=coleccion)
                send_tool_message(f"Buscando en la colección '{coleccion}'...")
                results = target_collection.query(
                    query_embeddings=[query_embedding],
                    n_results=n_resultados
                )
                
                if results and results.get('metadatas') and results['metadatas'][0]:
                    teclas_encontradas = results['metadatas'][0]
                    
                    # Si es la colección de teclas, agregar las sugerencias/extras y verificar agotamiento
                    if coleccion == "teclas":
                        teclas_con_extras = []
                        
                        # Obtener fecha actual para verificar teclas agotadas
                        fecha_hoy = date.today().strftime("%Y/%m/%d")
                        
                        # Obtener IDs de teclas agotadas hoy
                        teclas_agotadas_hoy = set(
                            TeclasAgotadas.objects.filter(fecha=fecha_hoy).values_list('tecla_id', flat=True)
                        )
                        
                        for tecla_metadata in teclas_encontradas:
                            tecla_id = tecla_metadata.get('id')
                            
                            if tecla_id:
                                try:
                                    # Obtener la tecla para verificar si tiene padre
                                    tecla = Teclas.objects.select_related('parent_tecla').get(id=tecla_id)
                                    
                                    # IDs a buscar: tecla actual y padre si existe
                                    teclas_ids_buscar = [tecla_id]
                                    if tecla.parent_tecla:
                                        teclas_ids_buscar.append(tecla.parent_tecla.id)
                                    
                                    # Buscar sugerencias con incremento > 0
                                    sugerencias = list(Sugerencias.objects.filter(
                                        tecla_id__in=teclas_ids_buscar,
                                        incremento__gt=0
                                    ).values('sugerencia', 'incremento'))
                                    
                                    # Verificar si la tecla está agotada hoy
                                    agotada_hoy = tecla_id in teclas_agotadas_hoy
                                    
                                    # Agregar las sugerencias y estado de agotamiento al metadata
                                    tecla_con_extras = tecla_metadata.copy()
                                    tecla_con_extras['sugerencias'] = sugerencias
                                    tecla_con_extras['hay_existencias'] = 0 if agotada_hoy else 1
                                    teclas_con_extras.append(tecla_con_extras)
                                    
                                except Teclas.DoesNotExist:
                                    # Si la tecla no existe, mantener el metadata original
                                    tecla_con_extras = tecla_metadata.copy()
                                    tecla_con_extras['sugerencias'] = []
                                    tecla_con_extras['hay_existencias'] = 1  # Por defecto  agotada si no existe
                                    teclas_con_extras.append(tecla_con_extras)
                            else:
                                # Si no hay ID, mantener el metadata original
                                tecla_con_extras = tecla_metadata.copy()
                                tecla_con_extras['sugerencias'] = []
                                tecla_con_extras['hay_existencias'] = 1  # Por defecto no agotada si no hay ID
                                teclas_con_extras.append(tecla_con_extras)
                        
                        resultados_finales["resultados"][coleccion] = teclas_con_extras
                    else:
                        # Para mesas, mantener el resultado original
                        resultados_finales["resultados"][coleccion] = teclas_encontradas
                else:
                    resultados_finales["resultados"][coleccion] = []
            except Exception as e:
                error_message = f"Ocurrió un error al buscar en la colección '{coleccion}': {e}"
                send_tool_message(error_message)
                resultados_finales["errores"].append(error_message)
        
        send_tool_message("Búsqueda finalizada.")
        return resultados_finales

    except Exception as e:
        error_message = f"Ocurrió un error general durante la búsqueda: {e}"
        send_tool_message(error_message)
        return {"resultados": {}, "errores": [error_message]}


@tool
def verificar_existencia_por_id(
    coleccion: str,
    id_elemento: str
) -> Dict[str, Any]:
    """
    Verifica si un elemento con un ID específico existe en una colección de la base de datos vectorial.

    Esta herramienta es útil para comprobaciones rápidas antes de intentar añadir o modificar un elemento,
    evitando duplicados o errores.

    Args:
        coleccion (str): El nombre de la colección donde buscar. Valores válidos: 'mesas', 'teclas'.
        id_elemento (str): El ID del elemento a verificar. Debe ser una cadena de texto.

    Returns:
        Dict[str, Any]: Un diccionario que indica si el elemento existe.
                        Si existe, devuelve `{"existe": True, "metadata": {...}}`.
                        Si no existe, devuelve `{"existe": False, "mensaje": "..."}`.
                        Si hay un error (ej. colección no válida), devuelve `{"error": "mensaje"}`.
    """
    if coleccion not in ["mesas", "teclas"]:
        return {"error": f"La colección '{coleccion}' no es válida. Use 'mesas' o 'teclas'."}

    try:
        target_collection = client.get_collection(name=coleccion)
        
        # El método get es eficiente para buscar por ID
        resultado = target_collection.get(ids=[str(id_elemento)])
        
        if resultado and resultado.get('ids'):
            metadata = resultado['metadatas'][0] if resultado.get('metadatas') else {}
            return {"existe": True, "metadata": metadata}
        else:
            return {"existe": False, "mensaje": f"El elemento con ID '{id_elemento}' no se encontró en la colección '{coleccion}'."}

    except Exception as e:
        return {"error": f"Ocurrió un error al verificar la existencia en la colección '{coleccion}': {e}"}


@tool
@db_connection_handler
def actualizar_o_crear_registros_chromadb(elementos: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    Actualiza o crea registros específicos en las colecciones de ChromaDB ('mesas', 'teclas').

    Esta herramienta recibe una lista de elementos, cada uno con su colección y ID. Para cada elemento,
    busca los datos más recientes en la base de datos de Django, genera un nuevo embedding y lo
    inserta o actualiza (upsert) en la base de datos vectorial. Es ideal para sincronizar
    cambios individuales sin tener que repoblar toda la base de datos.

    Args:
        elementos (List[Dict[str, Any]]): Una lista de diccionarios. Cada diccionario debe contener:
            - "coleccion" (str): El nombre de la colección ('mesas', 'teclas').
            - "id_elemento" (int): El ID del elemento a actualizar o crear.
            Ejemplo: [{"coleccion": "teclas", "id_elemento": 101}, {"coleccion": "mesas", "id_elemento": 5}]

    Returns:
        List[Dict[str, Any]]: Una lista de diccionarios con el resultado de cada operación.
                              Ej: [{"id_elemento": 101, "coleccion": "teclas", "status": "success", "accion": "upserted"}]
    """
    resultados = []
    send_tool_message(f"Iniciando actualización/creación de {len(elementos)} registros...")

    for elemento in elementos:
        coleccion = elemento.get("coleccion")
        id_elemento = elemento.get("id_elemento")

        if not coleccion or not id_elemento:
            resultados.append({"error": "Faltan 'coleccion' o 'id_elemento'", "elemento": elemento})
            continue

        try:
            target_collection = client.get_or_create_collection(name=coleccion)
            metadata = {}
            content_to_embed = ""

            if coleccion == "mesas":
                mesa = Mesas.objects.prefetch_related('mesaszona_set__zona').get(id=id_elemento)
                tarifa = 1
                mesaszona = mesa.mesaszona_set.first()
                if mesaszona and mesaszona.zona:
                    tarifa = mesaszona.zona.tarifa
                metadata = {"id": mesa.id, "nombre": mesa.nombre, "tarifa": int(tarifa)}
                content_to_embed = mesa.nombre

            elif coleccion == "teclas":
                tecla = Teclas.objects.select_related('parent_tecla').get(id=id_elemento, tipo="SP")
                
                # Limpiar datos para evitar nulos
                nombre_limpio = tecla.nombre or ""
                descripcion_limpia = tecla.descripcion_r or ""
                descripcion_t_limpia = tecla.descripcion_t or ""
                tag_limpio = tecla.tag or ""
                p1_limpio = float(tecla.p1 or 0.0)
                p2_limpio = float(tecla.p2 or 0.0)
                
                # Crear descripción según las reglas especificadas
                if not descripcion_limpia:  # Si descripcion_r es None o ""
                    if tecla.parent_tecla:  # Si tiene parent_tecla
                        nombre_padre = tecla.parent_tecla.nombre or ""
                        descripcion_final = f"{nombre_padre} {nombre_limpio}".strip()
                    else:  # Si no tiene parent_tecla
                        descripcion_final = nombre_limpio
                else:
                    descripcion_final = descripcion_limpia
                
                # Crear descripcion_t según las reglas especificadas
                if not descripcion_t_limpia:  # Si descripcion_t es None o ""
                    if tecla.parent_tecla:  # Si tiene parent_tecla
                        descripcion_t_final = tecla.parent_tecla.nombre or ""
                    else:  # Si no tiene parent_tecla
                        descripcion_t_final = nombre_limpio
                else:
                    descripcion_t_final = descripcion_t_limpia
                
                # Crear contenido para embedding (sin usar el campo nombre)
                content_to_embed = f"Tecla: {descripcion_final}. Tags: {tag_limpio}"
                metadata = {
                    "id": tecla.id, 
                    "p1": p1_limpio,
                    "p2": p2_limpio, 
                    "descripcion": descripcion_final,
                    "descripcion_t": descripcion_t_final,
                    "tag": tag_limpio,
                }

            else:
                resultados.append({"id_elemento": id_elemento, "coleccion": coleccion, "status": "error", "mensaje": f"Colección '{coleccion}' no válida."})
                continue

            embedding = embedding_model.encode(content_to_embed).tolist()
            target_collection.upsert(ids=[str(id_elemento)], embeddings=[embedding], metadatas=metadata)
            
            send_tool_message(f"Registro {id_elemento} en '{coleccion}' procesado.")
            resultados.append({"id_elemento": id_elemento, "coleccion": coleccion, "status": "success", "accion": "upserted"})

        except (Mesas.DoesNotExist, Teclas.DoesNotExist):
            mensaje = f"Elemento con ID {id_elemento} no encontrado en la BD de Django para la colección '{coleccion}'."
            send_tool_message(mensaje)
            logger.error(mensaje)
            resultados.append({"id_elemento": id_elemento, "coleccion": coleccion, "status": "error", "mensaje": mensaje})
        except Exception as e:
            mensaje = f"Error procesando elemento {id_elemento} en '{coleccion}': {e}"
            send_tool_message(mensaje)
            logger.error(f"{mensaje}\n{traceback.format_exc()}")
            resultados.append({"id_elemento": id_elemento, "coleccion": coleccion, "status": "error", "mensaje": mensaje})

    send_tool_message("Actualización/creación de registros finalizada.")
    return resultados


@tool
def eliminar_registros_chromadb(
    elementos: List[Dict[str, Any]]
) -> List[Dict[str, Any]]:
    """
    Elimina registros específicos de las colecciones de ChromaDB ('mesas', 'teclas') por su ID.

    Esta herramienta es útil para limpiar la base de datos vectorial, por ejemplo, eliminando
    registros de teclas que ya no existen en la base de datos principal de Django.

    Args:
        elementos (List[Dict[str, Any]]): Una lista de diccionarios. Cada diccionario debe contener:
            - "coleccion" (str): El nombre de la colección ('mesas', 'teclas').
            - "id_elemento" (int): El ID del elemento a eliminar.
            Ejemplo: [{"coleccion": "teclas", "id_elemento": 101}, {"coleccion": "mesas", "id_elemento": 5}]

    Returns:
        List[Dict[str, Any]]: Una lista de diccionarios con el resultado de cada operación de borrado.
                              Ej: [{"id_elemento": 101, "coleccion": "teclas", "status": "success", "accion": "deleted"}]
    """
    resultados = []
    send_tool_message(f"Iniciando eliminación de {len(elementos)} registros...")

    for elemento in elementos:
        coleccion = elemento.get("coleccion")
        id_elemento = elemento.get("id_elemento")

        if not coleccion or not id_elemento:
            resultados.append({"error": "Faltan 'coleccion' o 'id_elemento'", "elemento": elemento})
            continue

        try:
            target_collection = client.get_collection(name=coleccion)
            target_collection.delete(ids=[str(id_elemento)])

            send_tool_message(f"Registro {id_elemento} en '{coleccion}' eliminado.")
            resultados.append({"id_elemento": id_elemento, "coleccion": coleccion, "status": "success", "accion": "deleted"})

        except Exception as e:
            mensaje = f"Error eliminando elemento {id_elemento} en '{coleccion}': {e}"
            send_tool_message(mensaje)
            logger.error(f"{mensaje}\n{traceback.format_exc()}")
            resultados.append({"id_elemento": id_elemento, "coleccion": coleccion, "status": "error", "mensaje": mensaje})

    return resultados


tools = [
    crear_y_poblar_base_de_datos_tpv,
    buscar_por_similitud_tpv,
    verificar_existencia_por_id,
    actualizar_o_crear_registros_chromadb,
    eliminar_registros_chromadb,
]