from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from gestion.models import Arqueocaja, Sync
from api_android.tools.mails import getUsuariosMail, send_cierre


@csrf_exempt
def send_last_cierre(request):
    arqueo = Arqueocaja.objects.order_by('-id')
    
    if "id" in request.POST:
        arqueo = arqueo.filter(pk=request.POST["id"])

    arqueo = arqueo.first()
    
    if arqueo:
        users = getUsuariosMail()
        desglose = arqueo.get_desglose_cierre()
        
        for us in users:
            send_cierre(us, desglose)
            
        return HttpResponse("success")
        
    return HttpResponse("No hay arqueos")


def actualiza_sync(request, tb_name):
    Sync.actualizar(tb_name=tb_name)
    return HttpResponse("success")