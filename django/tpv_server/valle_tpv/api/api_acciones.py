## impotar librerias
from django.apps import apps
from tokenapi.decorators import token_required
from tokenapi.http import  JsonResponse
from django.forms import model_to_dict
from valle_tpv.tools.acciones import add_handler, modifcar_handler, delete_handler
import json


@token_required
def add_reg(request):
    app_name = request.POST["app"] if "app" in request.POST else "valle_tpv"
    tb_name = request.POST["tb_name"]
    reg = json.loads(request.POST["reg"])
    model = apps.get_model(app_name, tb_name)
    if hasattr(model, "add_handler"):
        obj = model.add_handler(reg)
    else:
        obj = add_handler(model, tb_name, reg)

    return JsonResponse(obj)

@token_required
def delete_reg(request):
    app_name = request.POST["app"] if "app" in request.POST else "valle_tpv"
    tb_name = request.POST["tb_name"]
    filter = json.loads(request.POST["filter"])
    model = apps.get_model(app_name,  tb_name)
    if hasattr(model, "delete_handler"):
        obj = model.delete_handler(filter)
    else:
        obj = delete_handler(model, tb_name, filter)

    return JsonResponse(obj)

@token_required
def update_reg(request):
    app_name = request.POST["app"] if "app" in request.POST else "valle_tpv"
    tb_name = request.POST["tb_name"]
    filter = json.loads(request.POST["filter"])
    model = apps.get_model(app_name,  tb_name)
    if hasattr(model, "modifcar_handler"):
        obj = model.modifcar_handler(filter)
    else:
        obj = modifcar_handler(model, tb_name, filter)

    return JsonResponse(obj)




@token_required
def exec_inst(request):
    insts = json.loads(request.POST["isnts"])
    result = []
    for inst in insts:
        app_name = inst["app"] if "app" in inst else "valle_tpv"
        tb_name = inst["tb"]
        reg = inst["reg"] if "reg" in inst else None
        filter = inst["filter"]
        model = apps.get_model(app_name,  tb_name)
        obj = None
        if inst["tipo"] == "md":
            if hasattr(model, "modifcar_handler"):
                obj = model.modificar_handler(tb_name, reg, filter)
            else:
                obj = modifcar_handler(model, tb_name, reg, filter)
                 
        elif inst["tipo"] == "rm":
            if hasattr(model, "delete_handler"):
                obj = model.delete_handler(tb_name, filter)
            else:
                obj = delete_handler(model, tb_name, filter)

            result.append(obj)
            obj = None
        elif inst["tipo"] == "add":
            if hasattr(model, "add_handler"):
                obj = model.add_handler(tb_name, reg)
            else:
                obj = add_handler(model, tb_name, reg)
        
        if obj:  
            result.append(obj.serialize() if hasattr(obj, "serialize") else model_to_dict(obj))
          
    return JsonResponse(result)


