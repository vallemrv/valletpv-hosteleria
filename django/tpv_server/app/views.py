import json
from django.shortcuts import render
from tokenapi.http import JsonResponse
from django.apps import apps
from django.forms.models import model_to_dict
from tokenapi.decorators import token_required
from gestion.models import Teclas, Subteclas, Secciones

def inicio(request):
    return render(request, "index.html")


@token_required
def getlistado(request):
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tb_name = request.POST["tb"]
    filter = json.loads(request.POST["filter"]) if "filter" in request.POST else {}
    model = apps.get_model(app_name, tb_name)
    objs = model.objects.filter(**filter)
    regs = []
    for obj in objs:
        regs.append(model_to_dict(obj))

    return JsonResponse({'tb':tb_name, "regs": regs})


@token_required
def get_listado_compuesto(request):
    tbs = json.loads(request.POST["tbs"])
    tablas = []
    for tb_name in tbs:
        model = apps.get_model("gestion", tb_name)
        regs = model.update_for_devices()
        tablas.append({"tb": tb_name, "regs": regs})

    return JsonResponse(tablas)


@token_required
def add_reg(request):
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tb_name = request.POST["tb_name"]
    reg = json.loads(request.POST["reg"])
    model = apps.get_model(app_name, tb_name)
    obj = model(**reg)
    obj.save()
    return JsonResponse({"reg":json.dumps(model_to_dict(obj))})

@token_required
def mod_regs(request):
    insts = json.loads(request.POST["isnts"])
    for inst in insts:
        if inst["tipo"] == "md":
            modifcar_reg(inst)
        elif inst["tipo"] == "rm":
            delete_reg(inst)
          

    return JsonResponse("success")


def modifcar_reg(inst):
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb"]
    reg = inst["reg"]
    id = inst["id"]
    model = apps.get_model(app_name, tb_name)

    obj = model.objects.filter(id=id).first()
    if (obj):
        for key in reg:
            setattr(obj, key, reg[key])
            
        obj.save()

def delete_reg(inst):
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb"]
    id = inst["id"]
    model = apps.get_model(app_name, tb_name)

    obj = model.objects.filter(id=id).first()
    if (obj):
        obj.delete()