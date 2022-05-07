# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:34:14+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-01T16:43:14+01:00
# @License: Apache License v2.0

from api_android.tools import send_update_ws
from tokenapi.http import JsonError, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.forms.models import model_to_dict
from django.http import HttpResponse
from gestion.models import Camareros
import json

@csrf_exempt
def sel_camareros(request):
    lista = json.loads(request.POST["lista"])
    camarero = request.POST["camarero"] if 'camarero' in request.POST else {}
    Camareros.objects.filter().update(autorizado=False)
    for l in lista:
        c = Camareros.objects.get(id=l["ID"])
        c.autorizado = True
        c.save()

    #enviar notficacion de update
    update = {
       "OP": "CAMBIO_TURNO",
       "receptor": "comandas",
    }
    send_update_ws(update)
    return JsonResponse({"cam":camarero})

@csrf_exempt
def listado_activos(request):
    camareros =  Camareros.objects.filter(activo=True)
    lstObj = []
    for c in camareros:
        obj = {
            "ID": c.id,
            "Nombre": c.nombre,
            "Apellidos": c.apellidos,
            "Email": c.email,
            "Pass": c.pass_field,
            "Activo": c.activo
        }
        lstObj.append(obj)

    return JsonResponse(lstObj)

@csrf_exempt
def listado_autorizados(request):
    camareros =  Camareros.objects.filter(autorizado=True)
    lstObj = []
    for c in camareros:
        obj = {
            "ID": c.id,
            "Nombre": c.nombre,
            "Apellidos": c.apellidos,
            "Email": c.email,
            "Pass": c.pass_field,
            "Activo": c.activo
        }
        lstObj.append(obj)

    return JsonResponse(lstObj)

@csrf_exempt
def listado(request):
    camareros =  Camareros.objects.filter(activo=True, autorizado=True)
    lstObj = []
    for r in camareros:
        obj = {
            "ID":r.id,
            "Nombre": r.nombre,
            "Apellidos": r.apellidos,
            "Pass": r.pass_field,
            "autorizado": r.autorizado,
            "permisos": r.permisos
        }
        lstObj.append(obj)

    return JsonResponse(lstObj)


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

@csrf_exempt
def es_autorizado(request):
    id = request.POST["id"]
    try:
        id = json.loads(id)
        id = id["ID"]
        try:
            camarero =  Camareros.objects.get(pk=id)
            if camarero.autorizado and camarero.activo:
                return JsonResponse({"autorizado": True, "cam": request.POST["id"]})
            else:
                return JsonResponse({"autorizado": False})
        except:
            return JsonResponse({"autorizado": False})
    except:
        return JsonResponse({"autorizado": True, "cam": request.POST["id"]})


@csrf_exempt
def camarero_add(request):
    nombre = request.POST["nombre"]
    apellido = request.POST["apellido"]
    camarero = Camareros()
    camarero.nombre = nombre
    camarero.apellidos = apellido
    camarero.autorizado = 1
    camarero.save()
    return JsonResponse("success")
