# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-05T23:03:14+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError, HttpResponse
from django.db.models import Q
from django.forms.models import model_to_dict
from django.shortcuts import render, reverse
from gestion.models import Familias, Teclas
from gestion.forms import FamiliasForm, TeclasForm, TeclaSeccionForm, TeclasFormOrden
from django.contrib.auth.decorators import login_required

@login_required(login_url='login_tk')
def lista_familias(request):
    objs = Familias.objects.all()
    return render(request, "articulos/listado_familias.html", {"listado": objs,
                                                               "form": FamiliasForm()})

@login_required(login_url='login_tk')
def articulos(request):
    objs = Teclas.objects.all()
    return render(request, "articulos/listado_teclas.html",
                           {"listado": objs, "form": TeclasForm(),
                           'form_secundary': TeclaSeccionForm(), 'familia': 0})


@login_required(login_url='login_tk')
def add_familia(request):
    form = FamiliasForm(request.POST)
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, obj.tipo, str(obj.receptor)],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                                 {'tipo':'borrar', 'icon':'fa-trash'},
                                 {'tipo':'pass', 'icon':'fa-search'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "tipo": obj.tipo,
                          "numtapas": obj.numtapas, "receptor": obj.receptor.pk }
                   }

    return JsonResponse(obj_send)


@login_required(login_url='login_tk')
def edit_teclas_orden(request, id):
    obj = Teclas.objects.get(pk=id)
    form = TeclasFormOrden(request.POST, instance=obj)
    if form.is_valid():
        form.save()
    else:
        return JsonError(form.errors)
   
    obj_send = []
    for t in Teclas.objects.filter(familia__pk=obj.familia.id):
        obj_send.append(t.get_items_edit())
    
    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_familia(request, id):
    obj = Familias.objects.get(pk=id)
    form = FamiliasForm(request.POST, instance=obj)
    obj_send= {}
    if form.is_valid():
        obj = form.save()

        obj_send = { 'cols': [obj.nombre, obj.tipo, str(obj.receptor)],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'pass', 'icon':'fa-search'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "tipo": obj.tipo,
                          "numtapas": obj.numtapas, "receptor": obj.receptor.pk }
                   }
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_familia(request, id):
    Familias.objects.filter(pk=id).delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def lista_teclas(request, id=-1):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Teclas.objects.filter(Q(nombre__icontains=filter) |
                                     Q(tag__icontains=filter))
        listaObj = []
        for obj in objs:
            listaObj.append(obj.get_items_edit(obj.orden))

        return JsonResponse(listaObj)
    elif id > -1:
        objs = Teclas.objects.filter(familia__pk=id)
    else:
        objs = Teclas.objects.all()

    return render(request, "articulos/listado_teclas.html",
                           {"listado": objs, "form": TeclasForm(),
                           'form_secundary': TeclasFormOrden(), 'familia':id})


@login_required(login_url='login_tk')
def lista_teclas_add(request):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Teclas.objects.filter(Q(nombre__icontains=filter) |
                                     Q(tag__icontains=filter))
    
    else:
        objs = Teclas.objects.all()


    listaObj = []
    for obj in objs:
        listaObj.append(obj.get_items_add())

    return JsonResponse(listaObj)
     


@login_required(login_url='login_tk')
def add_articulo(request):
    form = TeclasForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = obj.get_items_edit()
    else:
        return JsonError(form.errors)
       
    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_articulo(request, id):
    obj = Teclas.objects.get(pk=id)
    form = TeclasForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = obj.get_items_edit()
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_tecla(request, id):
    Teclas.objects.filter(pk=id).delete()
    return HttpResponse("success")
