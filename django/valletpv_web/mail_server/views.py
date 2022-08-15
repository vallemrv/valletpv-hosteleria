from django.core.mail import send_mail
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.shortcuts import render, redirect

def index(resquest):
    return render(resquest, "index.html")

@csrf_exempt
def contactar(resquest):
    if "contactusername" in resquest.POST and 'contactemail' in resquest.POST and 'contactcomment' in resquest.POST:
        send_mail(
            'Contacto valletpv '+resquest.POST["contactusername"],
            resquest.POST["contactcomment"],
            resquest.POST["contactemail"],
            ['manuelrodriguez@valleapp.com'],
            fail_silently=False,
        )
    return redirect( "index")

@csrf_exempt
def suscribirse(resquest):
    if "mail" in resquest.POST:
        from mail_server.models import Suscripcion
        s = Suscripcion(email=resquest.POST["mail"])
        s.save()
        send_mail(
            'Alta suscripción novedades valletpv.',
            "Muchas gracias por suscribirse a nuestras noticias. Le tendremos informado de todas nuestras novedades. Un saludo y fuerza. ",
            "manuelrodriguez@valleapp.com",
            [resquest.POST["mail"]],
            fail_silently=False,
        )
    return redirect( "index")

def valleges(request):
    return render(request, template_name="aplicaciones/valleges/index.html")