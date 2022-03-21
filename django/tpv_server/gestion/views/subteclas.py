# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:00+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-16T01:46:16+02:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError, HttpResponse
from django.db.models import Q
from django.forms.models import model_to_dict
from django.shortcuts import render, reverse
from gestion.models import Subteclas, Teclascom, Teclas
from gestion.forms import SubteclasForm, TeclasForm, TeclasFormOrden
from django.contrib.auth.decorators import login_required


@login_required(login_url='login_tk')
def lista_subteclas(request, id):
    try:
        tecla = Teclas.objects.get(pk=id)
        objs = Subteclas.objects.filter(tecla__pk=id)        
        return render(request, "comandas/listado_subteclas.html", 
                              {"listado": objs, 'tecla': id, 
                              'nombre_tecla': tecla.nombre,
                              "precio": tecla.p1,
                               "form": SubteclasForm()})
    except(e):
        print(e)
        return render(request, "error.html")

@login_required(login_url='login_tk')
def add_subtecla_grupo(request, id, IDTecla):
    s = Subteclas.objects.filter(tecla__pk=IDTecla, tecla_child__pk=id).first()
    if not s:
        s = Subteclas(tecla_id=IDTecla, tecla_child_id=id)
        s.save()

    sub_send = []
    for s in Subteclas.objects.filter(tecla__pk=IDTecla).order_by('-tecla_child__orden'):
        sub_send.append(s.tecla_child.get_items_edit(s.orden))

    return JsonResponse(sub_send)
        

@login_required(login_url='login_tk')
def rm_subtecla_grupo(request, id, IDTecla):
    Subteclas.objects.filter(tecla__pk=IDTecla, tecla_child__pk=id).delete()
    return HttpResponse('success')

@login_required(login_url='login_tk')
def lista_subteclas_grupo(request, id):
    try:
        tecla = Teclas.objects.get(pk=id)
        objs = Subteclas.objects.filter(tecla__pk=id).order_by('-tecla_child__orden')        
        return render(request, "articulos/listado_subteclas.html", 
                              {"listado": objs, 'tecla': id, 'nombre_tecla': tecla.nombre,
                              "precio": tecla.p1,
                               "form": TeclasForm(), "form_orden": TeclasFormOrden()})
    except:
        return render(request, "error.html")


@login_required(login_url='login_tk')
def lista_subteclas_tecla(request, id):
    tecla = Teclas.objects.get(pk=id)
    objs = Subteclas.objects.filter(tecla__pk=tecla.pk)
   
    if tecla.tipo == "GR":
        return render(request, "articulos/listado_subteclas.html", 
                              {"listado": objs, 'tecla': id, 'nombre_tecla': tecla.nombre,
                              "precio": tecla.p1,
                               "form": TeclasForm(), "form_orden": TeclasFormOrden()})
    
    return render(request,  "comandas/listado_subteclas.html", {"listado": objs, 'tecla': tecla.pk,
                                                                'nombre_tecla': tecla.nombre,
                                                                "precio": tecla.p1,
                                                                "form": SubteclasForm()})

@login_required(login_url='login_tk')
def add_subtecla(request, id):
    form = SubteclasForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save(commit=False)
        obj.tecla_id = id
        obj.save()
        obj_send = { 'cols': [obj.nombre, obj.incremento+obj.tecla.p1, 
                              obj.descripcion_r if obj.descripcion_r else obj.tecla.nombre + " " +obj.nombre],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'}],
                     'obj': {"id":obj.id, "nombre": obj.nombre, "incremento": obj.incremento,
                              "descripcion_r": obj.descripcion_r}
               }
        if obj.tecla.tipo != "ML":
            obj.tecla.tipo = "ML"
            obj.tecla.save()
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_subtecla(request, id):
    obj = Subteclas.objects.get(pk=id)
    form = SubteclasForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, obj.incremento+obj.tecla.p1, 
                              obj.descripcion_r if obj.descripcion_r else obj.tecla.nombre + " " +obj.nombre],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'}],
                     'obj': {"id":obj.id, "nombre": obj.nombre, "incremento": obj.incremento,
                             "descripcion_r": obj.descripcion_r }
               }
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_subtecla(request, id):
    Subteclas.objects.filter(pk=id).delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def lista_teclas_subtecla(request):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Teclas.objects.filter(Q(nombre__icontains=filter) |
                                     Q(tag__icontains=filter)).exclude(tipo="SP")
    else:
        objs = Teclas.objects.all().exclude(tipo="SP")



    listaObj = []
    for obj in objs:
        print(obj.tipo)
        secciones =  ", ".join(obj.teclaseccion_set.all().values_list("seccion__nombre", flat=True))
        obj_send = { 'cols': [obj.nombre, obj.p1, obj.p2, secciones],
                     'botones': [{'tipo':'ver', 'icon':'fas fa-grip-vertical'}],
                     'obj': {"id": obj.id, "nombre": obj.nombre}
                   }
        listaObj.append(obj_send)

    return JsonResponse(listaObj)


@login_required(login_url='login_tk')
def subteclas(request):
    objs = Subteclas.objects.all().exclude(nombre=None)
    return render(request, "comandas/listado_subteclas.html", {"listado": objs,
                                                      "form": SubteclasForm()})
