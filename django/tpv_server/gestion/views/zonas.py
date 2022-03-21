# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-06T13:15:21+02:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError, HttpResponse
from django.db.models import Q
from django.forms.models import model_to_dict
from django.shortcuts import render, reverse
from app.utility import rgbToHex
from gestion.models import Zonas, Mesas, Mesaszona
from gestion.forms import ZonasForm, MesasForm
from app.utility import rgbToHex
from django.contrib.auth.decorators import login_required


@login_required(login_url='login_tk')
def zonas(request):
    objs = Zonas.objects.all()
    return render(request, "mesas/listado_zonas.html", {"listado": objs,
                                                        "form": ZonasForm()})


@login_required(login_url='login_tk')
def add_zona(request):
    form = ZonasForm(request.POST)
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, obj.tarifa,  rgbToHex(obj.rgb)],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'ver', 'icon':'fa-search'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "tarifa": obj.tarifa,
                              "rgb": rgbToHex(obj.rgb)}
                   }

    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_zona(request, id):
    obj = Zonas.objects.get(pk=id)
    form = ZonasForm(request.POST, instance=obj)
    obj_send= {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, obj.tarifa,  rgbToHex(obj.rgb)],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'ver', 'icon':'fa-search'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "tarifa": obj.tarifa,
                              "rgb": rgbToHex(obj.rgb)}
                   }
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_zona(request, id):
    zonas = Zonas.objects.filter(pk=id)
    zonas.delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def asociar_mesa(request, id, idzona):
    asc = Mesaszona.objects.filter(zona__pk=idzona, mesa__pk=id).first()
    if not asc:
        Mesaszona(zona_id=idzona, mesa_id=id).save()
    
    objs = Mesas.objects.filter(mesaszona__zona__pk=idzona)
    listaObj = []
    for obj in objs:
        nombre = Zonas.objects.get(pk=idzona).nombre
        obj_send = { 'cols': [obj.nombre,  obj.orden, nombre],
                        'botones': [{"tipo":"add", 'icon':'fa-plus'}],
                        'obj': {"id":obj.id, "nombre": obj.nombre, "orden": obj.orden}
                    }
        listaObj.append(obj_send)
    return JsonResponse(listaObj)

@login_required(login_url='login_tk')
def lista_mesas(request):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Mesas.objects.filter(Q(nombre__icontains=filter))
        listaObj = []
        for obj in objs:
            nombres = obj.mesaszona_set.all().values_list("zona__nombre", flat=True)
            nombre = ", ".join(nombres)
            obj_send = { 'cols': [obj.nombre,  obj.orden, nombre],
                         'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                                     {'tipo':'borrar', 'icon':'fa-trash'}],
                         'obj': {"id":obj.id, "nombre": obj.nombre, "orden": obj.orden}
                       }
            listaObj.append(obj_send)

        return JsonResponse(listaObj)
    else:
        objs = Mesas.objects.all()
       
    return render(request, "mesas/listado_mesas.html",
                           {"listado": objs, "form": MesasForm()})

@login_required(login_url='login_tk')
def lista_mesas_by_zona(request, id=-1):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Mesas.objects.filter(Q(nombre__icontains=filter))
        listaObj = []
        for obj in objs:
            nombres = obj.mesaszona_set.all().values_list("zona__nombre", flat=True)
            nombre = ", ".join(nombres)
            obj_send = { 'cols': [obj.nombre,  obj.orden, nombre],
                         'botones': [{"tipo":"add", 'icon':'fa-plus'}],
                         'obj': {"id":obj.id, "nombre": obj.nombre, "orden": obj.ordenm,
                                "color": obj.get_color(hex=True) }
                       }
            listaObj.append(obj_send)

        return JsonResponse(listaObj)
    elif id > -1:
        objs = Mesas.objects.filter(mesaszona__zona__pk=id)
        zona = Zonas.objects.get(pk=id)
    else:
        objs = Mesas.objects.all()
        zona = Zonas(pk=1, nombre="No definido")

    return render(request, "mesas/listado_mesas_by_zona.html",
                           {"listado": objs, "form": MesasForm(),
                           'zona':zona.nombre, 'zona_id': zona.pk})



@login_required(login_url='login_tk')
def add_mesa(request, id=-1):
    form = MesasForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        nombre = ""
        if id > -1:
            union = Mesaszona(mesa_id=obj.pk,zona_id=id)
            union.save()
            m = Zonas.objects.get(pk=id)
            nombre = m.nombre
       

        obj_send = { 'cols': [obj.nombre,  obj.orden, nombre],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "orden": obj.orden}
                   }
    else:
        return JsonError(form.errors)
       
    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_mesa(request, id):
    obj = Mesas.objects.get(pk=id)
    form = MesasForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        mz = obj.mesaszona_set.all().first()
        nombre = ""
        if mz:
            nombre = mz.zona.nombre
        
        obj_send = { 'cols': [obj.nombre,  obj.orden, nombre],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "orden": obj.orden}
                   }
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_mesa(request, id):
    Mesas.objects.filter(pk=id).delete()
    return HttpResponse("success")
