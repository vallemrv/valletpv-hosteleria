# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:00+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-29T02:11:40+02:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError, HttpResponse
from django.db.models import Q
from django.forms.models import model_to_dict
from django.shortcuts import render, reverse
from gestion.models import SeccionesCom, Sync, Teclascom, Teclas
from gestion.forms import SeccionesComForm,  TeclascomForm, TeclasForm
from app.utility import rgbToHex
from django.contrib.auth.decorators import login_required


@login_required(login_url='login_tk')
def listado_secciones_comanda(request):
    objs = SeccionesCom.objects.all()
    return render(request, "comandas/listado_secciones.html", {"listado": objs,
                                                               "form": SeccionesComForm()})

@login_required(login_url='login_tk')
def rm_tecla_com(request, id, idsec):
    Teclascom.objects.filter(tecla__pk=id, seccion__pk=idsec).delete()
    return HttpResponse("success")

@login_required(login_url='login_tk')
def edit_seccion_com(request, id):
    obj = SeccionesCom.objects.get(pk=id)
    form = SeccionesComForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, obj.es_promocion, obj.descuento, obj.icono],
                     'botones': [{'tipo':'edit', 'icon':'fa-edit'},
                                 {'tipo':'ver', 'icon':'fa-search'}],
                      'obj': {"id":obj.id, "nombre": obj.nombre, "es_promocion": obj.es_promocion,
                              "descuento": obj.descuento, "icono": obj.icono}
                   }
    else:
        return JsonError(form.errors)
       
    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def crear_tecla_add_teclascom(request, id):
    form = TeclasForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        tc = Teclascom(seccion_id=id, tecla_id=obj.pk, orden=0)
        tc.save()
        obj_send = obj.get_items_edit()
    else:
        return JsonError(form.errors)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def add_teclascom(request, id, idsec):
    tc = Teclascom(seccion_id=idsec, tecla_id=id, orden=0)
    tc.save()
    listaObj = []
    for tc in Teclascom.objects.filter(seccion__pk=idsec):
        obj = tc.tecla
        listaObj.append(obj.get_items_edit(tc.orden))

    return JsonResponse(listaObj)

@login_required(login_url='login_tk')
def edit_teclascom_orden(request, id, idsec):
    obj = Teclascom.objects.filter(tecla__pk=id, seccion__pk=idsec).first()
    form = TeclascomForm(request.POST, instance=obj)
    listaObj = []
    if form.is_valid():
        form.save()
        for tc in Teclascom.objects.filter(seccion__pk=idsec):
            obj = tc.tecla
            listaObj.append(obj.get_items_edit(tc.orden))
    else:
        return JsonError(form.errors)
        
    return JsonResponse(listaObj)


@login_required(login_url='login_tk')
def lista_teclas_seccion_com(request, id=-1):
    if "filter" in request.POST:
        filter = request.POST["filter"]
        objs = Teclas.objects.filter(Q(nombre__contains=filter) |
                                     Q(tag__contains=filter))
        listaObj = []
        for obj in objs:
            listaObj.append(obj.get_items_add())

        return JsonResponse(listaObj)
    elif id > -1:
        objs = []
        for ts in Teclascom.objects.filter(seccion__pk=id):
            objs.append(ts)

    else:
        objs = Teclascom.objects.all()

    return render(request, "comandas/listado_teclas.html",
                           {"listado": objs, "form": TeclascomForm(),
                           'form_teclas': TeclasForm(), 'seccion': id})
