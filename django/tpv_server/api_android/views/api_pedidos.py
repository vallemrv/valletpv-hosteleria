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


def find(id, lineas):
    for l in lineas:
        if int(l["ID"]) == int(id):
            lineas.remove(l)
            return l
    return None

@csrf_exempt
def  get_pendientes(request):
    idz = request.POST["idz"]
    lineas = json.loads(request.POST["lineas"])
    mesas = Mesasabiertas.objects.filter(mesa__mesaszona__zona__pk=idz).distinct()
    result = []
    lineas_server = []
    
    for m in mesas:
        lineas_server = [*lineas_server, *m.get_lineaspedido()]
    for l in lineas_server:
        linea = find(l["ID"], lineas)
        if linea:
            if not Lineaspedido.is_equals(l, linea):
                result.append({'op':'md', 'reg': l})
        else:
            result.append({'op':'insert', 'reg': l})
            
    for l in lineas:
         result.append({'op':'rm', 'reg': {"ID":l["ID"]}})   

    return JsonResponse(result)

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


def get_pedidos_by_receptor(request):
    rec = json.loads(request.POST["receptores"])
    lineas = Lineaspedido.objects.filter(tecla__familia__receptor__pk=rec.id).values("pedido","descripcion", "estado")
    for l in lineas:
        print(l.descripcion)

