from .utils import get_total_by_horas, get_total
from tokenapi.http import JsonResponse
from gestion.models import Ticket
from tokenapi.decorators import token_required

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
def mesas_abiertas(request):
    print(request.POST)
    return JsonResponse({'tb':"infmesas", 'regs':[]})
