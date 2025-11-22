from typing import List, Dict
from uuid import uuid4
from gestion.models.pedidos import Lineaspedido
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.infmesa import Infmesa
from gestion.models.mesas import Mesas
 # Agregar la línea a la mesa destino
from datetime import datetime                       
from comunicacion.tools import comunicar_cambios_devices
       
from langchain_core.tools import tool
from gestion.tools.config_logs import log_debug_chatbot as logger
from chatbot.decorators.db_connection_manager import db_connection_handler
from ..comandas.embeding_teclas_y_mesas import buscar_por_similitud_tpv
from ..mesas_funcs import get_mesa_id
from .ventas_funcs import get_pedidos_mesa


from ...utilidades.ws_sender import (
    send_tool_message, 
    current_camarero_id_var
)

@tool
@db_connection_handler
def mover_mesa_completa(
    mesa_origen_id: int,
    mesa_destino_id: int,
    tipo_operacion: str = "cambiar"
):
    """
    Mueve o junta una mesa completa a otra mesa utilizando las operaciones nativas del sistema.
    
    Args:
        mesa_origen_id (int): ID de la mesa origen que se va a mover
        mesa_destino_id (int): ID de la mesa destino
        tipo_operacion (str): Tipo de operación:
            - "cambiar": Cambia o Mueve la mesa de lugar (intercambia si destino ocupado)
            - "juntar": Juntao o UNE el contenido de origen en destino y cierra origen
    
    Returns:
        dict: Resultado de la operación con:
            - status: 'success' o 'error'
            - message: Mensaje descriptivo
            - operacion: Tipo de operación realizada
            - mesa_origen: ID de mesa origen
            - mesa_destino: ID de mesa destino
    """
    send_tool_message(f"Moviendo mesa {mesa_origen_id} a mesa {mesa_destino_id} (operación: {tipo_operacion})...")
    
    try:
        # Validar que la mesa origen existe y está abierta
        mesa_origen = Mesasabiertas.objects.filter(mesa__id=mesa_origen_id).first()
        if not mesa_origen:
            return {
                'status': 'error',
                'message': f'No encuetro la mesa con id {mesa_origen_id}, asegurate que no sea el nombre',
                'operacion': tipo_operacion,
                'mesa_origen': mesa_origen_id,
                'mesa_destino': mesa_destino_id
            }
        
        # Verificar si la mesa destino existe físicamente
        mesa_destino_fisica = Mesas.objects.filter(id=mesa_destino_id).first()
        if not mesa_destino_fisica:
            return {
                'status': 'error',
                'message': f'Mesa destino {mesa_destino_id} no existe en el sistema',
                'operacion': tipo_operacion,
                'mesa_origen': mesa_origen_id,
                'mesa_destino': mesa_destino_id
            }
        
        # Obtener nombres de las mesas para mensajes más claros
        mesa_origen_nombre = mesa_origen.mesa.nombre
        mesa_destino_nombre = mesa_destino_fisica.nombre
        
        # Verificar si la mesa destino está abierta
        mesa_destino = Mesasabiertas.objects.filter(mesa__id=mesa_destino_id).first()
        
        if tipo_operacion == "cambiar":
            # Usar el método estático para cambiar mesas
            Mesasabiertas.cambiar_mesas_abiertas(mesa_origen_id, mesa_destino_id)
            
            if mesa_destino:
                mensaje = f'Mesa "{mesa_origen_nombre}" intercambiada con mesa "{mesa_destino_nombre}"'
            else:
                mensaje = f'Mesa "{mesa_origen_nombre}" movida a "{mesa_destino_nombre}"'
                
        elif tipo_operacion == "juntar":
            if not mesa_destino:
                return {
                    'status': 'error',
                    'message': f'No se puede juntar: mesa destino "{mesa_destino_nombre}" no está abierta',
                    'operacion': tipo_operacion,
                    'mesa_origen': mesa_origen_id,
                    'mesa_destino': mesa_destino_id
                }
            
            # Usar el método estático para juntar mesas
            Mesasabiertas.juntar_mesas_abiertas(mesa_origen_id, mesa_destino_id)
            mensaje = f'Mesa "{mesa_origen_nombre}" juntada con mesa "{mesa_destino_nombre}". Mesa origen cerrada.'
            
        else:
            return {
                'status': 'error',
                'message': f'Tipo de operación "{tipo_operacion}" no válido. Use "cambiar" o "juntar"',
                'operacion': tipo_operacion,
                'mesa_origen': mesa_origen_id,
                'mesa_destino': mesa_destino_id
            }
        
        return {
            'status': 'success',
            'message': mensaje,
            'operacion': tipo_operacion,
            'mesa_origen': mesa_origen_id,
            'mesa_destino': mesa_destino_id
        }
        
    except Exception as e:
        logger.error(f"Error en mover_mesa_completa: {e}", exc_info=True)
        return {
            'status': 'error',
            'message': f'Error interno: {str(e)}',
            'operacion': tipo_operacion,
            'mesa_origen': mesa_origen_id,
            'mesa_destino': mesa_destino_id
        }


@tool
@db_connection_handler
def mover_lineas_entre_mesas(
    lineas_data: List[Dict]
):
    """
    Mueve líneas específicas de una mesa a otra buscando por descripción.
    Para cada línea encontrada, la borra de la mesa origen y la agrega a la mesa destino.
    
    Args:
        lineas_data (list): Lista de diccionarios con datos de las líneas a mover.
            Cada diccionario debe contener:
            - mesa_origen_id (int): ID de la mesa origen
            - mesa_destino_id (int): ID de la mesa destino
            - descripcion (str): Descripción de la línea a buscar y mover
            - cantidad (int, opcional): Cantidad de líneas a mover (por defecto 1)
            - mover_todas (bool, opcional): Si es True, mueve TODAS las líneas que coincidan
    
    Note:
        - Si la mesa destino no está abierta, se abre automáticamente
        - Se crean nuevos pedidos en la mesa destino preservando la información original
    
    Returns:
        list: Lista de resultados para cada línea procesada con:
            - status: 'success', 'error' o 'not_found'
            - mesa_origen: ID de mesa origen
            - mesa_destino: ID de mesa destino
            - descripcion: Descripción buscada
            - lineas_movidas: Número de líneas movidas exitosamente
            - message: Mensaje descriptivo del resultado
    """
    send_tool_message(f"Moviendo {len(lineas_data)} tipos de líneas entre mesas...")
    
    results = []


    camarero_id = current_camarero_id_var.get()
    
    for linea_data in lineas_data:
        try:
            # Validar datos requeridos
            mesa_origen_id = linea_data.get('mesa_origen_id')
            mesa_destino_id = linea_data.get('mesa_destino_id')
            descripcion = linea_data.get('descripcion')
            cantidad = linea_data.get('cantidad', 1)
            mover_todas = linea_data.get('mover_todas', False)
            
            if not mesa_origen_id or not mesa_destino_id or not descripcion:
                results.append({
                    'status': 'error',
                    'mesa_origen': mesa_origen_id,
                    'mesa_destino': mesa_destino_id,
                    'descripcion': descripcion,
                    'lineas_movidas': 0,
                    'message': 'Datos incompletos: mesa_origen_id, mesa_destino_id y descripcion son requeridos',
                })
                continue
            
            if mesa_origen_id == mesa_destino_id:
                results.append({
                    'status': 'error',
                    'mesa_origen': mesa_origen_id,
                    'mesa_destino': mesa_destino_id,
                    'descripcion': descripcion,
                    'lineas_movidas': 0,
                    'message': 'Mesa origen y destino no pueden ser la misma',
                })
                continue
            
            # Buscar la mesa origen
            mesa_origen = Mesasabiertas.objects.filter(mesa__id=mesa_origen_id).first()
            if not mesa_origen:
                results.append({
                    'status': 'error',
                    'mesa_origen': mesa_origen_id,
                    'mesa_destino': mesa_destino_id,
                    'descripcion': descripcion,
                    'lineas_movidas': 0,
                    'message': f'No encuetro la mesa origen con id {mesa_origen_id}, asegurate que no sea el nombre',
                })
                continue
            
            # Verificar que la mesa destino existe físicamente
            mesa_destino_fisica = Mesas.objects.filter(id=mesa_destino_id).first()
            if not mesa_destino_fisica:
                results.append({
                    'status': 'error',
                    'mesa_origen': mesa_origen_id,
                    'mesa_destino': mesa_destino_id,
                    'descripcion': descripcion,
                    'lineas_movidas': 0,
                    'message': f'No encuetro la mesa destino con id {mesa_origen_id}, asegurate que no sea el nombre',
                })
                continue
            
            # Buscar líneas en la mesa origen que coincidan con la descripción
            lineas_encontradas = mesa_origen.infmesa.lineaspedido_set.filter(
                descripcion__icontains=descripcion,
                estado__in=['P', 'M', "R"]
            ).order_by('-id')

            if not lineas_encontradas.exists():
                results.append({
                    'status': 'not_found',
                    'mesa_origen': mesa_origen_id,
                    'mesa_destino': mesa_destino_id,
                    'descripcion': descripcion,
                    'lineas_movidas': 0,
                    'message': f'No se encontraron líneas con descripción "{descripcion}" en mesa origen {mesa_origen.mesa.nombre} (ID: {mesa_origen_id})',
                })
                continue
            
               
            # Buscar o crear la mesa destino abierta
            mesa_destino = Mesasabiertas.objects.filter(mesa__id=mesa_destino_id).first()
            if not mesa_destino:
                # Crear nueva mesa abierta
                uid = f"{mesa_destino_fisica.id}_{str(uuid4())}"
                nuevo_infmesa = Infmesa.objects.create(
                    id=uid,
                    camarero_id=camarero_id,
                    fecha=datetime.now().strftime("%Y/%m/%d"),
                    hora=datetime.now().strftime("%H:%M"),
                )
                mesa_destino = Mesasabiertas.objects.create(
                    infmesa=nuevo_infmesa,
                    mesa=mesa_destino_fisica
                )
                comunicar_cambios_devices("md", "mesasabiertas", {"id": mesa_destino.id, "abierta": 1, "num": 0 })
            else:
                nuevo_infmesa = mesa_destino.infmesa
            
            # Procesar cada línea a mover
            # --- PASO QUE FALTABA: Modificar los objetos en memoria ---
            # Determinar cuántas líneas mover
            if mover_todas:
                lineas_a_mover_qs = lineas_encontradas
            else:
                ids_a_mover = list(lineas_encontradas.values_list('id', flat=True)[:cantidad])
                lineas_a_mover_qs = Lineaspedido.objects.filter(id__in=ids_a_mover)

            if lineas_a_mover_qs.exists():
                # 1. Mover todas las líneas con UNA SOLA CONSULTA a la base de datos
                lineas_movidas_exitosas = lineas_a_mover_qs.update(infmesa=nuevo_infmesa)

                # 2. Serializar y comunicar después de la actualización
                # Obtenemos los datos frescos de las líneas ya movidas para la comunicación
                lineas_modificadas_serializadas = [
                    linea.serialize() for linea in Lineaspedido.objects.filter(infmesa=nuevo_infmesa, id__in=lineas_a_mover_qs.values('id'))
                ]
                comunicar_cambios_devices("md", "teclas", lineas_modificadas_serializadas)
            else:
                lineas_movidas_exitosas = 0
                            # Actualizar composición de artículos en ambas mesas
            try:
                mesa_origen.infmesa.componer_articulos()
                mesa_origen.infmesa.unir_en_grupos()
                mesa_destino.infmesa.componer_articulos()
                mesa_destino.infmesa.unir_en_grupos()
            except Exception as e:
                pass
            
            # Verificar si la mesa origen se quedó vacía
            lineas_restantes_origen = mesa_origen.infmesa.lineaspedido_set.filter(
                estado__in=['P', 'M']
            ).count()
            
       
            if lineas_restantes_origen == 0:
               mensaje_final = f'Mesa origen {mesa_origen.mesa.nombre} cerrada automáticamente por que esta vacia.'
               mesa_origen.delete()
            else:
                mensaje_final = f'Mesa origen {mesa_origen.mesa.nombre} aún tiene líneas pendientes {lineas_restantes_origen}.'
               
            results.append({
                'status': 'success',
                'mesa_origen': mesa_origen_id,
                'mesa_destino': mesa_destino_id,
                'descripcion': descripcion,
                'lineas_movidas': lineas_movidas_exitosas,
                'message': mensaje_final,
            })
            
        except Exception as e:
            logger.error(f"Error en mover_lineas_entre_mesas: {e}", exc_info=True)
            results.append({
                'status': 'error',
                'mesa_origen': linea_data.get('mesa_origen_id', 'desconocido'),
                'mesa_destino': linea_data.get('mesa_destino_id', 'desconocido'),
                'descripcion': linea_data.get('descripcion', 'desconocida'),
                'lineas_movidas': 0,
                'message': f'Error interno en mover_lineas_entre_mesas: {str(e)}',
            })
    
       
    return results


tools = [
    get_pedidos_mesa,
    mover_mesa_completa,
    mover_lineas_entre_mesas,
    buscar_por_similitud_tpv,
    get_mesa_id,
]
