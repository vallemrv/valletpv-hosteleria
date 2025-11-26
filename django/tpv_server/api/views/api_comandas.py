# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:17:04+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T17:44:16+02:00
# @License: Apache License v2.0

from uuid import uuid4
from api.tools import imprimir_pedido
from api.tools.smart_receptor import enviar_pedido_smart_receptor
from comunicacion.tools import comunicar_cambios_devices
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from api.decorators.uid_activo import verificar_uid_activo
from gestion.models.mesasabiertas import Mesasabiertas 
from gestion.models.pedidos import Pedidos

import json


@verificar_uid_activo
def marcar_rojo(request):
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.get(mesa_id=idm)
    infmesa = mesa_abierta.infmesa
    infmesa.numcopias = infmesa.numcopias + 1
    infmesa.save()
    if infmesa.numcopias <= 1:
        comunicar_cambios_devices("md", "mesas", mesa_abierta.mesa.serialize())
    return JsonResponse("success")



@verificar_uid_activo
def pedir(request):
    idm = int(request.POST["idm"])
    idc = int(request.POST["idc"])
    uid_device = request.POST["uid_device"] if "uid_device" in request.POST else str(uuid4())
    lineas = json.loads(request.POST["pedido"])
    

    pedido = Pedidos.agregar_nuevas_lineas(idm,idc,lineas, uid_device)
    if pedido:
        imprimir_pedido(pedido)
        enviar_pedido_smart_receptor(pedido)
        
    return HttpResponse("success")


