from collections import defaultdict
from django.db.models import Count, Sum
from langchain_core.tools import tool

from chatbot.decorators.db_connection_manager import db_connection_handler
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.pedidos import Lineaspedido
from gestion.tools.config_logs import log_debug_chatbot as logger

from ...utilidades.ws_sender import send_tool_message

from ..comandas.embeding_teclas_y_mesas import buscar_por_similitud_tpv
from ..mesas_funcs import get_mesa_id


@tool
@db_connection_handler
def get_pedidos_mesa(id_mesa: int) -> str:
    """
    Obtiene el historial completo de pedidos de una mesa, agrupado por pedido y artículo.
    
    Args:
        id_mesa: ID de la mesa de la cual obtener el historial de pedidos.
            
    Returns:
        str: Cadena de texto formateada con el historial de pedidos de la mesa.
    """
    try:
        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=id_mesa).select_related('mesa', 'infmesa').first()
        if not mesa_abierta:
            return f"La mesa {id_mesa} no está abierta o no existe."

        lineas_agrupadas = Lineaspedido.objects.filter(
            infmesa=mesa_abierta.infmesa,
            estado__in=['P', 'M', 'R']  # Excluir cobradas (C) y anuladas (A)
        ).values(
            'pedido__id',
            'pedido__hora',
            'descripcion'  # Usar descripción directa en lugar de tecla__descripcion_t
        ).annotate(
            cantidad=Count('id')  # Cada línea es una unidad, Count('id') es correcto
        ).order_by('pedido__id', 'descripcion')

        if not lineas_agrupadas:
            return f"La mesa {mesa_abierta.mesa.nombre} no tiene pedidos registrados."

        pedidos_para_texto = defaultdict(lambda: {"hora": "", "articulos": []})
        for linea in lineas_agrupadas:
            pedido_id = linea['pedido__id']
            if not pedidos_para_texto[pedido_id]["hora"]:
                pedidos_para_texto[pedido_id]["hora"] = linea['pedido__hora']
            pedidos_para_texto[pedido_id]['articulos'].append(
                f"{linea['cantidad']}x {linea['descripcion'] or 'Sin descripción'}"
            )

        pedidos_texto = f"Historial de pedidos de la mesa: {mesa_abierta.mesa.nombre}\n"
        for _, info_pedido in pedidos_para_texto.items():
            pedidos_texto += f"\nHora: {info_pedido['hora']}\n"
            pedidos_texto += "\n".join(info_pedido['articulos']) + "\n"

        send_tool_message(f"📋 Historial de pedidos generado para la mesa {mesa_abierta.mesa.nombre}")
        return pedidos_texto.strip()
        
    except Exception as e:
        error_msg = f"Error obteniendo pedidos de mesa {id_mesa}: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(f"❌ {error_msg}")
        return error_msg


@tool
@db_connection_handler
def get_cuenta_mesa(id_mesa: int) -> str:
    """
    Obtiene la cuenta actual de una mesa mostrando solo artículos pendientes y marcados.
    
    Args:
        id_mesa: ID de la mesa de la cual obtener la cuenta.
            
    Returns:
        str: Cadena de texto formateada con la cuenta de la mesa.
    """
    try:
        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=id_mesa).select_related('mesa', 'infmesa').first()
        if not mesa_abierta:
            return f"La mesa {id_mesa} no está abierta o no existe."
        
        articulos_agrupados = Lineaspedido.objects.filter(
            infmesa=mesa_abierta.infmesa,
            estado__in=['P', 'M']  # Incluir también regalos en la cuenta
        ).values(
            'descripcion'  # Usar descripción directa
        ).annotate(
            cantidad=Count('id'),  # Cada línea es una unidad
            precio_total=Sum('precio')
        ).order_by('descripcion')
        
        if not articulos_agrupados:
            return f"La mesa {mesa_abierta.mesa.nombre} no tiene artículos pendientes de cobro."
        
        total_cuenta = 0.0
        cuenta_texto = f"Cuenta de la mesa: {mesa_abierta.mesa.nombre}\n\n"
        
        for articulo in articulos_agrupados:
            cantidad = articulo["cantidad"]
            descripcion = articulo["descripcion"] or "Sin descripción"  # Manejo de nulos
            precio_total = float(articulo["precio_total"] or 0.0)  # Convertir Decimal a float
            
            cuenta_texto += f"{cantidad}x {descripcion} - {precio_total:.2f}€\n"
            total_cuenta += precio_total
        
        cuenta_texto += f"\nTotal = {total_cuenta:.2f}€"
        
        send_tool_message(f"🧾 Cuenta generada para la mesa {mesa_abierta.mesa.nombre}")
        return cuenta_texto
        
    except Exception as e:
        error_msg = f"Error obteniendo cuenta de mesa {id_mesa}: {str(e)}"
        logger.error(error_msg, exc_info=True)
        send_tool_message(f"❌ {error_msg}")
        return error_msg

tools = [
    get_mesa_id,
    buscar_por_similitud_tpv,
    get_pedidos_mesa,
    get_cuenta_mesa,
]