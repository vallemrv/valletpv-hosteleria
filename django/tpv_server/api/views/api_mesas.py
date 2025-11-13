# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:09:05+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-06T19:08:26+02:00
# @License: Apache License v2.0

from gestion.models.mesas import Mesas, Zonas, Mesasabiertas

from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
def ls_zonas(request):
    lsZonas = []
    for z in Zonas.objects.all():
        obj = {
            "ID": z.id,
            "Nombre": z.nombre,
            "Tarifa": z.tarifa,
            "Color": "",
            "RGB": z.rgb,
        }
        lsZonas.append(obj)
    return JsonResponse(lsZonas)

@csrf_exempt
def lstodaslasmesas(request):
    return JsonResponse([])

@csrf_exempt
def lsmesasabiertas(request):
    lstObj = []
    if 'idz' in request.POST:
        idz = request.POST["idz"]
        mesas = Mesasabiertas.objects.filter(mesa__mesaszona__zona__pk=idz)
    else:
        mesas = Mesasabiertas.objects.all()

    for m in mesas:
        obj = {
            "ID": m.id,
            "UID": m.infmesa.pk,
            "IDMesa": m.mesa_id,
            "num": m.infmesa.numcopias,
            "Hora": m.infmesa.hora,
            "NomMesa": m.mesa.nombre,
            "RGB": m.mesa.mesaszona_set.all().first().zona.rgb

        }
        lstObj.append(obj)

    return JsonResponse(lstObj)
