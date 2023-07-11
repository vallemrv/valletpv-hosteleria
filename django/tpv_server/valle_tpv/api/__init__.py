# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:20:27+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-01T14:33:04+01:00
# @License: Apache License v2.0

from .api_sync import *
from .api_pedidos import *
from .api_comandas import *
from .api_cuenta import *
from .api_impresion import *
from .api_sugerencias import *
from .api_arqueos import *
from .api_receptores import *
from .api_nulos import *
from .api_autorizaciones import *
from .api_dash_board import *
from .api_user_profile import *
from django.conf import settings
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
def get_datos_empresa(request):
    return JsonResponse({'nombre':settings.BRAND, "email": settings.MAIL})


@csrf_exempt
def get_uuid_factura(request, num):
    from gestion.models import Ticket
    ticket = Ticket.objects.filter(id=num).first()
    if ticket:
        return JsonResponse({'id':ticket.id,'uid':ticket.uid})
    else:
        return JsonError("Ticket no valido")