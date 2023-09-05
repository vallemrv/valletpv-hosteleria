# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-21T23:27:42+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-22T01:43:07+02:00
# @License: Apache License v2.0

from django.db.models import  Count, Sum, F
from valle_tpv.tools.ws import  send_mensaje_impresora
from valle_tpv.models import (Ticket,  Teclas,
                            Receptores, Pedidos,
                            Camareros)


def imprimir_pedido(id):
    pedido = Pedidos.objects.get(pk=id)
    camareo = Camareros.objects.get(pk=pedido.camarero_id)
    mesa = pedido.infmesa.mesasabiertas_set.get().mesa
    lineas = pedido.lineaspedido_set.values("idart",
                                           "descripcion",
                                           "estado",
                                           "pedido_id").annotate(can=Count('idart'))
    
    
    receptores = {}
    for l in lineas:
        receptor = Teclas.objects.get(id=l['idart']).familia.receptor
        if receptor.nombre not in receptores:
            receptores[receptor.nombre] = {
                "op": "pedido",
                "hora": pedido.hora,
                "receptor": receptor.nomimp,
                "nom_receptor": receptor.nombre,
                "receptor_activo": receptor.activo,
                "camarero": camareo.nombre + " " + camareo.apellidos,
                "mesa": mesa.nombre,
                "lineas": []
            }
    
        receptores[receptor.nombre]['lineas'].append(l)

    send_mensaje_impresora(receptores)

def send_imprimir_ticket(request, id, es_factura=False):
    receptor_activo = request.POST["receptor_activo"] if "receptor_activo" in request.POST else None
    abrircajon = request.POST["abrircajon"] if "abrircajon" in request.POST else True
    handler_enviar_imprimir_ticket(id, receptor_activo, abrircajon, es_factura, request)


def handler_enviar_imprimir_ticket(id, receptor_activo, abrircajon, es_factura, request):
    receptor = Receptores.objects.filter(isTicket=True).first()
    if not receptor:
        receptor_activo = False
    else:
        receptor_activo = receptor_activo if receptor_activo else receptor.activo

    if not receptor_activo:
        return    

    ticket = Ticket.objects.get(pk=id)
    camarero = Camareros.objects.get(pk=ticket.camarero_id)
    lineas = ticket.ticketlineas_set.all().annotate(teclaId=F("linea__tecla_id"),
                                                    precio=F("linea__precio"),
                                                    descripcion=F("linea__descripcion_t"))

    lineas = lineas.values("teclaId",
                           "descripcion",
                           "precio").annotate(can=Count('tecla'),
                                              totallinea=Sum("precio"))
    

    lineas_ticket = []
    for l in lineas:
        lineas_ticket.append(l)

    url_factura = ""
    if es_factura:
       url_factura = "http://"+request.get_host()+"/app/facturas/"+id+"/"+ticket.uid
    
    obj = {
        "op": "ticket",
        "fecha": ticket.fecha + " " + ticket.hora,
        "receptor": receptor.nombre,
        "isActivo": receptor_activo if receptor_activo else receptor.activo,
        "abrirCajon": abrircajon,
        "camarero": camarero.nombre + " " + camarero.apellidos,
        "mesa": ticket.mesa,
        "lineas":lineas_ticket,
        "num": ticket.id,
        "efectivo": ticket.entrega,
        'total': ticket.ticketlineas_set.all().aggregate(Total=Sum("linea__precio"))['Total'],
        "url_factura": url_factura
    }

 
    send_mensaje_impresora(obj)
