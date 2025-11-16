from django.db import models
from django.db.models import Q
from .mesasabiertas import  Mesasabiertas
from .arqueos import Cierrecaja
from .basemodels import BaseModels
from comunicacion.tools import comunicar_cambios_devices   
from django.db import transaction
from gestion.decorators.log_excepciones import log_excepciones
from .pedidos import Lineaspedido
from datetime import datetime
        
class Ticket(BaseModels):
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    entrega = models.DecimalField(db_column='Entrega', max_digits=6, decimal_places=2)  # Field name made lowercase.
    uid = models.CharField(db_column='UID', max_length=100)  # Field name made lowercase.
    mesa = models.CharField(db_column='Mesa', max_length=40)  # Field name made lowercase.
    url_factura = models.CharField(max_length=140, default="")  # Field name made lowercase.
    recibo_tarjeta = models.TextField(default="")


    @staticmethod
    @transaction.atomic # El decorador ya maneja la transacción, no necesitamos "with transaction.atomic()" dentro.
    @log_excepciones
    def cerrar_cuenta(idm, idc, entrega,  idsCobrados, recibo=""):
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).select_related('infmesa').first()
        if not mesa:
            # Si la mesa no existe, no hacemos nada más.
            return (0, -1)

        uid = mesa.infmesa.pk
        now = datetime.now()

         # --- MEJORA 1: OBTENER TODOS LOS OBJETOS EN UNA SOLA CONSULTA ---
        lineas_a_procesar = Lineaspedido.objects.filter(Q(id__in=idsCobrados) | Q(id_local__in=idsCobrados), infmesa__pk=uid)
       
        if not lineas_a_procesar:
            return (0, -1)

        # Crear el ticket principal
        ticket = Ticket.objects.create(
            hora=now.strftime("%H:%M"),
            fecha=now.strftime("%Y/%m/%d"),
            camarero_id=idc,
            uid=uid,
            entrega=entrega,
            mesa=mesa.mesa.nombre,
            recibo_tarjeta=recibo
        )

        total = 0
        lineas_ticket_a_crear = []
        
        # --- MEJORA 2: PREPARAR OBJETOS PARA OPERACIONES BULK ---
        lineas_a_borrar = []
        for linea in lineas_a_procesar:
            total += linea.precio
            # Preparamos la nueva línea de ticket para guardarla después
            lineas_ticket_a_crear.append(
                Ticketlineas(ticket=ticket, linea_id=linea.pk)
            )
            lineas_a_borrar.append({'ID': linea.pk})
            # Marcamos la línea de pedido original como pagada
            linea.estado = 'C'

        # Notificar a devices y smart receptors
        comunicar_cambios_devices("rm", "lineaspedido", lineas_a_borrar)
        
        # Import aquí para evitar circular import
        from api.tools.smart_receptor import notificar_lineas_borradas
        notificar_lineas_borradas(lineas_a_procesar)

         # --- MEJORA 3: OPERACIONES BULK PARA MEJORAR RENDIMIENTO ---

        # Creamos todas las Ticketlineas en una sola operación de base de datos
        Ticketlineas.objects.bulk_create(lineas_ticket_a_crear)
        
        # Actualizamos todas las Lineaspedido en una sola operación de base de datos
        Lineaspedido.objects.bulk_update(lineas_a_procesar, ['estado'])

        # Comprobar si la mesa se puede cerrar
        if not Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).exists():
            # Antes de eliminar la mesa, enviar al cliente que borre
            # todas las líneas que queden asociadas a esta infmesa,
            # independientemente de su estado (para mantener sincronía).
            remaining_lineas = Lineaspedido.objects.filter(infmesa__pk=uid)
            if remaining_lineas.exists():
                rm_list = [{'ID': int(i)} for i in remaining_lineas.values_list('id', flat=True)]
                comunicar_cambios_devices("rm", "lineaspedido", rm_list)
                
                # Import aquí para evitar circular import
                from api.tools.smart_receptor import notificar_lineas_borradas
                notificar_lineas_borradas(remaining_lineas)

            mesa.delete()

        return (total, ticket.id)

    @staticmethod
    def get_last_id_linea():
        cierre = Cierrecaja.objects.first()
        last_ticket = 0
        if (cierre):
            last_ticket = cierre.ticketfinal


        t = Ticketlineas.objects.filter(ticket__id__lte=last_ticket).order_by("-linea_id").first()
        last_id = 0
        if  (t):
            last_id = t.linea.id 

        return last_id  


    class Meta:
        db_table = 'ticket'

class Ticketlineas(BaseModels):
    ticket = models.ForeignKey('Ticket',  on_delete=models.CASCADE, db_column='IDTicket')  # Field name made lowercase.
    linea = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE, db_column='IDLinea')  # Field name made lowercase.


    class Meta:
        db_table = 'ticketlineas'

