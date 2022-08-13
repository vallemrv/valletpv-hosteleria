# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-31T16:11:02+01:00
# @License: Apache License v2.0

from api_android.tools import (send_mensaje_devices, send_imprimir_ticket, send_mensaje_impresora)
from tokenapi.http import  JsonResponse
from comunicacion.tools import comunicar_cambios_devices
from gestion.models import (Pedidos, Teclas, Receptores,
                            Mesasabiertas, Camareros, Sync)
from django.http import HttpResponse
from django.db.models import Count, Sum
from django.views.decorators.csrf import csrf_exempt
from datetime import datetime


@csrf_exempt
def preimprimir(request):
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.filter(mesa_id=idm).first()
    if mesa_abierta:
        infmesa = mesa_abierta.infmesa
        infmesa.numcopias = infmesa.numcopias + 1
        infmesa.save()
        Sync.actualizar("mesasabiertas")
        camareo = infmesa.camarero
        mesa = mesa_abierta.mesa
        lineas = infmesa.lineaspedido_set.filter(estado="P")
        lineas = lineas.values("idart", 
                            "descripcion_t", 
                            "precio").annotate(can=Count('idart'),
                                                totallinea=Sum("precio"))
        

        receptor = Receptores.objects.get(nombre='Ticket')
        lineas_ticket = []
        for l in lineas:
            lineas_ticket.append(l)

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
            comunicar_cambios_devices("md", "mesasabiertas", 
                                    mesa_abierta.serialize(), 
                                    {"op": "preimprimir"})

        send_mensaje_impresora(obj)
    return JsonResponse({})


@csrf_exempt
def reenviarpedido(request):
    idp = request.POST["idp"];
    idr = request.POST["idr"];
    pedido = Pedidos.objects.get(pk=idp);
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa_a = pedido.infmesa.mesasabiertas_set.first()
    lineas = pedido.lineaspedido_set.filter(tecla__familia__receptor__pk=idr).values("idart",
                                            "descripcion",
                                            "estado",
                                            "pedido_id").annotate(can=Count('idart'))
    return send_urgente(lineas, pedido.hora, camarero, mesa_a)

@csrf_exempt
def reenviarlinea(request):
    idp = request.POST["idp"];
    id = request.POST["id"];
    nombre = request.POST["Descripcion"];
    pedido = Pedidos.objects.get(pk=idp)
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa_a = pedido.infmesa.mesasabiertas_set.first()
    lineas = pedido.lineaspedido_set.filter(idart=id, descripcion=nombre).values("idart",
                                            "descripcion",
                                            "estado",
                                            "pedido_id").annotate(can=Count('idart'))
    return send_urgente(lineas, pedido.hora, camarero, mesa_a)

@csrf_exempt
def abrircajon(request):
    obj = {
        "op": "open",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "receptor_activo": True,
    }
    send_mensaje_impresora(obj)
    return HttpResponse("success")

@csrf_exempt
def imprimir_ticket(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id)
    return HttpResponse("success")


@csrf_exempt
def imprimir_factura(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id, True)
    return HttpResponse("success")


def send_urgente(lineas, hora, camarero, mesa_a):
    if mesa_a:
        mesa = mesa_a.mesa
        receptores = {}
        for l in lineas:
            receptor = Teclas.objects.get(id=l['idart']).familia.receptor
            if receptor.nombre not in receptores:
                receptores[receptor.nombre] = {
                    "op": "urgente",
                    "hora": hora,
                    "receptor_activo": receptor.activo,
                    "receptor": receptor.nomimp,
                    "nom_receptor": receptor.nombre,
                    "camarero": camarero.nombre + " " + camarero.apellidos,
                    "mesa": mesa.nombre,
                    "lineas": []
                }
            receptores[receptor.nombre]['lineas'].append(l)
    
        send_mensaje_impresora(receptores)
    return HttpResponse("success")