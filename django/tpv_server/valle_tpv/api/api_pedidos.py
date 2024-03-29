import json
from uuid import uuid4
from tokenapi.http import JsonResponse
from valle_tpv.models import  Pedidos, Dispositivos
from valle_tpv.tools.impresion import imprimir_pedido
from valle_tpv.decorators import check_dispositivo



@check_dispositivo
def pedir(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    uid_pedido = request.POST["uid_pedido"] if "uid_pedido" in request.POST else str(uuid4())
    uid_device = request.POST["UID"]
    lineas = json.loads(request.POST["pedido"])
    pedido = Pedidos.agregar_nuevas_lineas(idm, idc, lineas, uid_pedido)
    dispositivo = Dispositivos.objects.get(UID=uid_device)
    if pedido and dispositivo.puede_enviar:
        imprimir_pedido(pedido.id)
    return JsonResponse({})
