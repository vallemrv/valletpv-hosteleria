# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:20:27+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-01T14:33:04+01:00
# @License: Apache License v2.0

from .api_sync import *
from .api_camareros import *
from .api_pedidos import *
from .api_mesas import *
from .api_comandas import *
from .api_cuenta import *
from .api_impresion import *
from .api_articulos import *
from .api_sugerencias import *
from .api_secciones import *
from .api_arqueos import *
from .api_receptores import *
from .api_nulos import *

from api_android.tools import send_update_ws
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
def send_test_mensaje(request, msg):
    #enviar notficacion de update
    update = {
       "OP": "MENSAJE",
       "msg": msg,
       "receptor": "comandas",
    }
    send_update_ws(request, update)
    return HttpResponse("success")

@csrf_exempt
def reparar_subteclas(request):
    from gestion.models import Subteclas

    sub = Subteclas.objects.all()
    for s in sub:
        try:
            tecla = s.tecla
        except:
            s.delete()
        

    return HttpResponse("success")
