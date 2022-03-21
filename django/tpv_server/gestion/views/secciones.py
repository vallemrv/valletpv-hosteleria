# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:00+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-16T14:03:56+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, HttpResponse, JsonError
from django.db.models import Q
from django.forms.models import model_to_dict
from django.shortcuts import render, reverse
from gestion.models import Secciones, Teclaseccion, Teclas
from gestion.forms import SeccionesForm, TeclasForm, TeclaSeccionForm
from app.utility import rgbToHex
from django.contrib.auth.decorators import login_required


@login_required(login_url='login_tk')
def lista_secciones(request):
    objs = Secciones.objects.all()
    return render(request, "articulos/listado_secciones.html", {"listado": objs,
                                                                "form": SeccionesForm()})

@login_required(login_url='login_tk')
def add_seccion(request):
    form = SeccionesForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, rgbToHex(obj.rgb), obj.orden],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'ver', 'icon':'fa-search'}],
                      'obj': {"id": obj.id, "nombre": obj.nombre, "orden": obj.orden,
                              "rgb": rgbToHex(obj.rgb)}
                   }

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_seccion(request, id):
    obj = Secciones.objects.get(pk=id)
    form = SeccionesForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, rgbToHex(obj.rgb), obj.orden],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'ver', 'icon':'fa-search'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "orden": obj.orden,
                              "rgb": rgbToHex(obj.rgb), "color": obj.color}
                   }

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_seccion(request, id):
    Secciones.objects.filter(pk=id).delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def add_teclaseccion(request, id, idsecc):
    
    ts = Teclaseccion.objects.filter(tecla__pk=id, seccion__pk=idsecc).first()
    if not ts:
       ts =  Teclaseccion(tecla_id=id, seccion_id=idsecc)
       ts.save()
    objs = []

    for ts in Teclaseccion.objects.filter(seccion__pk=idsecc).order_by('-tecla__orden'):
        objs.append(ts.tecla.get_items_edit())

    return JsonResponse(objs)

@login_required(login_url='login_tk')
def rm_tecla_seccion(request, id, idsecc):
    ts = Teclaseccion.objects.filter(tecla__pk=id, seccion__pk=idsecc)
    print(ts)
    ts.delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def lista_teclas_seccion(request, id=-1):
    nombre = ""
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Teclas.objects.filter(Q(nombre__icontains=filter) |
                                     Q(tag__icontains=filter))
        listaObj = []
        for obj in objs:
            listaObj.append(obj.get_items_add(show_secciones=True))

        return JsonResponse(listaObj)
    elif id > -1:
        objs = []
        nombre = Secciones.objects.get(pk=id).nombre
        for ts in Teclaseccion.objects.filter(seccion__pk=id).order_by('-tecla__orden'):
            objs.append(ts.tecla)
        
    else:
        objs = Teclas.objects.all()

    return render(request, "articulos/listado_teclas_seccion.html",
                           {"listado": objs, "form": TeclasForm(), 'nombre_seccion': nombre, "seccion":id})

