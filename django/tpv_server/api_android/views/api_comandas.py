# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:17:04+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T17:44:16+02:00
# @License: Apache License v2.0

from __future__ import barry_as_FLUFL
from os import sync

from django.forms import model_to_dict
from api_android.tools import (send_update_ws, imprimir_pedido,
                               get_descripcion_pedido)
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
from django.db.models import  Count
from gestion.models import (Mesasabiertas, SeccionesCom, Subteclas, Sync,
                            Teclas, Infmesa, Pedidos, Lineaspedido, Camareros)

from datetime import datetime
from uuid import uuid4
import json


@csrf_exempt
def marcar_rojo(request):
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.get(mesa_id=idm)
    infmesa = mesa_abierta.infmesa
    infmesa.numcopias = infmesa.numcopias + 1
    infmesa.save()
    Sync.actualizar("mesasabiertas")
    if infmesa.numcopias <= 1:
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(request, update)
    return JsonResponse({})

@csrf_exempt
def ls(request):
    if "sec" in request.POST:
        articulos = Teclas.objects.filter(teclascom__seccion_id=request.POST["sec"])


    if 'str' in request.POST:
        articulos = Teclas.objects.filter(tag__icontains=request.POST["str"])
        articulos = articulos.order_by('-tag')

    lstArt = []
    for art in articulos:
        seccion = art.teclaseccion_set.all().first()
        if seccion:
            seccion = seccion.seccion

        obj = {
          'ID': art.id,
          'Nombre': art.nombre,
          'P1': art.p1,
          'P2': art.p2,
          'Orden': art.orden,
          'IDFamilia': art.familia.pk,
          'Tag': art.tag,
          'TTF': art.ttf,
          'Precio': art.p1,
          'RGB': seccion.rgb if seccion else "255,255,0",
          'Color': seccion.color if seccion else "gray",
          'IDSeccion': seccion.id if seccion else -1,
          'OrdCom': art.teclascom_set().first().orden,
          'Nombre_sec': art.teclascom_set().first().nombre
        }
        lstArt.append(obj)

    return JsonResponse(lstArt)

@csrf_exempt
def descargarteclados(request):
    secciones = SeccionesCom.objects.all()
    teclados = []
    for s in secciones:
        teclados[s.nombre] = []
        for art in s.teclascom_set.all():
            teclacom = art
            art = art.tecla
            seccion = art.teclaseccion_set.all().first()
            if seccion:
                seccion = seccion.seccion


            a = {
                'ID': art.id,
                'Nombre': art.nombre,
                'P1': art.p1,
                'P2': art.p2,
                'Orden': art.orden,
                'IDFamilia': art.familia.pk,
                'Tag': art.tag,
                'TTF': art.ttf,
                'Precio': art.p1,
                'RGB': seccion.rgb if seccion else "255,255,0",
                'Color': seccion.color if seccion else "gray",
                'IDSeccion': seccion.id if seccion else -1,
                'OrdCom': teclacom.orden,
                'Nombre_sec': s.nombre,
                'Sub': []

            }
            teclados[s.nombre].append(a)
            for sub in Subteclas.objects.filter(tecla__pk=a['ID']):
                obj = {
                    "ID": sub.id,
                    "Nombre": sub.nombre,
                    "IDTecla": sub.tecla_id,
                    "Incremento": sub.incremento
                }
                a["Sub"].append(sub)


        return JsonResponse(teclados)


@csrf_exempt
def lssubteclas(request):
    if 'IDTecla' in request.POST:
        sub = Subteclas.objects.filter(tecla_id=request.POST["IDTecla"])
    else:
        sub = Subteclas.objects.all()

    lstObj = []
    for s in sub:
        obj = {
            "ID": s.id,
            "Nombre": s.nombre,
            "IDTecla": s.tecla_id,
            "Incremento": s.incremento
        }
        lstObj.append(obj)
    return JsonResponse(lstObj)

@csrf_exempt
def lssecciones(request):
    return JsonResponse(SeccionesCom.get_all_for_devices())

@csrf_exempt
def lspedidos(request):
    idm = request.POST["idm"];
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    lstObj = []
    if mesa:
        uid = mesa.infmesa.uid
        sql_pedido = ''.join(
             ["SELECT l.ID, count(l.IDArt) as Can, l.Nombre, l.IDArt, IDPedido ",
              "FROM lineaspedido as l ",
              "WHERE (Estado='P' OR Estado='R')  AND UID='{0}' ",
              "GROUP BY l.IDArt, l.Nombre, l.Precio, l.IDPedido ",
              "ORDER BY l.ID DESC"]
        ).format(uid)

        with connection.cursor() as cursor:
            cursor.execute(sql_pedido)
            rows = cursor.fetchall()
            for r in rows:
                lstObj.append({
                   'ID': r[0],
                   'Can': r[1],
                   'Nombre': r[2],
                   'IDArt': r[3],
                   'IDPedido': r[4],
             })

    return JsonResponse(lstObj)

@csrf_exempt
def lsall(request):
    secciones = SeccionesCom.objects.all()
    lstArt = []
    for s in secciones:
        for art in s.teclascom_set.all():
            teclacom = art
            art = art.tecla
            seccion = art.teclaseccion_set.all().first()
            if seccion:
                seccion = seccion.seccion


            a = {
                'ID': art.id,
                'Nombre': art.nombre,
                'P1': art.p1,
                'P2': art.p2,
                'Orden': art.orden,
                'IDFamilia': art.familia.pk,
                'Tag': art.tag,
                'TTF': art.ttf,
                'Precio': art.p1,
                'RGB': art.get_color(),
                'Color': seccion.color if seccion else "gray",
                'IDSeccion': seccion.id if seccion else -1,
                'OrdCom': teclacom.orden,
                'Nombre_sec': s.nombre

            }
            lstArt.append(a)
    return JsonResponse(lstArt)

@csrf_exempt
def pedir(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    lineas = json.loads(request.POST["pedido"])
    is_updatable, pedido = Pedidos.agregar_nuevas_lineas(idm,idc,lineas)
    
    if is_updatable:
        #enviar notficacion de update
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(request, update)


    
    imprimir_pedido(request, pedido.id)
    return HttpResponse("success")

@csrf_exempt
def get_ultimas(request):
    args = json.loads(request.POST['args'])
    c = args['o']
    lista = args['r']
    pedidos = Pedidos.objects.all().order_by('-id')[c:c+5]
    send = []
    for p in pedidos:
        camareo = Camareros.objects.get(pk=p.camarero_id)
        mesa = p.infmesa.mesasabiertas_set.first()
        if mesa:
            mesa = mesa.mesa
            lineas = p.lineaspedido_set.values("idart",
                                               "nombre",
                                               "precio",
                                               "pedido_id").annotate(can=Count('idart'))
            receptores = {}
            for l in lineas:
                receptor = Teclas.objects.get(id=l['idart']).familia.receptor
                if receptor.nomimp in lista:
                    if  receptor.nombre not in receptores:
                        receptores[receptor.nombre] = {
                            "op": "pedido",
                            "hora": p.hora,
                            "receptor": receptor.nomimp,
                            "receptor_activo": receptor.activo,
                            "camarero": camareo.nombre + " " + camareo.apellidos,
                            "mesa": mesa.nombre,
                            "lineas": []
                        }
                    l["precio"] = float(l["precio"])
                    receptores[receptor.nombre]['lineas'].append(l)

            send.append(receptores)


    return JsonResponse(send)
