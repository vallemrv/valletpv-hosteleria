from valle_tpv.models import Dispositivos
from tokenapi.decorators import csrf_exempt
from tokenapi.http import JsonResponse
import json

@csrf_exempt 
def add(request):
    reg = json.loads(request.POST["reg"])
    obj = Dispositivos.add_handler(reg)
    return JsonResponse(obj)

