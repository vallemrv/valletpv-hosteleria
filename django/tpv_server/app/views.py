import json
from attr import field
from django.shortcuts import render
from tokenapi.http import JsonResponse
from django.apps import apps
from django.forms.models import model_to_dict
from tokenapi.decorators import token_required
from gestion.models import Secciones, Teclas, Teclaseccion

def inicio(request):
    return render(request, "index.html")

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

    return JsonResponse({"reg":json.dumps(model_to_dict(obj))})

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
            
    return JsonResponse("success")

def add_reg_handler(app_name, tb_name, reg):
    model = apps.get_model(app_name, tb_name)
    obj = model()
   
    
    for key in reg:
        if hasattr(obj, key):
            field = getattr(obj, key)
            if "models" in str(type(field)):
                attr = field.__class__.objects.get(pk=reg[key])
            elif "nonetype" in str(type(field)):
                attr = reg[key]
                key = key+"_id"
            else:
                attr = reg[key] 
            setattr(obj, key, attr)  
        elif hasattr(model, key):
            setattr(obj, key+"_id", reg[key])      
        elif "__" in key:
                attr, field, str_parent = key.split("__")
                parent = apps.get_model(app_name, str_parent)
                filter = {field:reg[key]}
                p = parent.objects.filter(**filter).first()
                if p:
                    setattr(obj, attr, p)

    obj.save()
    return obj

def modifcar_reg(inst):
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb"]
    reg = inst["reg"]
    filter = inst["filter"] if "filter" in inst else  {"id": inst["id"]}
    model = apps.get_model(app_name, tb_name)

    obj = model.objects.filter(**filter).first()
    if (obj):
        for key in reg:
            if hasattr(obj, key):
                field = getattr(obj, key)
                if "models" in str(type(field)):
                    attr = field.__class__.objects.get(pk=reg[key])
                else:
                    attr = reg[key] 
                setattr(obj, key, attr)      
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