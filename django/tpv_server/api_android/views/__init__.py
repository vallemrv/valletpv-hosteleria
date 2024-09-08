# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:20:27+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-01T14:33:04+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError
from django.conf import settings
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
def get_datos_empresa(request):
    return JsonResponse({'nombre':settings.BRAND, "email": settings.MAIL})


@csrf_exempt
def get_uuid_factura(request, num):
    from gestion.models.ticket import Ticket
    ticket = Ticket.objects.filter(id=num).first()
    if ticket:
        return JsonResponse({'id':ticket.id,'uid':ticket.uid})
    else:
        return JsonError("Ticket no valido")