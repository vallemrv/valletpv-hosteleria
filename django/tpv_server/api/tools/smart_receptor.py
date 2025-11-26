# @Author: Manuel Rodriguez <valle>
# @Date:   2025-11-14
# @Email:  valle.mrv@gmail.com
# @License: Apache License v2.0

from comunicacion.tools import send_mensaje_devices
from gestion.models.pedidos import Pedidos, Lineaspedido
from gestion.models.camareros import Camareros
from gestion.models.teclados import Teclas
from gestion.tools.config_logs import configurar_logging
from django.db.models import Prefetch

logger = configurar_logging("smart_receptor")

def enviar_pedido_smart_receptor(pedido):
    """
    Envía pedido a receptores inteligentes (smart receptors).
    No agrupa líneas, las envía una a una para que el receptor las gestione.
    """
    
    # Si recibimos un ID en lugar del objeto, lo buscamos (retrocompatibilidad)
    if isinstance(pedido, int):
        lineas_prefetch = Prefetch(
            'lineaspedido_set',
            queryset=Lineaspedido.objects.select_related('tecla__familia__receptor')
        )
        pedido = Pedidos.objects.select_related('infmesa').prefetch_related(
            lineas_prefetch,
            'infmesa__mesasabiertas_set__mesa'
        ).get(pk=pedido)
    
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa = pedido.infmesa.mesasabiertas_set.all()[0].mesa
    
    # Organizar por receptores (sin agrupar líneas)
    receptores = {}
    for linea in pedido.lineaspedido_set.all():
        if not linea.tecla or not linea.tecla.familia or not linea.tecla.familia.receptor:
            logger.warning(f"Tecla no encontrada para idart: {linea.idart}")
            continue
        
        receptor = linea.tecla.familia.receptor
        receptor_nombre = receptor.nombre
        
        if receptor_nombre not in receptores:
            receptores[receptor_nombre] = {
                "op": "pedido",
                "pedido_id": pedido.id,
                "hora": pedido.hora,
                "receptor": receptor.nombre.lower(),
                "nom_impresora": receptor.nomimp,
                "camarero": camarero.nombre + " " + camarero.apellidos,
                "mesa": mesa.nombre,
                "lineas": []
            }
        
        # Agregar línea sin agrupar (cada línea individual)
        receptores[receptor_nombre]['lineas'].append({
            "id": linea.id,
            "idart": linea.idart,
            "descripcion": linea.descripcion,
            "estado": linea.estado,
            "pedido_id": pedido.id
        })
    
    # Enviar a cada receptor usando send_mensaje_devices (necesita for)
    for receptor_nombre, datos in receptores.items():
        send_mensaje_devices(datos)


def enviar_urgente_smart_receptor(lineas):
    """
    Notifica urgencia a smart receptors enviando solo los IDs de las líneas.
    Parámetros:
        - lineas: queryset de Lineaspedido
    """
    _notificar_lineas("marcar_urgente", lineas)


def _notificar_lineas(op, lineas):
    """
    Función interna que notifica cambios en líneas al receptor.
    Solo notifica, NO actualiza base de datos (eso lo hace el endpoint).
    Parámetros:
        - op: operación a realizar ("servir_lineas", "borrar_lineas", etc)
        - lineas: queryset o lista de objetos Lineaspedido
    """
    if not lineas:
        return
    
    # Agrupar por receptor según familia del artículo
    receptores_ids = {}
    
    for linea in lineas:
        tecla = Teclas.objects.filter(id=int(linea.idart)).first()
        if not tecla:
            continue
        
        receptor = tecla.familia.receptor.nombre.lower()
        
        if receptor not in receptores_ids:
            receptores_ids[receptor] = set()
        
        receptores_ids[receptor].add(linea.id)
    
    # Enviar UNA SOLA notificación por receptor con todas sus líneas
    for receptor, linea_ids_set in receptores_ids.items():
        send_mensaje_devices({
            "op": op,
            "receptor": receptor,
            "ids": list(linea_ids_set)
        })


def notificar_lineas_servidas(lineas):
    """
    Notifica al receptor que las líneas han sido servidas.
    Parámetros: queryset o lista de objetos Lineaspedido
    """
    _notificar_lineas("servir_lineas", lineas)


def notificar_lineas_borradas(lineas):
    """
    Notifica al receptor que las líneas han sido borradas.
    Parámetros: queryset o lista de objetos Lineaspedido
    """
    _notificar_lineas("borrar_lineas", lineas)

