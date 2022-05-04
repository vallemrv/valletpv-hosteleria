# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-08-02T15:16:09+02:00
# @License: Apache License v2.0

import json
from multiprocessing.dummy import JoinableQueue
from tokenapi.http import  JsonResponse
from django.views.decorators.csrf import csrf_exempt         
from gestion.models import *


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
    if t == 'camareros':
        tbModel = Camareros
    elif t == 'mesas':
        tbModel = Mesas
    elif t == "zonas":
        tbModel = Zonas
    elif t == "secciones":
        tbModel = Secciones
    elif t == "teclas":
        tbModel = Teclas
    elif t in ["cuenta", "lineaspedido"]:
        tbModel = Lineaspedido
    elif t == "mesasabiertas":
        tbModel = Mesasabiertas
    elif t in  ["seccionescom", "secciones_com"]:
        tbModel = SeccionesCom
    elif t == "sugerencias":
        tbModel = Sugerencias
    elif t == "subteclas":
        tbModel = Subteclas
    
    
    if tbModel:
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
    tbs = []

    if tb == "camareros":
        tbs.append(tb)
        for row in rows:
            Camareros.update_from_device(row)
    
    return JsonResponse(
         {"tb": tb, 
          "last":datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
          })