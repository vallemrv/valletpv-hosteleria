from django.forms import model_to_dict
from django.http import HttpResponse
from tokenapi.http import JsonResponse

from api_android.tests.tests_autorizaciones import *
from api_android.tests.tests_websocket import *
from api_android.tests.tests_mails import *
from api_android.tests.tests_ventas import *



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


def composicion(request):
    from gestion.models import Mesasabiertas, ComposicionTeclas
    mesa = Mesasabiertas.objects.all().first()
    obj = []
    if mesa:
        for l in mesa.infmesa.lineaspedido_set.all():
            obj.append({"nombre":l.nombre, "estado":l.estado})
           
    return JsonResponse(obj)


