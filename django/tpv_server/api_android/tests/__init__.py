from django.http import HttpResponse


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