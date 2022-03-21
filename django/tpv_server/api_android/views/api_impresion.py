# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-31T16:11:02+01:00
# @License: Apache License v2.0

from api_android.tools import (send_pedidos_ws, send_ticket_ws,
                               send_update_ws, send_imprimir_ticket)
from tokenapi.http import JsonError, JsonResponse
from gestion.models import (Pedidos, Lineaspedido, Ticket, Infmesa, Teclas,
                            Arqueocaja, Efectivo, Ticketlineas, Receptores,
                            Mesasabiertas, Camareros)
from django.http import HttpResponse
from django.forms.models import model_to_dict
from django.db.models import Q, Count, Sum, F
from django.views.decorators.csrf import csrf_exempt
from django.db.models.fields import DecimalField
from django.conf import settings
from datetime import datetime


@csrf_exempt
def preimprimir(request):
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.get(mesa_id=idm)
    infmesa = mesa_abierta.infmesa
    infmesa.numcopias = infmesa.numcopias + 1
    infmesa.save()
    camareo = infmesa.camarero
    mesa = mesa_abierta.mesa
    lineas = infmesa.lineaspedido_set.filter(estado="P")
    lineas = lineas.values("idart", "precio").annotate(can=Count('idart'),
                                                       totallinea=Sum("precio"))
    lineas_ticket = []
    for l in lineas:
        try:
            art = Teclas.objects.get(id=l["idart"])
            l["nombre"] = art.nombre
        except:
            art = infmesa.lineaspedido_set.filter(idart=l["idart"]).first()
            if art:
                l["nombre"] = art.nombre
        lineas_ticket.append(l)

    receptor = Receptores.objects.get(nombre='Ticket')
    obj = {
      "op": "preticket",
      "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
      "receptor": receptor.nomimp,
      "receptor_activo": receptor.activo,
      "camarero": camareo.nombre + " " + camareo.apellidos,
      "mesa": mesa.nombre,
      "numcopias": infmesa.numcopias,
      "lineas": lineas_ticket,
      'total': infmesa.lineaspedido_set.filter(estado="P").aggregate(Total=Sum("precio"))['Total']
      }

    if infmesa.numcopias <= 1:
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(request, update)

    send_ticket_ws(request, obj)
    return JsonResponse({})

@csrf_exempt
def reenviarlinea(request):
    idp = request.POST["idp"];
    id = request.POST["id"];
    nombre = request.POST["Nombre"];
    pedido = Pedidos.objects.get(pk=idp)
    camareo = Camareros.objects.get(pk=pedido.camarero_id)
    mesa = pedido.infmesa.mesasabiertas_set.get().mesa
    lineas = pedido.lineaspedido_set.filter(idart=id, nombre=nombre).values("idart",
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
                "receptor_activo": receptor.activo,
                "receptor": receptor.nomimp,
                "camarero": camareo.nombre + " " + camareo.apellidos,
                "mesa": mesa.nombre,
                "lineas": []
            }
        l["precio"] = float(l["precio"])
        receptores[receptor.nombre]['lineas'].append(l)

    send_pedidos_ws(request, receptores)
    return HttpResponse("success")

@csrf_exempt
def abrircajon(request):
    obj = {
        "op": "open",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "receptor_activo": True,
    }
    send_ticket_ws(request, obj)
    return HttpResponse("success")

@csrf_exempt
def imprimir_ticket(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id)
    return HttpResponse("success")
