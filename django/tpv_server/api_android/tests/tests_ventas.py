from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from gestion.models import Arqueocaja, Sync
from api_android.tools.mails import getUsuariosMail, send_cierre


@csrf_exempt
def send_last_cierre(request):
    if ("id" in request.POST):
        arqueo = Arqueocaja.objects.filter(pk=request.POST["id"]).order_by('-id').first()
    else:
        arqueo = Arqueocaja.objects.all().order_by('-id').first()
        
    if arqueo:
        users = getUsuariosMail()
        for us in users:
            send_cierre(us, arqueo.get_desglose_cierre())

        return HttpResponse("success")
    else:
        return HttpResponse("No hay arqueos")


def actualiza_sync(request, tb_name):
    Sync.actualizar(tb_name=tb_name)
    return HttpResponse("success")
