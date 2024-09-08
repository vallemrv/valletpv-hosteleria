# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-08-02T15:16:09+02:00
# @License: Apache License v2.0

import json
from tokenapi.http import  JsonResponse
from django.views.decorators.csrf import csrf_exempt         
from django.apps import apps
from datetime import datetime
from gestion.models.pedidos import Lineaspedido
from gestion.models.sync import Sync
from gestion.models.teclados import SeccionesCom


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


@csrf_exempt
def sync_devices(request):
    from api_android.views.api_pedidos import comparar_lineaspedido
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tb_name = request.POST["tb"] 
    if (tb_name == "lineaspedido"): return comparar_lineaspedido(request)
    reg = json.loads(request.POST["reg"])
    model = apps.get_model(app_name, tb_name)
    result = []
    pks = []
    print(tb_name)
   
    for r in reg:
        try:
            key, v = ("ID", r["ID"]) if "ID" in r.keys() else ("id", r['id'])
            if (tb_name == "mesasabiertas"):
                obj = model.objects.filter(mesa__id=v).first()
                if not obj:
                    result.append({"tb":tb_name, "op": "md", "obj":{ 'ID':v, 'abierta': 0, "num":0 }})
                    continue
           
            else:
                obj = model.objects.filter(pk=v).first()
                if not obj:
                    result.append({"tb":tb_name, "op": "rm", "obj":{key:v}})
                    continue
           
            pks.append(v)
           
            obj = obj.serialize()
            
            for k, v in r.items():
                obj_v =  obj[k] if k in obj else obj[k.lower()]
                if not equals(k, str(obj_v), str(v)):
                    result.append({"tb":tb_name, "op": "md", "obj":obj})
                    break

        except Exception as e:
            print(e)
            print(tb_name, r)  
        
   
   
    op = "insert"
    if (tb_name == "mesasabiertas"):
        objs = model.objects.exclude(mesa__id__in=pks)
        op = "md"
    else:    
        objs = model.objects.exclude(pk__in=pks)
        

    for obj in objs:
        obj = obj.serialize()
        
        result.append({"tb":tb_name, "op": op, "obj":obj})     

    return JsonResponse(result)

def equals(k, obj1, obj2):
    # Manejo de valores numéricos
    if k.lower() in ["p1", "p2", "precio", "incremento", "entrega"]:
        try:
            # Intentamos convertir ambos valores a float, si alguno falla, se consideran diferentes
            return float(obj1) == float(obj2)
        except (ValueError, TypeError):
            return False

    # Manejo de None
    if obj1 in [None, "None", "null"] or obj2 in [None, "None", "null"]:
        return obj1 in [None, "None", "null"] and obj2 in [None, "None", "null"]

    # Comparación de strings normalizados
    return str(obj1).strip().lower() == str(obj2).strip().lower()
