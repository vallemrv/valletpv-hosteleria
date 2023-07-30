from valle_tpv.models import Dispositivos
from tokenapi.decorators import csrf_exempt
from tokenapi.http import JsonResponse
import json

@csrf_exempt 
def add(request):
    obj = Dispositivos.add_handler()
    return JsonResponse(obj)

