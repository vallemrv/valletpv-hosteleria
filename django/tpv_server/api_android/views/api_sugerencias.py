# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-17T00:02:21+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-17T08:41:10+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse
from django.http import HttpResponse
from gestion.models import Sugerencias
from django.views.decorators.csrf import csrf_exempt


@csrf_exempt
def sugerencia_ls(request):
    id = request.POST["id"]
    condicion = {'tecla_id':id}
    if "str" in request.POST:
        condicion["sugerencia__icontains"] = request.POST["str"]
    sug = Sugerencias.objects.filter(**condicion)
    a = []
    for s in sug:
        obj = {
            "ID": s.pk,
            "IDTeclas": s.tecla.pk,
            "Sugerencia": s.sugerencia,
        }
        a.append(obj)
    return JsonResponse(a)

@csrf_exempt
def sugerencia_add(request):
    idart = request.POST["idArt"]
    sug = request.POST["sug"]
    r = Sugerencias.objects.filter(sugerecia=sug, tecla__id=idart).first()
    if sug != "" and not r:
        sugerecia = Sugerencias()
        sugerecia.tecla_id = idart
        sugerecia.sugerencia = sug
        sugerecia.save()

    return HttpResponse("success")
