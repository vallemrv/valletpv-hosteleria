# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-19T23:54:21+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-07T08:45:00+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from gestion.models import Secciones

@csrf_exempt
def sec_listado(request):
    lstSecciones = []
    secciones = Secciones.objects.all().order_by('-orden')
    for s in secciones:
        obj = {
            "ID": s.pk,
            "Color": "",
            "RGB": s.rgb,
            "Orden": s.orden,
            "Nombre": s.nombre
        }
        lstSecciones.append(obj)
    return JsonResponse(lstSecciones)
