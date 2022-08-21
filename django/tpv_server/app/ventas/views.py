from .utils import get_total_by_horas, get_total
from tokenapi.http import JsonResponse
from gestion.models import Camareros, Ticket
from tokenapi.decorators import token_required
from gestion.models import Mesasabiertas

@token_required
def get_pedidos_by_hora(request):
    last_id = Ticket.get_last_id_linea()
    res = []   
    res.append({"pedido": get_total_by_horas({'estado':'P'})})
    res.append({"cobrado": get_total_by_horas({'id__gt':last_id, 'estado':'C'})})
    res.append({"borrado":get_total_by_horas({'id__gt':last_id, 'estado':'A'})})
    return JsonResponse(res)

@token_required
def datasets(request):
    last_id = Ticket.get_last_id_linea()
    return JsonResponse( get_total("estado", {'id__gt':last_id}))


@token_required
def cuenta_rm(request):
    idm = request.POST["idm"]
    motivo = request.POST["motivo"]
    idc = Camareros.objects.first().id
    Mesasabiertas.borrar_mesa_abierta(idm,idc,motivo)
    return JsonResponse({})

@token_required
def get_infomesa(request):
    pk = request.POST["pk"]
    m = Mesasabiertas.objects.filter(id=pk).first()
    if m:
        for p in m.infmesa.pedidos_set.all():
            pass
    return JsonResponse({})