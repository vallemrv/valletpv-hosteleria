from django.forms import model_to_dict
from gestion.models import Camareros
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from api_android.tools import send_mensaje_devices
import json

#Utilizado por los comanderros
@csrf_exempt
def listado(request):
    lista = Camareros.objects.filter(autorizado=1)
    objs = []
    for l in lista:
        objs.append(model_to_dict(l))
    return JsonResponse(objs)


@csrf_exempt
def camarero_add(request):
    nombre = request.POST["nombre"]
    apellido = request.POST["apellido"]
    c = Camareros()
    c.nombre = nombre
    c.apellidos = apellido
    c.save()
    update = {
        "op": "insert",
        "tb": "camareros",
        "obj": model_to_dict(c),
        "receptor": "devices",
        "device": ""
    }
    send_mensaje_devices(update)
   

    return JsonResponse("success")


@csrf_exempt
def crear_password(request):
    cam = request.POST["cam"]
    password = request.POST["password"]
    cam = json.loads(cam)
    id = cam["ID"]
    cam["Pass"] = password
    
    camarero =  Camareros.objects.get(pk=id)
    camarero.pass_field = password
    camarero.save()

    return JsonResponse("success")