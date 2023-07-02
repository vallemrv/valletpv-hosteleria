from valle_tpv.tools.ws import comunicar_cambios_devices
from valle_tpv.models import Camareros
from tokenapi.http import JsonResponse
from tokenapi.decorators import token_required
import json


@token_required
def crear_password(request):
    cam = request.POST["cam"]
    password = request.POST["password"]
    cam = json.loads(cam)
    id = cam["ID"]
    
    
    camarero =  Camareros.objects.get(pk=id)
    camarero.password = password
    camarero.save()
    comunicar_cambios_devices("md", "camareros", camarero.serialize())

    return JsonResponse("success")


@token_required
def permissions_list(request):
   return JsonResponse(Camareros.permisos_list())

