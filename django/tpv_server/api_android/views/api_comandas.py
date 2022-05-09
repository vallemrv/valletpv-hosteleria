# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:17:04+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T17:44:16+02:00
# @License: Apache License v2.0

from api_android.tools import (send_update_ws, imprimir_pedido)
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
from django.db.models import  Count
from gestion.models import (Mesasabiertas, SeccionesCom, Subteclas, Sync,
                            Teclas, Infmesa, Pedidos,  Camareros)

from datetime import datetime
from uuid import uuid4
import json


@csrf_exempt
def marcar_rojo(request):
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.get(mesa_id=idm)
    infmesa = mesa_abierta.infmesa
    infmesa.numcopias = infmesa.numcopias + 1
    infmesa.save()
    Sync.actualizar("mesasabiertas")
    if infmesa.numcopias <= 1:
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(update)
    return JsonResponse({})



@csrf_exempt
def pedir(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    lineas = json.loads(request.POST["pedido"])
    is_updatable, pedido = Pedidos.agregar_nuevas_lineas(idm,idc,lineas)
    
    if is_updatable:
        #enviar notficacion de update
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(update)


    
    imprimir_pedido(pedido.id)
    return HttpResponse("success")

@csrf_exempt
def get_ultimas(request):
    args = json.loads(request.POST['args'])
    c = args['o']
    lista = args['r']
    pedidos = Pedidos.objects.all().order_by('-id')[c:c+5]
    send = []
    for p in pedidos:
        camareo = Camareros.objects.get(pk=p.camarero_id)
        mesa = p.infmesa.mesasabiertas_set.first()
        if mesa:
            mesa = mesa.mesa
            lineas = p.lineaspedido_set.values("idart",
                                               "descripcion",
                                               "descripcion_t"
                                               "precio",
                                               "pedido_id").annotate(can=Count('idart'))
            receptores = {}
            for l in lineas:
                receptor = Teclas.objects.get(id=l['idart']).familia.receptor
                if receptor.nomimp in lista:
                    if  receptor.nombre not in receptores:
                        receptores[receptor.nombre] = {
                            "op": "pedido",
                            "hora": p.hora,
                            "receptor": receptor.nomimp,
                            "receptor_activo": receptor.activo,
                            "camarero": camareo.nombre + " " + camareo.apellidos,
                            "mesa": mesa.nombre,
                            "lineas": []
                        }
                    l["precio"] = float(l["precio"])
                    receptores[receptor.nombre]['lineas'].append(l)

            send.append(receptores)


    return JsonResponse(send)
