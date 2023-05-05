import json
import os
from sys import stdout
from datetime import datetime
from tokenapi.decorators import token_required
from tokenapi.http import JsonResponse

from django.shortcuts import render
from django.apps import apps
from django.forms.models import model_to_dict
from django.conf import settings
from django.core.management import call_command

from comunicacion.tools import comunicar_cambios_devices
from api_android.tools import send_mensaje_devices
from api_android.tools.mails import getUsuariosMail, send_cierre
from gestion.models import Secciones, Teclas, Teclaseccion, Arqueocaja

def inicio(request):
    return render(request, "app/index.html")


@token_required
def send_cierre_by_id(request):
    if ("id" in request.POST):
        arqueo = Arqueocaja.objects.filter(pk=request.POST["id"]).order_by('-id').first()
    else:
        arqueo = Arqueocaja.objects.all().order_by('-id').first()

    if arqueo:
        users = getUsuariosMail()
        for us in users:
            send_cierre(us, arqueo.get_desglose_cierre())
        return JsonResponse({'res':"success"})
    else:
        return JsonResponse({'res':"No hay arqueos"})

@token_required
def get_datos_empresa(request):
    return JsonResponse({'nombre':settings.BRAND, "email": settings.MAIL})

@token_required
def reset_db(request):
    media = settings.MEDIA_ROOT
    tablas = [
        "efectivo",
        "gastos",
        "arqueocaja",
        "cierrecaja",
        "mesasabiertas",
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
        
    obj = tecla.serialize()
    comunicar_cambios_devices("md", "teclas", obj)
    return JsonResponse(obj)

@token_required
def getlistado(request):
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tb_name = request.POST["tb"]
    filter = json.loads(request.POST["filter"]) if "filter" in request.POST else {}
    model = apps.get_model(app_name, tb_name)
    objs = model.objects.filter(**filter)
    regs = []
    for obj in objs:
        if hasattr(obj, "serialize"):
            regs.append(obj.serialize())
        else:
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
        if hasattr(model, k_lower):
            field = getattr(model, k_lower)
            attr = reg[key]
            if "ForwardManyToOneDescriptor" in field.__class__.__name__:
                k_lower =  k_lower+"_id"
            else:
                attr = reg[key] 
            setattr(obj, k_lower, attr)  
        

    obj.save()

    if (hasattr(obj, "serialize")):
        obj = obj.serialize()
    else:
        obj = model_to_dict(obj)
    
    update = {
        "op": "insert",
        "device": "",
        "tb": tb_name,
        "obj": obj,
        "receptor": "devices",
    }
    send_mensaje_devices(update) 

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
            attr = reg[key]
            if hasattr(obj, k_lower):
                field = getattr(model, k_lower)
                if "ForwardManyToOneDescriptor" in field.__class__.__name__:
                    k_lower =  k_lower+"_id"
                
            setattr(obj, k_lower, attr)        
            
        
        obj.save()

        if(tb_name == "tecladoscom"):
            tecla = Teclas.objects.filter(pk = inst["id"]).first()
            obj = tecla.serialize()
        else:
            obj = obj.serialize() if hasattr(obj, "serialize") else model_to_dict(obj)
            
        update = {
            "op": "md",
            "device": "",
            "tb": tb_name,
            "obj":obj,
            "receptor": "devices",
            }
        
        send_mensaje_devices(update) 

def delete_reg(inst):
    app_name = inst["app"] if "app" in inst else "gestion"
    tb_name = inst["tb"]
    filter = inst["filter"] if "filter" in inst else  {"id": inst["id"]}
    model = apps.get_model(app_name, tb_name)

    objs = model.objects.filter(**filter)
    for obj in objs:
        update = {
            "op": "rm",
            "device": "",
            "tb": tb_name,
            "obj": {'id': obj.pk},
            "receptor": "devices",
            }
        send_mensaje_devices(update) 
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