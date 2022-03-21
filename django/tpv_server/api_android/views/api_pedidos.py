# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:52:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-26T14:53:11+02:00
# @License: Apache License v2.0

from api_android.tools import send_update_ws
from tokenapi.http import JsonError, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.conf import settings
from django.db import connection
from gestion.models import Camareros, Lineaspedido, Servidos
import json


@csrf_exempt
def  get_pendientes(request):
    idz = request.POST["idz"]
    lstObj = []
    mz = "(SELECT mesaszona.IDZona, mesasabiertas.UID FROM mesasabiertas LEFT JOIN mesaszona ON mesaszona.IDMesa=mesasabiertas.IDMesa) as mz";
    m = "(SELECT mesas.Nombre AS nomMesa, mesasabiertas.UID FROM mesasabiertas LEFT JOIN mesas ON mesas.ID=mesasabiertas.IDMesa) as m";
    s = "(SELECT IDLinea FROM servidos)";
    lpedidos = ''.join(['SELECT m.nomMesa, mz.IDZona, l.ID, l.Precio, count(l.IDArt) as Can, l.Nombre, l.IDArt, IDPedido ',
               'FROM lineaspedido as l LEFT JOIN {0} ON mz.UID=l.UID LEFT JOIN {1} ON m.UID=l.UID ',
               'WHERE (Estado="P" OR Estado="R") AND mz.IDZona={2} AND l.ID NOT IN {3} ',
               'GROUP BY l.IDArt, l.Nombre, l.Precio, l.IDPedido, l.UID, m.nomMesa ',
               'ORDER BY l.ID DESC']).format(mz, m, idz, s)

    with connection.cursor() as cursor:
        cursor.execute(lpedidos)
        rows = cursor.fetchall()
        for r in rows:
            lstObj.append({
               'nomMesa': r[0],
               'IDZona': r[1],
               'ID': r[2],
               'Precio': r[3],
               'Can': r[4],
               'Nombre': r[5],
               'IDArt': r[6],
               'IDPedido': r[7]
            })

    return JsonResponse(lstObj)



@csrf_exempt
def  buscar(request):
    str = request.POST["str"]
    lstObj = []
    mz = "(SELECT mesaszona.IDZona, mesasabiertas.UID FROM mesasabiertas LEFT JOIN mesaszona ON mesaszona.IDMesa=mesasabiertas.IDMesa) as mz";
    m = "(SELECT mesas.Nombre AS nomMesa, mesasabiertas.UID FROM mesasabiertas LEFT JOIN mesas ON mesas.ID=mesasabiertas.IDMesa) as m";
    s = "(SELECT IDLinea FROM servidos)";
    lpedidos = ''.join(['SELECT m.nomMesa, mz.IDZona, l.ID, l.Precio, count(l.IDArt) as Can, l.Nombre,  l.IDArt, IDPedido ',
               'FROM lineaspedido as l LEFT JOIN {0} ON mz.UID=l.UID LEFT JOIN {1} ON m.UID=l.UID ',
               'WHERE (Estado="P" OR Estado="R")  AND l.Nombre LIKE "%{3}%" AND l.ID NOT IN {2} ',
               'GROUP BY l.IDArt, l.Nombre, l.Precio, l.IDPedido, l.UID, m.nomMesa ',
               'ORDER BY l.ID DESC']).format(mz, m, s, str)

    with connection.cursor() as cursor:
        cursor.execute(lpedidos)
        rows = cursor.fetchall()
        for r in rows:
            if r[0]:
                lstObj.append({
                   'nomMesa': r[0],
                   'IDZona': r[1],
                   'ID': r[2],
                   'Precio': r[3],
                   'Can': r[4],
                   'Nombre': r[5],
                   'IDArt': r[6],
                   'IDPedido': r[7]
                })


    return JsonResponse(lstObj)

@csrf_exempt
def servido(request):
    art = json.loads(request.POST["art"])
    lineas = Lineaspedido.objects.filter(idart=art["IDArt"],
                                         nombre=art["Nombre"],
                                         precio=art["Precio"],
                                         pedido_id=art["IDPedido"])

    for l in lineas:
        serv = Servidos()
        serv.linea_id = l.pk
        serv.save()

    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "pendientes",
       "receptor": "comandas",
    }
    send_update_ws(request, update)


    return get_pendientes(request)
