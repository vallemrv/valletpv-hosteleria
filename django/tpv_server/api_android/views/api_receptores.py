# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-25T00:15:22+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonError, JsonResponse
from gestion.models import Receptores
from django.views.decorators.csrf import csrf_exempt

from datetime import datetime
import json

@csrf_exempt
def get_lista(request):
    lista = []
    for r in Receptores.objects.all():
        lista.append(
            {
               'ID': r.id,
               'Nombre': r.nombre,
               'Activo': r.activo,
               'receptor': r.nomimp,
            }
        )
    return JsonResponse(lista)

@csrf_exempt
def set_settings(request):
    lista = json.loads(request.POST["lista"])
    for l in lista:
        Receptores.objects.filter(pk=l["ID"]).update(activo=l["Activo"])
    return JsonResponse({})
