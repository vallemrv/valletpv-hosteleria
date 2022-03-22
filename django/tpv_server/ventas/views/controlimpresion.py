# @Author: Manuel Rodriguez <valle>
# @Date:   27-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 29-Jun-2018
# @License: Apache license vesion 2.0


import json
from datetime import datetime
from django.db.models import Count, Sum, F
from django.db.models.fields import DecimalField
from django.core.serializers.json import DjangoJSONEncoder
from django.contrib.sites.shortcuts import get_current_site
from django.conf import settings
from tokenapi.http import JsonResponse
from gestion.models import (Pedidos,  Ticket,  Teclas,
                            Arqueocaja,  Receptores,
                            Mesasabiertas, Camareros)

from api_android.tools import send_pedidos_ws, send_ticket_ws


def imprimir_pedido(request, id):
    pedido = Pedidos.objects.get(pk=id)
    camareo = Camareros.objects.get(pk=pedido.camarero_id)
    mesa = pedido.infmesa.mesasabiertas_set.get().mesa
    lineas = pedido.lineaspedido_set.values("idart",
                                           "nombre",
                                           "precio",
                                           "pedido_id").annotate(can=Count('idart'))
    receptores = {}
    for l in lineas:
        receptor = Teclas.objects.get(id=l['idart']).familia.receptor
        if receptor.nombre not in receptores:
            receptores[receptor.nombre] = {
                "op": "pedido",
                "hora": pedido.hora,
                "receptor": receptor.nomimp,
                "camarero": camareo.nombre + " " + camareo.apellidos,
                "mesa": mesa.nombre,
                "lineas": []
            }
        l["precio"] = float(l["precio"])
        receptores[receptor.nombre]['lineas'].append(l)

    send_pedidos_ws(request, receptores)
    return JsonResponse({})


def imprimir_desglose(request, id):
    arqueo = Arqueocaja.objects.get(pk=id)
    cambio = arqueo.cambio
    efectivo = arqueo.efectivo_set.all().values("moneda").annotate(can = Sum("can")).order_by("-moneda")
    retirar = arqueo.efectivo_set.all().aggregate(total=Sum(F("can") * F("moneda"), output_field=DecimalField()))['total']
    retirar = float(retirar) - float(cambio)
    lineas_retirada = []
    lineas_cambio = []
    parcial = 0
    for linea in efectivo:
        can = linea["can"]
        moneda = linea["moneda"]
        texto = "moneda" if moneda < 5 else "billete"
        texto = texto + "s" if can > 1 else ""
        if retirar <= parcial:
            if can > 0:
                lineas_cambio.append({"titulo": "Cambio", 'can':can,'tipo':float(moneda), 'texto_tipo': texto })
        elif retirar > ((can * float(moneda)) + parcial):
            parcial = parcial + float((can * moneda))
            if can > 0:
                lineas_retirada.append({"titulo": "Retirar", 'can':can,'tipo':float(moneda), 'texto_tipo': texto })
        else:
            diferencia = retirar - parcial
            can_parcial = int(diferencia/float(moneda))
            parcial = parcial + (can_parcial * float(moneda))
            if can_parcial > 0:
                lineas_retirada.append({"titulo": "Retirar", 'can':can_parcial,'tipo':float(moneda), 'texto_tipo': texto })
            texto = "moneda" if moneda < 5 else "billete"
            texto = texto + "s" if can_parcial > 1 else texto
            if can - can_parcial > 0:
                lineas_cambio.append({"titulo": "Cambio", 'can':can - can_parcial, 'tipo':float(moneda), 'texto_tipo': texto })

    obj_cambio = {
        "op": "desglose",
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "lineas": lineas_cambio
    }

    obj_desglose = {
        "op": "desglose",
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "lineas": lineas_retirada
    }
    send_ticket_ws(request, obj_desglose)
    send_ticket_ws(request, obj_cambio)
    return JsonResponse({})

def preimprimir_ticket(request, id):
    mesa = Mesasabiertas.objects.get(mesa_id=id)
    infmesa = mesa.infmesa
    infmesa.numcopias = infmesa.numcopias + 1
    infmesa.save()
    camareo = infmesa.camarero
    mesa = mesa.mesa
    lineas = infmesa.lineaspedido_set.filter(estado="P")
    lineas = lineas.values("idart", "precio").annotate(can=Count('idart'),
                                                       totallinea=Sum("precio"))
    lineas_ticket = []
    for l in lineas:
        try:
            art = Teclas.objects.get(id=l["idart"])
            l["nombre"] = art.nombre
        except:
            art = infmesa.lineaspedido_set.filter(idart=l["idart"])
            if len(art) > 0:
                l["nombre"] = art[0].linea.nombre
        lineas_ticket.append(l)
    obj = {
        "op": "preticket",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "camarero": camareo.nombre + " " + camareo.apellidos,
        "mesa": mesa.nombre,
        "numcopias": infmesa.numcopias,
        "lineas": lineas_ticket,
        'total': infmesa.lineaspedido_set.filter(estado="P").aggregate(Total=Sum("precio"))['Total']
    }

    send_ticket_ws(request, obj)
    return JsonResponse({})

def imprimir_ticket(request, id):
    ticket = Ticket.objects.get(pk=id)
    camarero = Camareros.objects.get(pk=ticket.camarero_id)
    lineas = ticket.ticketlineas_set.all().annotate(idart=F("linea__idart"),
                                                    precio=F("linea__precio"))

    lineas = lineas.values("idart",
                           "precio").annotate(can=Count('idart'),
                                              totallinea=Sum("precio"))
    lineas_ticket = []
    for l in lineas:
        try:
            art = Teclas.objects.get(id=l["idart"])
            l["nombre"] = art.nombre
        except:
            art = ticket.ticketlineas_set.filter(linea__idart=l["idart"])
            if len(art) > 0:
                l["nombre"] = art[0].linea.nombre
        lineas_ticket.append(l)

    obj = {
        "op": "ticket",
        "fecha": ticket.fecha + " " + ticket.hora,
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "camarero": camarero.nombre + " " + camarero.apellidos,
        "mesa": ticket.mesa,
        "lineas": lineas_ticket,
        "num": ticket.id,
        "efectivo": ticket.entrega,
        'total': ticket.ticketlineas_set.all().aggregate(Total=Sum("linea__precio"))['Total']
    }
    send_ticket_ws(request, obj)
    return JsonResponse({})

def reenviarlinea(request, id, idl, nombre):
    import urllib.parse
    nombre = urllib.parse.unquote(nombre).replace("+", " ")
    pedido = Pedidos.objects.get(pk=id)
    camareo = Camareros.objects.get(pk=pedido.camarero_id)
    print(pedido.infmesa)
    mesa = pedido.infmesa.mesasabiertas_set.get().mesa
    lineas = pedido.lineaspedido_set.filter(idart=idl, nombre=nombre).values("idart",
                                           "nombre",
                                           "precio",
                                           "pedido_id").annotate(can=Count('idart'))
    receptores = {}
    for l in lineas:
        receptor = Teclas.objects.get(id=l['idart']).familia.receptor
        if receptor.nombre not in receptores:
            receptores[receptor.nombre] = {
                "op": "urgente",
                "hora": pedido.hora,
                "receptor": receptor.nomimp,
                "camarero": camareo.nombre + " " + camareo.apellidos,
                "mesa": mesa.nombre,
                "lineas": []
            }
        l["precio"] = float(l["precio"])
        receptores[receptor.nombre]['lineas'].append(l)

    send_pedidos_ws(request, receptores)
    return JsonResponse({})

def abrircajon(request):
    obj = {
        "op": "open",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
    }
    send_ticket_ws(request, obj)
    return JsonResponse({})

