# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:00+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-07T08:28:05+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError, HttpResponse
from django.db.models import Q
from django.forms.models import model_to_dict
from django.shortcuts import render, reverse
from gestion.models import Sugerencias, Teclascom, Teclas
from gestion.forms import SugerenciasForm

from django.contrib.auth.decorators import login_required


@login_required(login_url='login_tk')
def lista_sugerencias(request, id):
    tecla = Teclas.objects.get(pk=id)
    objs = Sugerencias.objects.filter(tecla__pk=tecla.pk)
    return render(request, "comandas/listado_sugerencias.html", {"listado": objs, 'tecla': tecla.pk, 
                                                                'nombre_tecla': tecla.nombre,
                                                                "form": SugerenciasForm()})

@login_required(login_url='login_tk')
def lista_sugerencia_tecla(request, id):
    tecla = Teclas.objects.get(pk=id)
    objs = Sugerencias.objects.filter(tecla__pk=tecla.pk)
    return render(request, "comandas/listado_sugerencias.html", {"listado": objs, 'tecla': tecla.pk,
                                                                'nombre_tecla': tecla.nombre,
                                                                "form": SugerenciasForm()})
@login_required(login_url='login_tk')
def sugerencias(request):
    objs = Sugerencias.objects.all()
    return render(request, "comandas/listado_sugerencias.html", {"listado": objs,
                                                                 "form": SugerenciasForm()})

@login_required(login_url='login_tk')
def add_sugerencia(request, id):
    form = SugerenciasForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save(commit=False)
        obj.tecla_id = id
        obj.save()
        obj_send = { 'cols': [obj.sugerencia],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'}],
                     'obj': {"id":obj.id, "sugerencia": obj.sugerencia }
               }
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_sugerencia(request, id):
    obj = Sugerencias.objects.get(pk=id)
    form = SugerenciasForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.sugerencia],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'}],
                     'obj': {"id":obj.id, "sugerencia": obj.sugerencia }
               }
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_sugerencia(request, id):
    Sugerencias.objects.filter(pk=id).delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def lista_teclas_sugerencia(request):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Teclas.objects.filter(Q(nombre__icontains=filter) |
                                     Q(tag__icontains=filter))

    else:
        objs = Teclas.objects.all()

    listaObj = []
    for obj in objs:
        secciones = obj.teclaseccion_set.all().values_list("seccion__nombre", flat=True)
        result= ", ".join(secciones)
        obj_send = { 'cols': [obj.nombre, obj.p1, obj.p2, result],
                     'botones': [{'tipo':'sugerencias', 'icon':'fas fa-comment'}],
                     'obj': {"id": obj.id, "nombre": obj.nombre, "orden":obj.orden, 
                             "color": obj.get_color(hex=True)}
                   }
        listaObj.append(obj_send)

    return JsonResponse(listaObj)
