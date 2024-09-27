# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-25T00:15:22+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse
from gestion.models.familias import Receptores
from django.views.decorators.csrf import csrf_exempt
import json


@csrf_exempt
def get_lista(request):
    lista = []
    for l in Receptores.objects.all().exclude(nombre__icontains="nulo"):
        lista.append({"Nombre": l.nombre, "Activo": l.activo, "ID":l.id, "nomimp": l.nomimp})

    return JsonResponse(lista)

@csrf_exempt
def set_settings(request):
    lista = json.loads(request.POST["lista"])
    for l in lista:
        Receptores.objects.filter(pk=l["ID"]).update(activo=l["Activo"])
    return JsonResponse({})
