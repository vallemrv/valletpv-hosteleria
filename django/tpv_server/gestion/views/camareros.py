# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:10:00+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-04T18:41:35+01:00
# @License: Apache License v2.0

from api_android.tools import send_update_ws
from tokenapi.http import JsonResponse, HttpResponse
from django.db.models import Q
from django.shortcuts import render
from gestion.models import Camareros, Sync
from gestion.forms import CamarerosForm
from django.contrib.auth.decorators import login_required


@login_required(login_url='login_tk')
def lista_camareros(request):
    if "filter" in request.POST:
        filter= request.POST["filter"]
        objs = Camareros.objects.filter(Q(nombre__icontains=filter) |
                                        Q(apellidos__icontains=filter)).exclude(activo=False)
        listaCam = []
        for obj in objs:
            obj_send = { 'cols': [obj.nombre, obj.apellidos],
                         'botones': [{'tipo':'activar',
                                      'icon':'fa-user-check' if obj.autorizado == 1 else 'fa-user'},
                                  {'tipo':'edit', 'icon':'fa-edit'},
                                  {'tipo':'borrar', 'icon':'fa-trash'},
                                  {'tipo':'pass',
                                  'icon':'fa-unlock-alt' if obj.pass_field == "" else 'fa-lock'}],
                         'obj': {"id":obj.id, "nombre": obj.nombre, "apellidos": obj.apellidos }
                   }
            listaCam.append(obj_send)

        return JsonResponse(listaCam)
    else:
        objs = Camareros.objects.filter(activo=True)
    return render(request, "camareros/listado.html", {"listado": objs,
                                                      "form": CamarerosForm()})

@login_required(login_url='login_tk')
def add_camarero(request):
    form = CamarerosForm(request.POST)
    obj_send = {}
    if form.is_valid():
        obj = form.save()

        obj_send = { 'cols': [obj.nombre, obj.apellidos],
                     'botones': [{'tipo':'activar',
                                  'icon':'fa-user-check' if obj.autorizado == 1 else 'fa-user'},
                                  {'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'pass', 'icon':'fa-unlock-alt'}],
                     'obj': {"id":obj.id, "nombre": obj.nombre, "apellidos": obj.apellidos }
               }
    else:
        print("no valido")

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def edit_camarero(request, id):
    obj = Camareros.objects.get(pk=id)
    form = CamarerosForm(request.POST, instance=obj)
    obj_send = {}
    if form.is_valid():
        obj = form.save()
        obj_send = { 'cols': [obj.nombre, obj.apellidos],
                     'botones': [{'tipo':'activar',
                                  'icon':'fa-user-check' if obj.autorizado == 1 else 'fa-user'},
                                  {'tipo':'edit', 'icon':'fa-edit'},
                              {'tipo':'borrar', 'icon':'fa-trash'},
                              {'tipo':'pass',
                              'icon':'fa-unlock-alt' if obj.pass_field == "" else 'fa-lock'}],
                     'obj': {"id":obj.id, "nombre": obj.nombre, "apellidos": obj.apellidos }
               }

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def rm_camarero(request, id):
    Camareros.objects.filter(pk=id).update(activo=0)
    Sync.actualizar("camareros")
    return HttpResponse("success")

@login_required(login_url='login_tk')
def rm_pass_camarero(request, id):
    obj = Camareros.objects.get(pk=id)
    obj.pass_field = ''
    obj.autorizado = 0
    obj.save()
    obj_send = { 'cols': [obj.nombre, obj.apellidos],
                 'botones': [{'tipo':'activar',
                              'icon':'fa-user'},
                              {'tipo':'edit', 'icon':'fa-edit'},
                          {'tipo':'borrar', 'icon':'fa-trash'},
                          {'tipo':'pass', 'icon':'fa-unlock-alt'}],
                 'obj': {"id":obj.id, "nombre": obj.nombre, "apellidos": obj.apellidos }
                }

    #enviar notficacion de update
    update = {
       "OP": "CAMBIO_TURNO",
       "receptor": "comandas",
    }
    send_update_ws(request, update)

    return JsonResponse(obj_send)

@login_required(login_url='login_tk')
def autorizar_cam(request, id):
    obj = Camareros.objects.get(pk=id)
    obj.autorizado = 0 if obj.autorizado == 1 else 1
    obj.save()
    obj_send = { 'cols': [obj.nombre, obj.apellidos],
                 'botones': [{'tipo':'activar',
                              'icon':'fa-user-check' if obj.autorizado == 1 else 'fa-user'},
                          {'tipo':'edit', 'icon':'fa-edit'},
                          {'tipo':'borrar', 'icon':'fa-trash'},
                          {'tipo':'pass', 'icon':'fa-unlock-alt'}],
                 'obj': {"id":obj.id, "nombre": obj.nombre, "apellidos": obj.apellidos }
                }

    #enviar notficacion de update
    update = {
       "OP": "CAMBIO_TURNO",
       "receptor": "comandas",
    }
    send_update_ws(request, update)

    return JsonResponse(obj_send)
