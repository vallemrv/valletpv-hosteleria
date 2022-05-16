import json
import os
from sys import stdout
from xml.etree.ElementInclude import include
from django.shortcuts import render
from tokenapi.http import JsonResponse
from django.apps import apps
from django.forms.models import model_to_dict
from tokenapi.decorators import token_required
from django.conf import settings
from gestion.models import Secciones, Teclas, Teclaseccion
from datetime import datetime
from django.core.management import call_command

def inicio(request):
    return render(request, "index.html")

@token_required
def reset_db(request):
    media = settings.MEDIA_ROOT
    tablas = [
        "efectivo",
        "gastos",
        "arqueocaja",
        "cierrecaja",
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

    return JsonResponse(tecla.serialize())



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
    obj = add_reg_handler(app_name, tb_name, reg);

    return JsonResponse(obj)

@token_required
def mod_regs(request):
    insts = json.loads(request.POST["isnts"])
    for inst in insts:
        if inst["tipo"] == "md":
           modifcar_reg(inst)
        elif inst["tipo"] == "rm":
           delete_reg(inst)
        elif inst["tipo"] == "add":
            app_name = inst["app"] if "app" in inst else "gestion"
            tb_name = inst["tb"]
            reg = inst["reg"]
            add_reg_handler(app_name, tb_name, reg)
        elif inst["tipo"] == "md_teclados":
            mod_teclados(inst)
            
    return JsonResponse("success")

def add_reg_handler(app_name, tb_name, reg):
    model = apps.get_model(app_name, tb_name)
    obj = model()
    for key in reg:
        k_lower = key.lower()
        if hasattr(obj, k_lower):
            field = getattr(obj, k_lower)
            if "models" in str(type(field)):
                attr = field.__class__.objects.get(pk=reg[key])
            elif "NoneType" in str(type(field)):
                attr = reg[key]
                k_lower = k_lower+"_id"
            else:
                attr = reg[key] 
            setattr(obj, k_lower, attr)  
        elif hasattr(model, k_lower):
            setattr(obj, k_lower+"_id", reg[key])      
        elif "__" in key:
                attr, field, str_parent = key.split("__")
                parent = apps.get_model(app_name, str_parent)
                filter = {field:reg[key]}
                p = parent.objects.filter(**filter).first()
                if p:
                    setattr(obj, attr, p)

    obj.save()

    if (hasattr(obj, "serialize")):
        obj = obj.serialize()
    else:
        obj = model_to_dict(obj)

    return {"reg":json.dumps(obj)}

def modifcar_reg(inst):
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb"]
    reg = inst["reg"]
    filter = ""
    if "filter" in inst:
        filter = inst["filter"] 
    elif "id" in inst:
        filter =  {"id": inst["id"]}
    elif "ID" in inst:
        filter = {"id": inst["ID"]}
    

    model = apps.get_model(app_name, tb_name)

    obj = model.objects.filter(**filter).first()
    if (obj):
        for key in reg:
            k_lower = key.lower()
            if hasattr(obj, k_lower):
                field = getattr(obj, k_lower)
                if "models" in str(type(field)):
                    attr = field.__class__.objects.get(pk=reg[key])
                else:
                    attr = reg[key] 
                setattr(obj, k_lower, attr)      
            else:
                if "__" in key:
                    attr, field, str_model = key.split("__")
                    parent = apps.get_model(app_name, str_model)
                    filter = {field:reg[key]}
                    p = parent.objects.filter(**filter).first()
                    if p:
                        setattr(obj, attr, p)

        obj.save()

def delete_reg(inst):
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb"]
    filter = inst["filter"] if "filter" in inst else  {"id": inst["id"]}
    model = apps.get_model(app_name, tb_name)

    obj = model.objects.filter(**filter).first()
    if (obj):
        obj.delete()


def mod_teclados(inst):
    item = inst["reg"]
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb_mod"]
    filter = inst["filter"]
    fl = {}
    for f in filter:
        fl[f+'__pk'] = item[f]
    
    model = apps.get_model(app_name, tb_name)
    r = model.objects.filter(**fl)
    r.delete()
    
    return add_reg_handler(app_name, tb_name, item)