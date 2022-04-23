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
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tbs = json.loads(request.POST["tbs"])
    filters = json.loads(request.POST["filters"]) if "filters" in request.POST else {}
    tablas = []
    for tb_name in tbs:
        model = apps.get_model(app_name, tb_name)
        filter = filters[tb_name] if tb_name in filters else {}
        objs = model.objects.filter(**filter)
        regs = []
        for obj in objs:
            regs.append(model_to_dict(obj))
        tablas.append({"tb": tb_name, "regs": regs})

    return JsonResponse({'tablas':tablas})


@token_required
def get_teclados(request):
    return JsonResponse([
    {"tb":"teclas", 'regs': Teclas.update_for_devices()},
    {"tb": "secciones","regs": Secciones.update_for_devices()},
    {"tb": "subteclas", "regs": Subteclas.update_for_devices()}
    ])

@token_required
def mod_regs(request):
    insts = json.loads(request.POST["isnts"])
    for inst in insts:
        app_name = inst["app"] if "app" in inst else "gestion"
        tb_name = inst["tb"]
        reg = inst["reg"]
        id = inst["id"]
        model = apps.get_model(app_name, tb_name)

        obj = model.objects.filter(id=id).first()
        if (obj):
            obj.save(**reg)

    return JsonResponse()


