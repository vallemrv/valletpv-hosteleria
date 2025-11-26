# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-21T23:27:42+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-22T01:43:07+02:00
# @License: Apache License v2.0

from django.db.models import  Count, Sum, F
from comunicacion.tools import  send_mensaje_impresora
from gestion.models.ticket import Ticket 
from gestion.models.teclados import Teclas
from gestion.models.familias import Receptores
from gestion.models.pedidos import Pedidos
from gestion.models.camareros import Camareros
from gestion.tools.config_logs import configurar_logging
from django.db.models import Prefetch
# Agrupar líneas por idart para contar (equivalente a annotate(can=Count('idart')))
from collections import defaultdict
import json

logger = configurar_logging("imprimir_pedido")                         

def imprimir_pedido(pedido):
    
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
    
    camareo = Camareros.objects.get(pk=pedido.camarero_id)
    mesa = pedido.infmesa.mesasabiertas_set.all()[0].mesa
    
    
    receptores = {}
    
    for linea in pedido.lineaspedido_set.all():
        if not linea.tecla or not linea.tecla.familia or not linea.tecla.familia.receptor:
            logger.warning("Tecla no encontrada para idart: %s", linea.idart)
            continue
            
        receptor = linea.tecla.familia.receptor
        receptor_nombre = receptor.nombre
        
        if receptor_nombre not in receptores:
            receptores[receptor_nombre] = {
                "op": "pedido",
                "hora": pedido.hora,
                "receptor": receptor.nomimp,
                "nom_receptor": receptor_nombre,
                "receptor_activo": receptor.activo,
                "camarero": camareo.nombre + " " + camareo.apellidos,
                "mesa": mesa.nombre,
                "lineas": []
            }
            
        # Buscar si ya existe una línea igual en este receptor
        encontrada = False
        for l in receptores[receptor_nombre]['lineas']:
            if (l['descripcion'] == linea.descripcion and 
                l['estado'] == linea.estado and 
                l['precio'] == linea.precio):
                l['can'] += 1
                encontrada = True
                break
        
        if not encontrada:
            receptores[receptor_nombre]['lineas'].append({
                "idart": linea.idart,
                "descripcion": linea.descripcion,
                "estado": linea.estado,
                "pedido_id": linea.pedido_id,
                "precio": linea.precio,
                "can": 1
            })

    send_mensaje_impresora(receptores)

def send_imprimir_ticket(request, id, es_factura=False):
    receptor_activo = request.POST["receptor_activo"] if "receptor_activo" in request.POST else None
    abrircajon = request.POST["abrircajon"] if "abrircajon" in request.POST else True
    handler_enviar_imprimir_ticket(id, receptor_activo, abrircajon, es_factura, request)


def handler_enviar_imprimir_ticket(id, receptor_activo, abrircajon, es_factura, request):
    ticket = Ticket.objects.get(pk=id)
    camarero = Camareros.objects.get(pk=ticket.camarero_id)
    lineas = ticket.ticketlineas_set.all().annotate(idart=F("linea__idart"),
                                                    precio=F("linea__precio"),
                                                    descripcion_t=F("linea__descripcion_t"))

    lineas = lineas.values("idart",
                           "descripcion_t",
                           "precio").annotate(can=Count('idart'),
                                              totallinea=Sum("precio")).filter(precio__gt=0)
    

    receptor = Receptores.objects.get(nombre='Ticket')
    lineas_ticket = []
    for l in lineas:
        lineas_ticket.append(l)

    url_factura = ""
    if es_factura:
       url_factura = "https://"+request.get_host()+"/app/facturas/"+id+"/"+ticket.uid

    if ticket.recibo_tarjeta != "":
        recibo = json.loads(ticket.recibo_tarjeta)
        
        # Convertir el recibo en una lista de líneas formato "clave: valor"
        lineas_recibo = []
        
        # Procesar las claves dentro de 'recibo'
        for key, value in recibo['recibo'].items():
            lineas_recibo.append(f"{key} {value}")
        
        # Añadir la línea con el código de autorización
        lineas_recibo.append(f"Código autorización: {recibo['codigo_autorizacion']}")
    else:
        lineas_recibo = []  # Si no hay recibo, la lista de líneas estará vacía

    
    obj = {
        "op": "ticket",
        "fecha": ticket.fecha + " " + ticket.hora,
        "receptor": receptor.nomimp,
        "nom_receptor": receptor.nombre,
        "receptor_activo": receptor_activo if receptor_activo else receptor.activo,
        "abrircajon": abrircajon,
        "camarero": camarero.nombre + " " + camarero.apellidos,
        "mesa": ticket.mesa,
        "lineas":lineas_ticket,
        "num": ticket.id,
        "efectivo": ticket.entrega,
        'total': ticket.ticketlineas_set.all().aggregate(Total=Sum("linea__precio"))['Total'],
        "url_factura": url_factura,
        "recibo": lineas_recibo
    }

 
    send_mensaje_impresora(obj)
