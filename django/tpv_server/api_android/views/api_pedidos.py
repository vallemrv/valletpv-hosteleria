# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:52:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-26T14:53:11+02:00
# @License: Apache License v2.0

from api_android.tools import send_mensaje_devices
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from gestion.models import  Lineaspedido, Mesasabiertas, Servidos
import json

@csrf_exempt
def  get_pendientes(request):
    idz = request.POST["idz"]
    mesas = Mesasabiertas.objects.filter(mesa__mesaszona__zona__pk=idz)
    lineas = []
    for m in mesas:
        lineas = [*lineas, *m.get_lineaspedido()]
          

    return JsonResponse(lineas)

@csrf_exempt
def servido(request):
    art = json.loads(request.POST["art"])
    lineas = Lineaspedido.objects.filter(idart=art["IDArt"],
                                         descripcion=art["Descripcion"],
                                         precio=art["Precio"],
                                         pedido_id=art["IDPedido"])

    for l in lineas:
        serv = Servidos()
        serv.linea_id = l.pk
        serv.save()

    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "pendientes",
       "receptor": "comandas",
    }
    send_mensaje_devices(update)


    return get_pendientes(request)

