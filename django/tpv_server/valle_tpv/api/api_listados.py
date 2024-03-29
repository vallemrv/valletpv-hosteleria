from tokenapi.decorators import token_required
from tokenapi.http import JsonResponse
from django.apps import apps
from django.forms import model_to_dict
from valle_tpv.models import Camareros
import json


@token_required
def permissions_list(request):
   return JsonResponse(Camareros.permisos_list())



@token_required
def listado_compuesto(request):
    tbs = json.loads(request.POST["tbs"])
    app_name = request.POST.get("app", "valle_tpv")
   
    tablas = []
    for modelo in tbs:
        model = apps.get_model(app_name, modelo)
        regs = []
        for reg in model.objects.all():
            if hasattr(reg, "serialize"):
                regs.append(reg.serialize())
            else:
                regs.append(model_to_dict(reg))
        tablas.append({"tb": modelo, "regs": regs})

    return JsonResponse(tablas)

@token_required
def listado(request):
    
    app_name = request.POST.get("app", "valle_tpv")
    tb_name = request.POST.get("tb_name")
    filter = json.loads(request.POST.get("filter", "{}"))
    limit = int(request.POST.get("limit", -1))
    offset = int(request.POST.get("offset", 0))
    
    try:
        model = apps.get_model(app_name, tb_name)
        objs = model.objects.filter(**filter)[offset:offset+limit] if limit > 0 else model.objects.filter(**filter)
        
        regs = []
        for obj in objs:
            if hasattr(obj, "serialize"):
                regs.append(obj.serialize())
            else:
                regs.append(model_to_dict(obj))

    except Exception as e:
        print(app_name, tb_name, e)
        regs = []
            
    return JsonResponse({"tb": tb_name, "regs": regs})