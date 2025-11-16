import uuid
from datetime import date
from typing import Any, Dict, List



from django.db.models import  Value
from django.db.models.functions import Concat
from langchain_core.tools import tool

from api.tools.impresion import imprimir_pedido
from api.tools.smart_receptor import enviar_pedido_smart_receptor
from chatbot.decorators.db_connection_manager import db_connection_handler



from gestion.models.pedidos import ESTADO_CHOICES, Pedidos
from gestion.models.teclados import Sugerencias, Teclas, TeclasAgotadas
from gestion.tools.config_logs import log_debug_chatbot as logger

from ...utilidades.ws_sender import current_camarero_id_var, send_tool_message
from ..comandas.embeding_teclas_y_mesas import buscar_por_similitud_tpv
from ..articulos_funcs.teclados_funcs import _construir_filtro_texto
from ..mesas_funcs import get_mesa_id



@tool
@db_connection_handler
def crear_pedido_cliente(
    idm: int,
    lineas: List[Dict[str, Any]]
) -> Dict[str, Any]:
    """
    Crea un pedido completo en la base de datos con las líneas ya procesadas.
    
    Esta función recibe las líneas de pedido ya procesadas y validadas por el agente
    y las inserta en la base de datos usando Pedidos.agregar_nuevas_lineas().
    
    Args:
        idm: ID de la mesa donde se va a crear el pedido
        lineas: Lista de líneas de pedido, cada una con:
            - IDArt: ID del artículo (original de la tecla)
            - Can: Cantidad (inferida del contexto)
            - Descripcion: Descripción (Original del la tecla) + extras (si las hay originales de la tecla) +  modificaciones (inferidos del contexto, mensajes, sugerencias, recordatorios, notas, etc.)
            - Precio: Precio unitario del producto (Se crea de la tecla.p1 o tecla.p2 segun la tarifa de la mesa.tarifa)
            - descripcion_t: Descripción para ticket (original de la tecla)

    Returns:
        Dict con el resultado del pedido:
        - success: True/False
        - pedido_id: ID del pedido creado (si exitoso)
        - uid_pedido: UID único generado para este pedido
        - mensaje: Mensaje descriptivo del resultado
        - total: Total del pedido
        - lineas_creadas: Número de líneas creadas
    """
    try:
        mensajes_proceso = []
        
        # Obtener camarero_id del contexto
        camarero_id = current_camarero_id_var.get()
        
        if not camarero_id:
            return {
                "success": False,
                "mensaje": "Error: No se pudo identificar el camarero",
                "total": 0,
                "lineas_creadas": 0
            }
        
        if not idm or idm <= 0:
            return {
                "success": False,
                "mensaje": "Error: ID de mesa inválido",
                "total": 0,
                "lineas_creadas": 0
            }
        
        if not lineas or len(lineas) == 0:
            return {
                "success": False,
                "mensaje": "Error: No se proporcionaron líneas de pedido",
                "total": 0,
                "lineas_creadas": 0
            }
        
        # Generar un UID único para este pedido específico
        uid_pedido = str(uuid.uuid4())
        
        mensajes_proceso.append(f"🛒 Creando pedido para mesa {idm}...")
        mensajes_proceso.append(f"🆔 UID pedido generado: {uid_pedido}")
        
        # Validar estructura de las líneas
        lineas_validadas = []
        total_precio = 0
        productos_info = []
        
        for i, linea in enumerate(lineas):
            try:
                # Validar campos requeridos
                required_fields = ['IDArt', 'Can', 'Descripcion', 'Precio', 'descripcion_t']
                for field in required_fields:
                    if field not in linea:
                        raise ValueError(f"Campo requerido '{field}' faltante en línea {i+1}")
                
                # Convertir y validar tipos
                linea_validada = {
                    "IDArt": int(linea['IDArt']),
                    "Can": int(linea['Can']),
                    "Descripcion": str(linea['Descripcion']),
                    "Precio": float(linea['Precio']),
                    "descripcion_t": str(linea['descripcion_t'])
                }
                
                # Validar valores
                if linea_validada['Can'] <= 0:
                    raise ValueError(f"Cantidad debe ser mayor a 0 en línea {i+1}")
                if linea_validada['Precio'] < 0:
                    raise ValueError(f"Precio no puede ser negativo en línea {i+1}")
                
                lineas_validadas.append(linea_validada)
                total_precio += linea_validada['Precio'] * linea_validada['Can']
                
                productos_info.append(f"📦 {linea_validada['Can']}x {linea_validada['descripcion_t']} - {linea_validada['Precio']}€ c/u")
                
            except (ValueError, TypeError, KeyError) as e:
                return {
                    "success": False,
                    "mensaje": f"Error validando línea {i+1}: {str(e)}",
                    "total": 0,
                    "lineas_creadas": 0
                }
        
        # Agregar información de productos y total
        mensajes_proceso.extend(productos_info)
        mensajes_proceso.append(f"💰 Total calculado: {total_precio:.2f}€")
        mensajes_proceso.append("💾 Creando pedido en la base de datos...")
        
        # Crear el pedido en la base de datos
        pedido_creado = Pedidos.agregar_nuevas_lineas(
            idm=idm,
            idc=camarero_id,
            lineas=lineas_validadas,
            uid_device=uid_pedido
        )
        
        if pedido_creado:
            mensajes_proceso.append(f"✅ ¡Pedido #{pedido_creado.id} creado exitosamente!")
            mensajes_proceso.append(f"🆔 UID del pedido: {uid_pedido}")
            imprimir_pedido(pedido_creado)
            enviar_pedido_smart_receptor(pedido_creado)
            
            # Enviar todos los mensajes concatenados
            send_tool_message("\n".join(mensajes_proceso))
            
            resultado = {
                "success": True,
                "pedido_id": pedido_creado.id,
                "uid_pedido": uid_pedido,
                "mensaje": f"✅ Pedido creado correctamente para mesa {idm}",
                "total": total_precio,
                "lineas_creadas": len(lineas_validadas),
                "mesa_id": idm,
                "productos": [f"{l['Can']}x {l['descripcion_t']}" for l in lineas_validadas]
            }
            
            return resultado
        else:
            send_tool_message("\n".join(mensajes_proceso) + "\n❌ Error al crear el pedido en la base de datos.")
            return {
                "success": False,
                "mensaje": "❌ Error al crear el pedido en la base de datos. Es posible que ya exista un pedido con este UID.",
                "total": 0,
                "lineas_creadas": 0
            }
    
    except Exception as e:
        error_msg = f"Error creando pedido: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(f"❌ {error_msg}")
        return {
            "success": False,
            "mensaje": error_msg,
            "total": 0,
            "lineas_creadas": 0
        }

@tool
@db_connection_handler
def buscar_teclas_directa(
    texto_busqueda: str
) -> List[Dict]:
    """
    Busca teclas por texto de forma directa y simplificada.

    Esta herramienta está específicamente diseñada para buscar teclas por nombre, descripción o tag (sinónimos y etiquetas),
    eliminando automáticamente preposiciones y palabras irrelevantes. Incluye extras/sugerencias de la tecla y su padre.
    También verifica si las teclas están agotadas en la fecha actual.

    Args:
        texto_busqueda: Texto a buscar en las teclas (se filtrarán preposiciones automáticamente)
            
    Returns:
        Array de teclas encontradas. Cada elemento contiene:
        {id, descripcion, descripcion_t, p1, p2, tag, extras, agotada_hoy}
        
        extras: Lista de sugerencias con incremento > 0 de la tecla y su padre (si existe)
        agotada_hoy: Boolean que indica si la tecla está agotada en la fecha actual
    """
    try:
         
        if not texto_busqueda or not texto_busqueda.strip():
            send_tool_message("❌ Debe proporcionar un texto para buscar")
            return []
        
        buscar_texto = texto_busqueda.strip()
        
        send_tool_message(f"🔍 Buscando teclas con texto: '{buscar_texto}'")
        
        # Construir query con descripción dinámica y filtrar solo teclas tipo SP
        query = Teclas.objects.filter(tipo="SP").select_related('parent_tecla').annotate(
            descripcion_dinamica=Concat(
                'parent_tecla__nombre', Value(' '), 'nombre'
            )
        )
        
        # Aplicar filtro de texto reutilizando la función centralizada
        query = _construir_filtro_texto(query, buscar_texto)
        
        teclas_encontradas = query.order_by('-orden', 'nombre').distinct()
        
        if not teclas_encontradas.exists():
            send_tool_message("❌ No se encontraron teclas que coincidan con la búsqueda")
            return []
        
        # Procesar resultados
        teclas_array = []
        
        # Obtener fecha actual para verificar teclas agotadas
        fecha_hoy = date.today().strftime("%Y/%m/%d")
        
        # Obtener IDs de teclas agotadas hoy
        teclas_agotadas_hoy = set(
            TeclasAgotadas.objects.filter(fecha=fecha_hoy).values_list('tecla_id', flat=True)
        )
        
        for tecla in teclas_encontradas:
            # Obtener valores básicos
            tecla_id = tecla.id
            descripcion_r = tecla.descripcion_r or ""
            descripcion_t = tecla.descripcion_t or ""
            p1 = float(tecla.p1)
            p2 = float(tecla.p2)
            tag = tecla.tag or ""
            
            # Verificar si la tecla está agotada hoy
            agotada_hoy = tecla_id in teclas_agotadas_hoy
            
            # Construir descripciones dinámicamente si están vacías
            if not descripcion_r or not descripcion_t:
                nombre_tecla = tecla.nombre
                nombre_padre = ""
                
                if tecla.parent_tecla:
                    nombre_padre = tecla.parent_tecla.nombre
                
                # Construir descripciones según las reglas
                if nombre_padre:  # Tiene padre
                    if not descripcion_r:
                        descripcion_r = f"{nombre_padre} {nombre_tecla}"
                    if not descripcion_t:
                        descripcion_t = nombre_padre
                else:  # No tiene padre
                    if not descripcion_r:
                        descripcion_r = nombre_tecla
                    if not descripcion_t:
                        descripcion_t = nombre_tecla
            
            # Obtener extras (sugerencias con incremento > 0)
            extras = []
            
            # IDs a buscar: tecla actual y padre si existe
            teclas_ids_buscar = [tecla_id]
            if tecla.parent_tecla:
                teclas_ids_buscar.append(tecla.parent_tecla.id)
            
            # Buscar sugerencias con incremento > 0
            sugerencias = Sugerencias.objects.filter(
                tecla_id__in=teclas_ids_buscar,
                incremento__gt=0
            ).values('sugerencia', 'incremento')
            
            for sug in sugerencias:
                extras.append({
                    "nombre": sug['sugerencia'],
                    "incremento": float(sug['incremento'])
                })
            
            # Agregar al array como diccionario
            teclas_array.append({
                "id": tecla_id,
                "descripcion": descripcion_r,
                "descripcion_t": descripcion_t,
                "p1": p1,
                "p2": p2,
                "tag": tag,
                "extras": extras,
                "hay_existencias": 0 if agotada_hoy else 1
            })
        
        total = len(teclas_array)
        teclas_agotadas_count = sum(1 for tecla in teclas_array if tecla['agotada_hoy'])
        
        mensaje = f"🔍 Encontradas {total} teclas para la búsqueda: '{buscar_texto}'"
        if teclas_agotadas_count > 0:
            mensaje += f" (⚠️ {teclas_agotadas_count} agotadas hoy)"
        
        send_tool_message(mensaje)
        
        # Mostrar resumen de teclas encontradas
        if teclas_array:
            resumen_teclas = []
            for tecla in teclas_array[:5]:  # Mostrar solo las primeras 5 en el mensaje
                desc = tecla['descripcion_t'] or f"Tecla ID {tecla['id']}"
                extras_count = len(tecla['extras'])
                extras_info = f" ({extras_count} extras)" if extras_count > 0 else ""
                agotada_info = " ⚠️ AGOTADA HOY" if tecla['agotada_hoy'] else ""
                resumen_teclas.append(f"• {desc} - P1: {tecla['p1']}€, P2: {tecla['p2']}€{extras_info}{agotada_info}")
            
            mensaje_teclas = "Teclas encontradas:\n" + "\n".join(resumen_teclas)
            if len(teclas_array) > 5:
                mensaje_teclas += f"\n... y {len(teclas_array) - 5} más"
            
            send_tool_message(mensaje_teclas)
        
        return teclas_array
            
    except Exception as e:
        error_msg = f"Error buscando teclas: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(f"❌ {error_msg}")
        return []



@tool
def buscar_id_producto(texto_busqueda: str) -> List[Dict[str, Any]]:
    """
    Busca un producto utilizando una estrategia de dos pasos: búsqueda directa y luego por similitud.

    Esta función primero intenta una búsqueda exacta con `buscar_teclas_directa`. Si no encuentra
    resultados, procede con una búsqueda por similitud semántica usando `buscar_por_similitud_tpv`.

    Args:
        texto_busqueda: El texto para buscar el producto.

    Returns:
        Una lista de diccionarios de los artículos encontrados, o una lista vacía si no se encuentra nada.
    """
    send_tool_message(f"🔎 Iniciando búsqueda de producto para: '{texto_busqueda}'")

    # 1. Búsqueda directa
    send_tool_message("1️⃣ Intentando búsqueda directa...")
    resultados_directos = buscar_teclas_directa.invoke({"texto_busqueda": texto_busqueda})
    if resultados_directos:
        send_tool_message("✅ Encontrado por búsqueda directa.")
        return resultados_directos

    # 2. Búsqueda por similitud
    send_tool_message("2️⃣ Búsqueda directa fallida. Intentando búsqueda por similitud...")
    resultados_similitud = buscar_por_similitud_tpv.invoke({
        "colecciones": ["teclas"],
        "texto_busqueda": texto_busqueda,
        "n_resultados": 5 # Devolvemos 5 resultados por similitud
    })
    
    if (resultados_similitud and resultados_similitud.get("resultados") and 
        "teclas" in resultados_similitud["resultados"] and 
        resultados_similitud["resultados"]["teclas"]):
        
        send_tool_message(f"✅ Encontrado por similitud.")
        # La función de similitud ya devuelve la información necesaria, incluyendo extras.
        return resultados_similitud["resultados"]["teclas"]

    send_tool_message("❌ No se encontró el artículo por ningún método de búsqueda.")
    return []



# Lista de herramientas específicas para pedidos
tools = [
    crear_pedido_cliente,
    get_mesa_id,
    buscar_teclas_directa,
    buscar_por_similitud_tpv
]
