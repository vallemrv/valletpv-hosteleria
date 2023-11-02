# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-31T16:11:02+01:00
# @License: Apache License v2.0

from valle_tpv.tools.impresion import (send_imprimir_ticket, send_mensaje_impresora)
from valle_tpv.tools.ws import comunicar_cambios_devices
from valle_tpv.decorators import check_dispositivo
from valle_tpv.models import (Pedidos, Teclas, Receptores,
                            Mesasabiertas, Camareros)
from tokenapi.http import JsonResponse
from django.db.models import Count, Sum

from datetime import datetime


@check_dispositivo
def abrircajon(request):
    receptor = Receptores.objects.filter(isTicket=True).first()
    if not receptor:
        return JsonResponse({})
    
    obj = {
        "op": "open",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": receptor.nombre,
        "impresora": receptor.nom_impresora,
        "receptor_activo": True,
    }
    send_mensaje_impresora(obj)
    return JsonResponse({})



@check_dispositivo
def preimprimir(request):
    
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.filter(mesa_id=idm).first()
    if mesa_abierta:
        infmesa = mesa_abierta.infmesa
        infmesa.numcopias = infmesa.numcopias + 1
        infmesa.save()
        
        if infmesa.numcopias <= 1:
            comunicar_cambios_devices("update", "mesas",[mesa_abierta.serialize()])
            
        receptor = Receptores.objects.filter(isTicket=True).first()
        if not receptor:
            return JsonResponse({})

        camareo = infmesa.camarero
        mesa = mesa_abierta.mesa
        lineas = infmesa.lineaspedido_set.filter(estado="P")
        lineas = lineas.values("tecla_id", 
                            "descripcion_t", 
                            "precio").annotate(can=Count('tecla_id'),
                                                totallinea=Sum("precio"))
        

        
        lineas_ticket = []
        for l in lineas:
            lineas_ticket.append(l)

        obj = {
        "op": "preticket",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": receptor.nombre,
        "impresora": receptor.nom_impresora,
        "receptor_activo": True,
        "camarero": camareo.nombre + " " + camareo.apellidos,
        "mesa": mesa.nombre,
        "numcopias": infmesa.numcopias,
        "lineas": lineas_ticket,
        'total': infmesa.lineaspedido_set.filter(estado="P").aggregate(Total=Sum("precio"))['Total']
        }


        send_mensaje_impresora(obj)
    return JsonResponse({})


@check_dispositivo
def reenviarpedido(request):
    idp = request.POST["idp"];
    idr = request.POST["idr"];
    pedido = Pedidos.objects.get(pk=idp);
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa_a = pedido.infmesa.mesasabiertas_set.first()
    lineas = pedido.lineaspedido_set.filter(tecla__familia__receptor__pk=idr).values("tecla_id",
                                            "descripcion",
                                            "estado",
                                            "pedido_id").annotate(can=Count('tecla_id'))
    return send_urgente(lineas, pedido.hora, camarero, mesa_a)



@check_dispositivo
def imprimir_ticket(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id)
    return JsonResponse({})


@check_dispositivo
def imprimir_factura(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id, True)
    return JsonResponse({})



@check_dispositivo
def reenviarlinea(request):
    idp = request.POST["idp"];
    id = request.POST["id"];
    nombre = request.POST["descripcion"];
    pedido = Pedidos.objects.get(pk=idp)
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa_a = pedido.infmesa.mesasabiertas_set.first()
    lineas = pedido.lineaspedido_set.filter(tecla_id=id, descripcion=nombre).values("tecla_id",
                                            "descripcion",
                                            "estado",
                                            "pedido_id").annotate(can=Count('tecla_id'))
    return send_urgente(lineas, pedido.hora, camarero, mesa_a)

def send_urgente(lineas, hora, camarero, mesa_a):
    if mesa_a:
        mesa = mesa_a.mesa
        receptores = {}
        for l in lineas:
            receptor = Teclas.objects.get(id=l['tecla_id']).familia.receptor
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
    return JsonResponse({})