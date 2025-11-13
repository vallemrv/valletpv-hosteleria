# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-20T21:18:53+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-07T17:20:09+01:00
# @License: Apache License v2.0

from api.decorators.uid_activo import verificar_uid_activo
from django.db.models import Sum
from django.core.exceptions import ObjectDoesNotExist
from django.http import HttpResponse as HttpRespose
from tokenapi.http import JsonResponse, JsonError
from comunicacion.tools import send_mensaje_impresora
from api.tools.mails import send_cierre, getUsuariosMail
from gestion.models.arqueos import Arqueocaja, Cierrecaja, Efectivo, Gastos
from gestion.models.familias import Receptores
from gestion.models.ticket import Ticket, Ticketlineas
from datetime import datetime
from threading import Thread
from gestion.tools.config_logs import logger
import json

@verificar_uid_activo
def get_cambio(request):
    """Obtiene el cambio actual y verifica si hay un arqueo pendiente."""
    try:
        arqueo = Arqueocaja.objects.latest('id') if Arqueocaja.objects.exists() else 650.0
        cierre = Cierrecaja.objects.latest('id') if Cierrecaja.objects.exists() else None
        ticket_final = Ticket.objects.latest('id') if Ticket.objects.exists() else None

        hay_arqueo = False
        if ticket_final:
            if not cierre or ticket_final.pk != cierre.ticketfinal:
                hay_arqueo = True

        return JsonResponse({
            "cambio": arqueo.cambio,
            "cambio_real": arqueo.cambio_real,
            "stacke": arqueo.stacke,
            "hay_arqueo": hay_arqueo
        })
    except ObjectDoesNotExist:
        return JsonResponse({"cambio": 600.0, "hay_arqueo": True})

@verificar_uid_activo
def update_last_cambio_stacke(request):
    """Actualiza los valores de cambio y stacke del último arqueo."""
    if request.method != 'POST':
        return JsonError({"error": "Se requiere método POST."})

    try:
        cambio = float(request.POST.get('cambio', 0))
        stacke = float(request.POST.get('stacke', 0))
        cambio_real = float(request.POST.get('cambio_real', 0))

        arqueo = Arqueocaja.objects.latest('id')
        arqueo.cambio = cambio
        arqueo.stacke = stacke
        arqueo.cambio_real = cambio_real
        arqueo.save()
        return JsonResponse({"message": "Arqueo actualizado con éxito"})
    except (ObjectDoesNotExist, ValueError) as e:
        logger.error(f"Error al actualizar cambio/stacke: {str(e)}", exc_info=True)
        return JsonError({"error": "No se pudo actualizar el arqueo"})

@verificar_uid_activo
def arquear(request):
    """Realiza un arqueo de caja basado en los tickets y el último cierre."""
    
    try:
        cierre = Cierrecaja.objects.latest('id') if Cierrecaja.objects.exists() else None
        ticket_final = Ticket.objects.latest('id') if Ticket.objects.exists() else None

        if not ticket_final:
            return JsonError({"error": "No hay tickets para arquear"})

        nuevo_cierre = Cierrecaja()
        if not cierre:
            nuevo_cierre.ticketcom = Ticket.objects.earliest('id').pk
            nuevo_cierre.ticketfinal = ticket_final.pk
        elif ticket_final.pk != cierre.ticketfinal:
            nuevo_cierre.ticketcom = cierre.ticketfinal + 1
            nuevo_cierre.ticketfinal = ticket_final.pk
        else:
            # Si no hay diferencia, eliminamos el arqueo previo y usamos el cierre existente
            ultimo_arqueo = Arqueocaja.objects.latest('id')
            ultimo_arqueo.gastos_set.all().delete()
            ultimo_arqueo.efectivo_set.all().delete()
            ultimo_arqueo.delete()
            nuevo_cierre = cierre

        crear_cierre(request, nuevo_cierre)
        return JsonResponse({"message": "Arqueo realizado con éxito"})
    except Exception as e:
        logger.error(f"Error en arquear: {str(e)}", exc_info=True)
        return JsonError({"error": "Error al realizar el arqueo"})

def crear_cierre(request, cierre):
    """Crea un cierre de caja con su arqueo asociado."""
    try:
        efectivo = float(request.POST.get("efectivo", 0))
        cambio = float(request.POST.get("cambio", 0))
        gastos = float(request.POST.get("gastos", 0))
        des_efectivo = json.loads(request.POST.get("des_efectivo", "[]"))
        des_gastos = json.loads(request.POST.get("des_gastos", "[]"))
        usaCashlogy = request.POST.get("usaCashlogy", "false") == "true"

        # Configurar fecha y hora
        ahora = datetime.now()
        cierre.hora = ahora.strftime("%H:%M")
        cierre.fecha = ahora.strftime("%Y/%m/%d")
        cierre.save()

        # Calcular el total usando el ORM, ajustado a los modelos reales
        total = (Ticketlineas.objects
                 .filter(ticket__id__gte=cierre.ticketcom, ticket__id__lte=cierre.ticketfinal)
                 .filter(ticket__entrega__gt=0)
                 .aggregate(total=Sum('linea__precio'))['total'] or 0.0)

        # Convertir total a float para que coincida con los otros valores
        total = float(total)
        if usaCashlogy:
            # Crear y guardar el arqueo
            arqueo = Arqueocaja(
                cierre=cierre,
                cambio=cambio,
                descuadre=round(efectivo - total, 2),  # Redondear a 2 decimales
                stacke=float(request.POST.get("stacke", 0))
            )
            arqueo.save()
        else:
            arqueo = Arqueocaja(
                cierre=cierre,
                cambio=cambio,
                descuadre=round(((efectivo + gastos) - cambio) - total, 2),  # Redondear a 2 decimales
                stacke=float(request.POST.get("stacke", 0))
            )
            arqueo.save()

            # Guardar efectivo
            for item in des_efectivo:
                Efectivo.objects.create(
                    arqueo=arqueo,
                    can=item.get("cantidad", 0),
                    moneda=item.get("denominacion", "")
                )

            # Guardar gastos
            for item in des_gastos:
                Gastos.objects.create(
                    arqueo=arqueo,
                    descripcion=item.get("descripcion", ""),
                    importe=item.get("importe", 0)
                )

            # Procesar impresión y correo
            imprimir_desglose(arqueo)
            
        Thread(target=run_enviar_correo, args=(arqueo, usaCashlogy)).start()

    except (ValueError, json.JSONDecodeError) as e:
        logger.error(f"Error al crear cierre: {str(e)}", exc_info=True)
        raise

def run_enviar_correo(arqueo, usaCashlogy=False):
    """Envía correos con el desglose del cierre a los usuarios."""
    try:
        usuarios = getUsuariosMail()
        desglose = arqueo.get_desglose_cierre(usaCashlogy=usaCashlogy)
        for usuario in usuarios:
            send_cierre(usuario, desglose)
    except Exception as e:
        logger.error(f"Error al enviar correo: {str(e)}", exc_info=True)

def imprimir_desglose(arqueo):
    """Imprime el desglose del arqueo si no se usa Cashlogy."""
  
    try:
        desglose = arqueo.get_desglose_efectivo()
        if not desglose:
            return JsonResponse({"message": "No hay desglose para imprimir"})

        receptor = Receptores.objects.get(nombre='Ticket').nomimp
        ahora = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")

        obj_cambio = {
            "op": "desglose",
            "receptor": receptor,
            "fecha": ahora,
            "lineas": desglose['lineas_cambio']
        }
        obj_desglose = {
            "op": "desglose",
            "receptor": receptor,
            "fecha": ahora,
            "lineas": desglose['lineas_retirar']
        }

        send_mensaje_impresora(obj_desglose)
        send_mensaje_impresora(obj_cambio)
    except Exception as e:
        logger.error(f"Error al imprimir desglose: {str(e)}", exc_info=True)
       