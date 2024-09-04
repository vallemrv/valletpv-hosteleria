# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-20T21:18:53+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-07T17:20:09+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse, JsonError
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
from api_android.tools import send_mensaje_impresora
from api_android.tools.mails import send_cierre, getUsuariosMail
from gestion.models import (Arqueocaja, Cierrecaja, Efectivo, Gastos,
                            Receptores, Ticket)
from datetime import datetime
import json
import threading

@csrf_exempt
def get_cambio(request):
    
    # Obtener el último arqueo
    arqueo = Arqueocaja.objects.all().order_by('-id').first()
    
    if arqueo:
        # Obtener el último cierre y el último ticket
        cierre = Cierrecaja.objects.all().order_by('-id').first()
        ticketfinal = Ticket.objects.all().order_by('-id').first()

        hay_arqueo = False

        # Verificar si hay un ticket final y cierre
        if ticketfinal:
            if cierre:
                # Comparar el último ticket con el cierre
                if ticketfinal.pk != cierre.ticketfinal:
                    hay_arqueo = True
            else:
                # Si no hay cierre pero hay un ticket, se puede hacer un arqueo
                hay_arqueo = True

        return JsonResponse({"cambio": arqueo.cambio, 
                             "cambio_real": arqueo.cambio_real,
                             "stacke":arqueo.stacke,
                             "hay_arqueo": hay_arqueo})
    else:
        # Si no hay arqueo, devolver un valor por defecto
        return JsonResponse({"cambio": 600.0, "hay_arqueo": True})



@csrf_exempt
def update_last_cambio_stacke(request):
    if request.method == 'POST':
        # Parse 'cambio' and 'stacke' values from POST data
        cambio = float(request.POST.get('cambio'))
        stacke = float(request.POST.get('stacke'))
        cambio_real = float(request.POST.get("cambio_real"))

        # Fetch the last Arqueocaja entry
        arqueo = Arqueocaja.objects.all().order_by('-id').first()
        if not arqueo:
            return HttpResponse("Error no hay arqueos")

        # Update the 'cambio' and 'stacke' fields
        arqueo.cambio = cambio
        arqueo.stacke = stacke
        arqueo.cambio_real = cambio_real
        arqueo.save()

        return HttpResponse("success")

    else:
        return JsonError({"error": "POST method required."})
    


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
            c.ticketcom = cierre.ticketfinal + 1
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
   
    # Set the time and date for the closure
    c.hora = datetime.now().strftime("%H:%M")
    c.fecha = datetime.now().strftime("%Y/%m/%d")
    c.save()

    # SQL query to calculate the total amount
    sql_total = '''
        SELECT SUM(Precio) AS Total
        FROM ticketlineas AS tk
        INNER JOIN lineaspedido AS lp ON tk.IDLinea=lp.ID
        INNER JOIN ticket ON ticket.ID=tk.IDTicket
        WHERE ticket.ID >= {0} AND ticket.ID <= {1} AND ticket.Entrega > 0
    '''.format(c.ticketcom, c.ticketfinal)

    total = 0
    with connection.cursor() as cursor:
        cursor.execute(sql_total)
        row = cursor.fetchone()
        total = float(row[0]) if row[0] is not None else 0.00

    # Create and save the arqueo object
    arqueo = Arqueocaja()
    arqueo.cierre_id = c.pk
    arqueo.cambio = cambio
    arqueo.descuadre = ((efectivo + gastos) - cambio) - total

    # Save the stacke value if it exists in the request
    if "stacke" in request.POST:
        arqueo.stacke = float(request.POST["stacke"])
    else:
        arqueo.stacke = 0

    arqueo.save()

    # Save the effective amounts
    for f in ef:
        lef = Efectivo()
        lef.arqueo_id = arqueo.pk
        lef.can = f["Can"]
        lef.moneda = f["Moneda"]
        lef.save()

    # Save the expenses
    for g in gas:
        lg = Gastos()
        lg.arqueo_id = arqueo.pk
        lg.descripcion = g["Des"]
        lg.importe = g["Importe"]
        lg.save()

    # Print the breakdown
    imprimir_desglose(request, arqueo)

    # Start a thread to send the email
    threading.Thread(target=run_enviar_correro, args={arqueo,}).start()

   
def run_enviar_correro(arqueo):
    users = getUsuariosMail()
    for us in users:
        send_cierre(us, arqueo.get_desglose_cierre())



def imprimir_desglose(request, arqueo):
    # Verificar si 'usaCashlogy' está en la request POST y es True
    if request.POST.get('usaCashlogy') == 'true':  # 'true' es una cadena, no un booleano
       return JsonResponse({"message": "No se realiza la impresión porque usaCashlogy es True."})

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

    return JsonResponse({"message": "Impresión completada."})
