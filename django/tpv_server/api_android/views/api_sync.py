# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-08-02T15:16:09+02:00
# @License: Apache License v2.0

import json
from tokenapi.http import  JsonResponse
from django.views.decorators.csrf import csrf_exempt         
from gestion.models import *
from django.apps import apps
from api_android.tools import send_mensaje_devices

from datetime import datetime


@csrf_exempt
def get_tb_up_last(request):
    t = request.POST["tb"]
    tb_sync = Sync.objects.filter(nombre=t).first()
    if not tb_sync:
        tb_sync = Sync();
        tb_sync.nombre = t
        tb_sync.last = datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
        tb_sync.save()

    
    return JsonResponse({"nombre": t, "last": tb_sync.last})

@csrf_exempt
def update_for_devices(request):
    t = request.POST["tb"] 
    
    tbModel = None
    objs = []
    
    if t in ["cuenta", "lineaspedido"]:
        tbModel = Lineaspedido
    elif t in  ["seccionescom", "secciones_com"]:
        tbModel = SeccionesCom
    else:
        tbModel = apps.get_model("gestion", t)
    
    
    if tbModel and hasattr(tbModel, "update_for_devices"):
        objs = tbModel.update_for_devices()
            
    
    tb_sync = Sync.objects.filter(nombre=t).first()
    if not tb_sync:
        tb_sync = Sync();
        tb_sync.nombre = t
        tb_sync.last = datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
        tb_sync.save()

   

    return JsonResponse(
        {"nombre": t, 
        "last": tb_sync.last, 
        "objs": objs})


@csrf_exempt
def update_from_devices(request):
    tb = request.POST["tb"]
    rows = json.loads(request.POST["rows"])
    
    model = apps.get_model("gestion", tb)
    if hasattr(model, "update_from_device"):
        for row in rows:
            model.update_from_device(row)
                   
    
    return JsonResponse(
         {"tb": tb, 
          "last":datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
          })