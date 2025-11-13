from django.forms import model_to_dict
from gestion.models.camareros import Camareros
from tokenapi.http import JsonResponse
from api.decorators.uid_activo import verificar_uid_activo
from comunicacion.tools import comunicar_cambios_devices

import json

#Utilizado por los comanderros
@verificar_uid_activo
def listado(request):
    lista = Camareros.objects.all()
    objs = []
    for l in lista:
        objs.append(model_to_dict(l))
    return JsonResponse(objs)


@verificar_uid_activo
def camarero_add(request):
    id_local = request.POST["id_local"]
    nombre = request.POST["nombre"]
    apellidos = request.POST["apellidos"]
    
    # Verificar si ya existe un camarero con el mismo nombre y apellidos (case-insensitive)
    if Camareros.objects.filter(nombre__iexact=nombre, apellidos__iexact=apellidos).exists():
        return JsonResponse({"error": "El camarero ya existe"})
    
    c = Camareros()
    c.nombre = nombre
    c.apellidos = apellidos
    c.save()
    comunicar_cambios_devices(tb="camareros", op="rm", obj={'id': int(id_local)})
    return JsonResponse("success")

@verificar_uid_activo
def authorize_waiter(request):
    waiter_id = request.POST["id"]
    autorizado = request.POST["autorizado"]
    cam = Camareros.objects.get(pk=waiter_id)
    cam.autorizado = autorizado
    cam.save()
    return JsonResponse("success")

@verificar_uid_activo
def crear_password(request):
    cam = request.POST["cam"]
    password = request.POST["password"]
    cam = json.loads(cam)
    id = cam["ID"]
    
    
    camarero =  Camareros.objects.get(pk=id)
    camarero.pass_field = password
    camarero.save()
   
    return JsonResponse("success")




@verificar_uid_activo
def comprobar(request):
    """
    Vista que recibe una lista de camareros y llama a compare_regs
    para comunicar los cambios entre cliente y servidor
    """
    try:
        # Obtener la lista de camareros desde el POST
        camareros_list = request.POST.get("camareros", "[]")
        camareros = json.loads(camareros_list)
        
        # Llamar al método compare_regs del modelo Camareros
        result = Camareros.compare_regs(camareros)
        
        # Devolver el resultado de la comparación
        return JsonResponse(result, safe=False)
        
    except json.JSONDecodeError:
        return JsonResponse({"error": "Formato JSON inválido"}, status=400)
    except Exception as e:
        return JsonResponse({"error": str(e)}, status=500)