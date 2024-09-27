from django.forms import model_to_dict
from comunicacion.tools import comunicar_cambios_devices
from gestion.models.camareros import Camareros
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
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
    comunicar_cambios_devices("md", "camareros", c.serialize())
   

    return JsonResponse("success")


@csrf_exempt
def crear_password(request):
    cam = request.POST["cam"]
    password = request.POST["password"]
    cam = json.loads(cam)
    id = cam["ID"]
    
    
    camarero =  Camareros.objects.get(pk=id)
    camarero.pass_field = password
    camarero.save()
    comunicar_cambios_devices("md", "camareros", camarero.serialize())

    return JsonResponse("success")