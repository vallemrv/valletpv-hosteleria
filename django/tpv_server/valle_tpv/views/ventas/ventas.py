import json
import os
from datetime import datetime
from tokenapi.decorators import token_required
from tokenapi.http import JsonResponse

from django.shortcuts import render
from django.apps import apps
from django.forms.models import model_to_dict
from django.conf import settings
from django.core.management import call_command

from django.tpv_server.valle_tpv.tools.tools import comunicar_cambios_devices,  send_mensaje_devices
from valle_tpv.api.tools.mails import getUsuariosMail, send_cierre
from valle_tpv.models import Secciones, Teclas, Teclaseccion, Arqueocaja

def inicio(request):
    return render(request, "app/index.html")


@token_required
def send_cierre_by_id(request):
    if ("id" in request.POST):
        arqueo = Arqueocaja.objects.filter(pk=request.POST["id"]).order_by('-id').first()
    else:
        arqueo = Arqueocaja.objects.all().order_by('-id').first()

    if arqueo:
        users = getUsuariosMail()
        for us in users:
            send_cierre(us, arqueo.get_desglose_cierre())
        return JsonResponse({'res':"success"})
    else:
        return JsonResponse({'res':"No hay arqueos"})

@token_required
def get_datos_empresa(request):
    return JsonResponse({'nombre':settings.BRAND, "email": settings.MAIL})

@token_required
def reset_db(request):
    media = settings.MEDIA_ROOT
    tablas = [
        "efectivo",
        "gastos",
        "arqueocaja",
        "cierrecaja",
        "mesasabiertas",
        "lineaspedido",
        "pedidos",
        "infmesa",
        "historialnulos",
        "camareros",
        "ticket"
    ]
    models = [] 
    for t in tablas:
        models.append("gestion." + t)
    file = os.path.join(media, datetime.now().strftime("%Y_%m_%d_%H_%M_%S")+".json")
    with open(file, "w") as f:
        call_command("dumpdata", *models,  stdout=f)
    
    for m in tablas:
        model = apps.get_model("gestion", m)
        if (m != "camareros"):
            model.objects.all().delete()
        else:
            model.objects.filter(activo=0).delete()
    
    return JsonResponse("success")
    

#este es especifico para las teclasseccion
@token_required
def mod_sec(request):
    item = json.loads(request.POST["item"])
    Teclaseccion.objects.filter(tecla__pk=item['id']).delete()
    tecla = Teclas.objects.get(pk=item['id'])
    main_sec = Secciones.objects.filter(nombre=item["main_sec"]).first()
    secundary_sec = Secciones.objects.filter(nombre=item["secundary_sec"]).first()
    
    if main_sec:
        sec = Teclaseccion()
        sec.tecla = tecla
        sec.seccion = main_sec
        sec.save()
    
    if secundary_sec:
        sec = Teclaseccion()
        sec.tecla = tecla
        sec.seccion = secundary_sec
        sec.save()
        
    obj = tecla.serialize()
    comunicar_cambios_devices("md", "teclas", obj)
    return JsonResponse(obj)

   