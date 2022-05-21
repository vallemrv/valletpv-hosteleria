# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:17:04+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T17:44:16+02:00
# @License: Apache License v2.0

from api_android.tools import imprimir_pedido
from comunicacion.tools import comunicar_cambios_devices
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from gestion.models import (Mesasabiertas, Sync,
                             Pedidos)


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
        comunicar_cambios_devices("md", "mesasabiertas", mesa_abierta.serialize())
    return JsonResponse("success")



@csrf_exempt
def pedir(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    lineas = json.loads(request.POST["pedido"])
    pedido = Pedidos.agregar_nuevas_lineas(idm,idc,lineas)
    imprimir_pedido(pedido.id)
    return HttpResponse("success")


