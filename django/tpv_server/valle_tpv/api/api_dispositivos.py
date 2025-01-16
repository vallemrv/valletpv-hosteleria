from valle_tpv.models import Dispositivos
from tokenapi.decorators import csrf_exempt
from tokenapi.http import JsonResponse


@csrf_exempt 
def add(request):
    obj = Dispositivos.add_handler()
    return JsonResponse(obj)

