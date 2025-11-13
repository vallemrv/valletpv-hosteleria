from typing import Dict, Any, List, Union
from datetime import datetime
from django.conf import settings
from langchain_core.tools import tool
from chatbot.decorators.db_connection_manager import db_connection_handler
from chatbot.utilidades.ws_sender import send_tool_message, current_camarero_id_var
from gestion.tools.config_logs import log_debug_chatbot as logger
from gestion.models.teclados import Teclas
from chatbot.tools.articulos_funcs.teclados_funcs import \
    gestionar_existencias_teclas
from .embeding_teclas_y_mesas import buscar_por_similitud_tpv, \
    actualizar_o_crear_registros_chromadb
from .pedidos_funcs import buscar_teclas_directa
import os

    

@tool
@db_connection_handler
def agregar_tags_teclas(
    teclas_tags: List[Dict[str, Union[int, str]]]
) -> Dict[str, Any]:
    """
    Agrega o modifica únicamente el campo 'tag' de múltiples teclas.

    Esta herramienta está específicamente diseñada para gestionar solo los tags de las teclas.
    Sirve para agregar nuevos sinónimos o etiquetas para mejorar la búsqueda de la tecla en futuras iteraciones.

    Args:
        teclas_tags: Lista de diccionarios con la estructura:
            [{"tecla_id": 1, "tag": "nuevo_tag"}, {"tecla_id": 2, "tag": "otro_tag"}, ...]
            
            Campos requeridos:
            - tecla_id: ID de la tecla a modificar (int)
            - tag: Nuevo valor del tag (str). Puede ser una cadena vacía "" para limpiar el tag
    
    Returns:
        Diccionario con:
        - success: True/False
        - mensaje: Mensaje resumen de la operación
        - resultados: Lista de resultados para cada tecla procesada con:
            - status: "success" o "error"
            - message: Mensaje descriptivo del resultado
            - tecla_id: ID de la tecla procesada
            - tecla_nombre: Nombre de la tecla (si existe)
            - tag_anterior: Valor anterior del tag
            - tag_nuevo: Nuevo valor del tag asignado
    """
    try:
        if not teclas_tags:
            return {
                "success": False,
                "mensaje": "No se proporcionaron datos de teclas para agregar tags",
                "resultados": []
            }
        
        resultados = []
        exitosas = 0
        
        for item in teclas_tags:
            tecla_id = item.get("tecla_id")
            nuevo_tag = item.get("tag")
            
            if not tecla_id:
                resultados.append({
                    "status": "error",
                    "message": "tecla_id es requerido",
                    "tecla_id": None,
                    "tecla_nombre": None,
                    "tag_anterior": None,
                    "tag_nuevo": None
                })
                continue
            
            if nuevo_tag is None:
                resultados.append({
                    "status": "error",
                    "message": f"tag es requerido para tecla ID {tecla_id}",
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "tag_anterior": None,
                    "tag_nuevo": None
                })
                continue
            
            try:
                # Obtener la tecla actual para guardar el tag anterior
                tecla_actual = Teclas.objects.get(id=tecla_id)
                tag_anterior = tecla_actual.tag or ""
                
                if nuevo_tag == "":
                    # Si el nuevo tag es una cadena vacía, limpiar el tag actual
                    tecla_actual.tag = ""
                else:
                    tecla_actual.tag = tag_anterior + " " + nuevo_tag.strip() 



                tecla_actual.save()

                actualizar_o_crear_registros_chromadb.func([
                    {"coleccion": "teclas", "id_elemento": tecla_actual.id}
                ])

                exitosas += 1

                resultados.append({
                    "status": "success",
                    "message": f"Tag actualizado correctamente para tecla ID {tecla_id}",
                    "tecla_id": tecla_id,
                    "tecla_nombre": tecla_actual.descripcion_r if tecla_actual else None,
                    "tag_anterior": tag_anterior,
                    "tag_nuevo": tecla_actual.tag
                })
                
            except Teclas.DoesNotExist:
                resultados.append({
                    "status": "error",
                    "message": f"Tecla con ID {tecla_id} no encontrada",
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "tag_anterior": None,
                    "tag_nuevo": None
                })
                
            except Exception as e:
                error_msg = f"Error procesando tecla ID {tecla_id}: {str(e)}"
                logger.error(error_msg, exc_info=True)
                resultados.append({
                    "status": "error",
                    "message": error_msg,
                    "tecla_id": tecla_id,
                    "tecla_nombre": None,
                    "tag_anterior": None,
                    "tag_nuevo": None
                })
        
        # Mensaje resumen
        mensaje_resumen = f"Operación completada: {exitosas}/{len(resultados)} tags actualizados exitosamente"
        send_tool_message(mensaje_resumen)
        
        return {
            "success": exitosas > 0,
            "mensaje": mensaje_resumen,
            "teclas_procesadas": len(resultados),
            "teclas_exitosas": exitosas,
            "resultados": resultados
        }
        
    except Exception as e:
        error_msg = f"Error general agregando tags: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(f"❌ {error_msg}")
        return {
            "success": False,
            "mensaje": error_msg,
            "resultados": []
        }



@tool
@db_connection_handler
def gestionar_memoria_camarero(
    accion: str,
    contenido: str = ""
) -> Dict[str, Any]:
    """
    Gestiona la memoria personal del camarero para recordar preferencias, notas e instrucciones.
    
    Esta herramienta permite al agente de IA guardar, recuperar, editar y borrar recuerdos específicos
    del camarero actual. Los recuerdos se almacenan en texto plano y pueden incluir preferencias
    de clientes, notas especiales, instrucciones personalizadas, etc.
    
    Args:
        accion: Tipo de operación a realizar:
            - "leer": Recupera todos los recuerdos guardados
            - "agregar": Añade un nuevo recuerdo al final del archivo
            - "reescribir": Reemplaza completamente la memoria con el nuevo contenido
            - "borrar": Elimina completamente la memoria del camarero
        contenido: Texto a agregar o contenido completo para reescribir (requerido para "agregar" y "reescribir")
            
    Returns:
        Diccionario con:
        - success: True/False
        - mensaje: Mensaje descriptivo del resultado
        - memoria_actual: Contenido actual de la memoria (para "leer" y después de modificaciones)
        - archivo_memoria: Ruta del archivo de memoria
        - camarero_id: ID del camarero actual
    """
    try:
        
        
        # Obtener camarero_id del contexto
        camarero_id = current_camarero_id_var.get()
        
        if not camarero_id:
            return {
                "success": False,
                "mensaje": "Error: No se pudo identificar el camarero para gestionar la memoria",
                "memoria_actual": "",
                "archivo_memoria": "",
                "camarero_id": None
            }
        
        # Construir ruta del archivo de memoria
        memoria_dir = os.path.join(settings.MEDIA_ROOT, "memoria")
        archivo_memoria = os.path.join(memoria_dir, f"camarero_{camarero_id}.dat")
        
        # Crear directorio si no existe
        os.makedirs(memoria_dir, exist_ok=True)
        
        # Validar acción
        if accion not in ["leer", "agregar", "reescribir", "borrar"]:
            return {
                "success": False,
                "mensaje": f"Acción '{accion}' no válida. Use: 'leer', 'agregar', 'reescribir' o 'borrar'",
                "memoria_actual": "",
                "archivo_memoria": archivo_memoria,
                "camarero_id": camarero_id
            }
        
        # Verificar contenido para acciones que lo requieren
        if accion in ["agregar", "reescribir"] and not contenido.strip():
            return {
                "success": False,
                "mensaje": f"La acción '{accion}' requiere contenido",
                "memoria_actual": "",
                "archivo_memoria": archivo_memoria,
                "camarero_id": camarero_id
            }
        
        # Leer memoria actual si existe
        memoria_actual = ""
        if os.path.exists(archivo_memoria):
            try:
                with open(archivo_memoria, 'r', encoding='utf-8') as f:
                    memoria_actual = f.read()
            except Exception as e:
                logger.warning(f"Error leyendo archivo de memoria {archivo_memoria}: {str(e)}")
                memoria_actual = ""
        
        # Ejecutar acción
        if accion == "leer":
            if memoria_actual:
                send_tool_message(f"📝 Memoria del camarero {camarero_id} recuperada ({len(memoria_actual)} caracteres)")
                return {
                    "success": True,
                    "mensaje": f"Memoria recuperada exitosamente ({len(memoria_actual)} caracteres)",
                    "memoria_actual": memoria_actual,
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }
            else:
                send_tool_message(f"📝 No hay memoria guardada para el camarero {camarero_id}")
                return {
                    "success": True,
                    "mensaje": "No hay memoria guardada para este camarero",
                    "memoria_actual": "",
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }
        
        elif accion == "agregar":
            # Agregar timestamp y nuevo contenido
            
            timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            nuevo_contenido = f"\n--- Agregado el {timestamp} ---\n{contenido.strip()}\n"
            
            memoria_nueva = memoria_actual + nuevo_contenido
            
            try:
                with open(archivo_memoria, 'w', encoding='utf-8') as f:
                    f.write(memoria_nueva)
                
                send_tool_message(f"📝 Recuerdo agregado a la memoria del camarero {camarero_id}")
                return {
                    "success": True,
                    "mensaje": f"Recuerdo agregado exitosamente ({len(nuevo_contenido)} caracteres nuevos)",
                    "memoria_actual": memoria_nueva,
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }
            except Exception as e:
                error_msg = f"Error escribiendo en archivo de memoria: {str(e)}"
                logger.error(error_msg, exc_info=True)
                return {
                    "success": False,
                    "mensaje": error_msg,
                    "memoria_actual": memoria_actual,
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }
        
        elif accion == "reescribir":
            # Reemplazar completamente la memoria
            
            timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            memoria_nueva = f"--- Memoria actualizada el {timestamp} ---\n{contenido.strip()}\n"
            
            try:
                with open(archivo_memoria, 'w', encoding='utf-8') as f:
                    f.write(memoria_nueva)
                
                send_tool_message(f"📝 Memoria del camarero {camarero_id} reescrita completamente")
                return {
                    "success": True,
                    "mensaje": f"Memoria reescrita exitosamente ({len(memoria_nueva)} caracteres totales)",
                    "memoria_actual": memoria_nueva,
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }
            except Exception as e:
                error_msg = f"Error reescribiendo archivo de memoria: {str(e)}"
                logger.error(error_msg, exc_info=True)
                return {
                    "success": False,
                    "mensaje": error_msg,
                    "memoria_actual": memoria_actual,
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }
        
        elif accion == "borrar":
            if os.path.exists(archivo_memoria):
                try:
                    os.remove(archivo_memoria)
                    send_tool_message(f"📝 Memoria del camarero {camarero_id} borrada exitosamente")
                    return {
                        "success": True,
                        "mensaje": "Memoria borrada exitosamente",
                        "memoria_actual": "",
                        "archivo_memoria": archivo_memoria,
                        "camarero_id": camarero_id
                    }
                except Exception as e:
                    error_msg = f"Error borrando archivo de memoria: {str(e)}"
                    logger.error(error_msg, exc_info=True)
                    return {
                        "success": False,
                        "mensaje": error_msg,
                        "memoria_actual": memoria_actual,
                        "archivo_memoria": archivo_memoria,
                        "camarero_id": camarero_id
                    }
            else:
                send_tool_message(f"📝 No hay memoria que borrar para el camarero {camarero_id}")
                return {
                    "success": True,
                    "mensaje": "No había memoria para borrar",
                    "memoria_actual": "",
                    "archivo_memoria": archivo_memoria,
                    "camarero_id": camarero_id
                }

    except Exception as e:
        error_msg = f"Error gestionando memoria del camarero: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(f"❌ {error_msg}")
        return {
            "success": False,
            "mensaje": error_msg,
            "memoria_actual": "",
            "archivo_memoria": "",
            "camarero_id": None
        }



tools = [
    buscar_teclas_directa,
    buscar_por_similitud_tpv,
    gestionar_memoria_camarero,
    gestionar_existencias_teclas,
    agregar_tags_teclas,
]