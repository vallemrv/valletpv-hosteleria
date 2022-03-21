# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-08-02T15:16:09+02:00
# @License: Apache License v2.0

from api_android.tools import send_update_ws
from tokenapi.http import JsonError, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.forms.models import model_to_dict
            
from gestion.models import *
import json

from datetime import datetime

@csrf_exempt
def getupdate(request):
    res = {"hora": datetime.now().strftime("%Y/%m/%d - %H:%M:%S"), 'Tablas':[]}
    try:
        hora = request.POST["hora"] if 'hora' in request.POST else ""
        if hora == '':
            res = {
                'hora': datetime.now().strftime("%Y/%m/%d - %H:%M:%S"),
                'Tablas': [
                   {"Tabla": 'Camareros'},
                   {"Tabla": 'Zonas'},
                   {"Tabla": 'Secciones'},
                   {"Tabla": 'MesasAbiertas'},
                   {"Tabla": 'SubTeclas'},
                   {"Tabla": 'TeclasCom'},
                ]
            }
        else:
            sync = Sync.objects.filter(modificado__gte=res['hora'])
            res['hora'] = datetime.now().strftime("%Y/%m/%d - %H:%M:%S")
            res['Tablas'] = []
            for s in sync:
                res["Tablas"].append({"Tabla": s.tabla})
    except:
        print("error en sincronizacion")
    return JsonResponse(res)

@csrf_exempt
def get_eco(request):
    #enviar notficacion de update
    code = request.POST["code"]
    update = {
       "OP": "ECO_ECO",
       "receptor": "comandas",
       "code": code
    }
    send_update_ws(request, update)
    return JsonResponse("success")


@csrf_exempt
def know_connect(request):
    return JsonResponse("success")

@csrf_exempt
def lastsync(request):
    return JsonResponse("success")


@csrf_exempt
def firstsync(request):
    import ast
    ts = ast.literal_eval(request.POST["tablas"])
    respose = {
        "Tablas":[],
        "lastsync":""
    }

    
    for t in ts:
        is_registro = False
        if t == 'Zonas':
            is_registro = False
            objs = Zonas.objects.all()
        elif t== "Mesas":
            is_registro = True
            objs = Mesas.get_all_for_devices()        
        elif t == "SeccionesCom":
            is_registro = True
            objs = SeccionesCom.get_all_for_devices()
        elif t == "Teclas":
            is_registro = True
            objs = Teclas.get_all_for_devices()
        elif t == "Sugerencias":
            is_registro = True
            objs = SeccionesCom.get_all_for_devices()
        elif t == "Subteclas":
            is_registro = True
            objs = SeccionesCom.get_all_for_devices()
        elif t == "Pendientes":
            is_registro = True
            objs = SeccionesCom.get_all_for_devices()
        else:
            objs = []

        sync = Sync.objects.filter(nombre__icontains=t).first()
        if not sync:
            sync =  Sync(nombre=t)
            sync.save()
        
        aux = {'Tabla': t, 'Registros': []}
        
       
        for obj in objs:
            columns = []
            values = []
            if not is_registro:
                obj = model_to_dict(obj)
            for k, v in obj.items():
                columns.append(k)
                values.append("'%s'" % v)
            reg = "INSERT INTO %s (%s) VALUES (%s);" % (t, ", ".join(columns), ", ".join(values))
            aux["Registros"].append(reg)
            respose["Tablas"].append(aux)
            
    respose["lastsync"] =   "%s" % datetime.now.last.strftime("%Y/%m/%d, %H:%M:%S")      
    print(respose)
    return JsonResponse(respose)


