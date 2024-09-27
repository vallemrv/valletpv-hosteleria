# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-17T00:02:21+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-17T08:41:10+01:00
# @License: Apache License v2.0

from django.forms import model_to_dict
from django.http import HttpResponse
from gestion.models.teclados import Sugerencias
from django.views.decorators.csrf import csrf_exempt
from comunicacion.tools import comunicar_cambios_devices

@csrf_exempt
def sugerencia_add(request):
    idart = request.POST["idArt"]
    sug = request.POST["sug"]
    r = Sugerencias.objects.filter(sugerencia=sug, tecla__id=idart).first()
    if sug != "" and not r:
        sugerencia = Sugerencias()
        sugerencia.tecla_id = idart
        sugerencia.sugerencia = sug
        sugerencia.save()
        r = sugerencia
        
    comunicar_cambios_devices("insert", "sugerencias", model_to_dict(r))

    return HttpResponse("success")
