from django.forms import model_to_dict
from gestion.models import Camareros
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json

@csrf_exempt
def camarero_add(request):
    nombre = request.POST["nombre"]
    apellido = request.POST["apellido"]
    c = Camareros()
    c.nombre = nombre
    c.apellidos = apellido
    c.save()

    return JsonResponse("success")

@csrf_exempt
def listado_camareros(request):
    camareros = Camareros.objects.filter(autorizado=1)
    objres = []
    for c in camareros:
        
        objres.append(model_to_dict(c))

    return JsonResponse(objres)

@csrf_exempt
def crear_password(request):
    cam = request.POST["cam"]
    password = request.POST["password"]
    cam = json.loads(cam)
    id = cam["ID"]
    cam["Pass"] = password
    try:
        camarero =  Camareros.objects.get(pk=id)
        if camarero.activo:
            camarero.pass_field = password
            camarero.save()
            return JsonResponse({"cam": json.dumps(cam),
                                 "autorizado": True})
        else:
            return JsonResponse({"cam": json.dumps(cam),
                                 "autorizado": False})
    except:
        return JsonResponse({"cam": json.dumps(cam),
                             "autorizado": False})