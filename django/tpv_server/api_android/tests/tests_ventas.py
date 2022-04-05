from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from gestion.models import Arqueocaja
from api_android.tools.mails import getUsuariosMail, send_cierre


@csrf_exempt
def send_last_cierre(request):
    arqueo = Arqueocaja.objects.all().order_by('-id').first()

    users = getUsuariosMail()
    for us in users:
        send_cierre(us, arqueo.get_desglose_cierre())

    return HttpResponse("success")