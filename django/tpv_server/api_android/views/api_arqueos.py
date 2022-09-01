# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-20T21:18:53+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-07T17:20:09+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
from api_android.tools import send_mensaje_impresora
from api_android.tools.mails import send_cierre, getUsuariosMail
from gestion.models import (Arqueocaja, Cierrecaja, Efectivo, Gastos,
                            Receptores, Ticket)
from datetime import datetime
from uuid import uuid4
import json
import threading

@csrf_exempt
def get_cambio(request):
    arqueo = Arqueocaja.objects.all().order_by('-id').first()
    if arqueo:
        cierre = Cierrecaja.objects.all().order_by('-id').first()
        ticketfinal = Ticket.objects.all().order_by('-id').first()

        if ticketfinal and cierre and ticketfinal.pk != cierre.ticketfinal:
            hay_arqueo = True
        elif not cierre and ticketfinal:
            hay_arqueo = True
        else:
            hay_arqueo = False

        return JsonResponse({"cambio":arqueo.cambio, "hay_arqueo": hay_arqueo})
    else:
        return JsonResponse({"cambio":600.0, "hay_arqueo": True})



@csrf_exempt
def arquear(request):
    cierre = Cierrecaja.objects.all().order_by('-id').first()
    ticketfinal = Ticket.objects.all().order_by('-id').first()
    c = Cierrecaja()
    if not cierre and ticketfinal:
        c.ticketcom = Ticket.objects.all().order_by('id').first().pk
        c.ticketfinal = ticketfinal.pk
        crear_cierre(request, c)
    elif ticketfinal and cierre:
        if ticketfinal.pk != cierre.ticketfinal:
            c.ticketcom = cierre.ticketfinal + 1;
            c.ticketfinal = ticketfinal.pk
        else:
            arqueo = Arqueocaja.objects.all().order_by("-id").first()
            if arqueo:
                arqueo.gastos_set.all().delete()
                arqueo.efectivo_set.all().delete()
                arqueo.delete()
                c = cierre
            
        crear_cierre(request, c)
    else:
        return HttpResponse("error")
    return HttpResponse("success")


def crear_cierre(request, c):
    efectivo = float(request.POST["efectivo"])
    cambio = float(request.POST["cambio"])
    gastos = float(request.POST["gastos"])
    ef = json.loads(request.POST["des_efectivo"])
    gas = json.loads(request.POST["des_gastos"])

    c.hora = datetime.now().strftime("%H:%M")
    c.fecha = datetime.now().strftime("%Y/%m/%d")
    c.save()

    sql_total = ''.join(['SELECT SUM(Precio) AS Total ',
                         'FROM ticketlineas AS tk ',
                         'INNER JOIN lineaspedido AS lp ON tk.IDLinea=lp.ID ',
                         'INNER JOIN ticket ON ticket.ID=tk.IDTicket ',
                         "WHERE ticket.ID >= {0} AND ticket.ID <= {1} AND ticket.Entrega > 0 ",
                         ]).format(c.ticketcom, c.ticketfinal)
    total = 0
    with connection.cursor() as cursor:
        cursor.execute(sql_total)
        row = cursor.fetchone()
        total = row[0] if row[0] != None else 0.00
        total = float(total)

    arqueo = Arqueocaja()
    arqueo.cierre_id = c.pk
    arqueo.cambio = cambio
    arqueo.descuadre = ((efectivo+gastos)-cambio)-total
    arqueo.save()
    for f in ef:
        lef = Efectivo()
        lef.arqueo_id = arqueo.pk
        lef.can = f["Can"]
        lef.moneda = f["Moneda"]
        lef.save()

    for g in gas:
        lg = Gastos()
        lg.arqueo_id = arqueo.pk
        lg.descripcion = g["Des"]
        lg.importe = g["Importe"]
        lg.save()

    imprimir_desglose(request, arqueo)
    threading.Thread(target=run_enviar_correro, args={arqueo,}).start()

def run_enviar_correro(arqueo):
    users = getUsuariosMail()
    for us in users:
        send_cierre(us, arqueo.get_desglose_cierre())



def imprimir_desglose(request, arqueo):
    obj = arqueo.get_desglose_efectivo()
    if obj:
        obj_cambio = {
            "op": "desglose",
            "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
            "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
            "lineas": obj['lineas_cambio']
            }

        obj_desglose = {
            "op": "desglose",
            "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
            "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
            "lineas": obj['lineas_retirar']
            }
        send_mensaje_impresora(obj_desglose)
        send_mensaje_impresora(obj_cambio)
