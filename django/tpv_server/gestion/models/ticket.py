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
    # --- Campos existentes (Legacy/Compatibilidad) ---
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Se mantiene por compatibilidad
    camarero_id = models.IntegerField(db_column='IDCam')
    hora = models.CharField(db_column='Hora', max_length=5)     # Se mantiene por compatibilidad
    entrega = models.DecimalField(db_column='Entrega', max_digits=6, decimal_places=2)
    uid = models.CharField(db_column='UID', max_length=100)
    mesa = models.CharField(db_column='Mesa', max_length=40)
    url_factura = models.CharField(max_length=140, default="")
    recibo_tarjeta = models.TextField(default="")

    # --- Campos VeriFactu (Nuevos) ---
    
    # Encadenamiento y Seguridad
    prev_hash = models.CharField(
        max_length=64, 
        default="", 
        blank=True,
        help_text="Hash del ticket encadenado anterior (SHA-256)"
    )
    signature = models.CharField(
        max_length=64, 
        default="", 
        blank=True,
        help_text="Huella o Hash del registro actual"
    )

    # Trazabilidad Fiscal
    fecha_expedicion = models.DateTimeField(
        auto_now_add=True,
        null=True,
        blank=True,
        help_text="Fecha y hora exacta de expedición (Requisito VeriFactu)"
    )
    numero_serie = models.CharField(
        max_length=20, 
        default="", 
        blank=True,
        help_text="Serie de la factura (ej: 2024A)"
    )
    numero_factura = models.CharField(
        max_length=50, 
        default="", 
        help_text="Número secuencial de la factura dentro de la serie"
    )
    tipo_factura = models.CharField(
        max_length=10, 
        default="F1", 
        choices=[
            ('F1', 'Factura Simplificada'),
            ('F2', 'Factura Completa'),
            ('R1', 'Factura Rectificativa'),
        ],
        help_text="Tipo de factura según normativa"
    )

    # QR y Estado
    qr_code = models.TextField(
        default="", 
        blank=True,
        help_text="Cadena de texto completa contenida en el QR"
    )
    sent_to_aeat = models.BooleanField(
        default=False,
        help_text="Indica si el registro ha sido enviado exitosamente a la AEAT"
    )
    esta_facturada = models.BooleanField(
        default=False,
        help_text="Indica si este ticket ha sido canjeado por una Factura Completa"
    )
    xml_firmado = models.TextField(
        blank=True, 
        null=True,
        help_text="XML firmado generado para VeriFactu"
    )

    @staticmethod
    @transaction.atomic 
    @log_excepciones
    def cerrar_cuenta(idm, idc, entrega, idsCobrados, recibo=""):
        # Import aquí para evitar circular import
        from api.tools.smart_receptor import notificar_lineas_borradas
       
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).select_related('infmesa').first()
        if not mesa:
            return (0, -1)

        uid = mesa.infmesa.pk
        now = datetime.now()

        lineas_a_procesar = Lineaspedido.objects.filter(Q(id__in=idsCobrados) | Q(id_local__in=idsCobrados), infmesa__pk=uid)
       
        if not lineas_a_procesar:
            return (0, -1)

        # Crear el ticket principal
        # NOTA: Aquí deberás implementar la lógica para calcular numero_factura y prev_hash
        ticket = Ticket.objects.create(
            hora=now.strftime("%H:%M"),
            fecha=now.strftime("%Y/%m/%d"),
            camarero_id=idc,
            uid=uid,
            entrega=entrega,
            mesa=mesa.mesa.nombre,
            recibo_tarjeta=recibo,
            # fecha_expedicion se llena automáticamente con auto_now_add
        )

        total = 0
        lineas_ticket_a_crear = []
        lineas_a_borrar = []
        
        for linea in lineas_a_procesar:
            total += linea.precio
            lineas_ticket_a_crear.append(
                Ticketlineas(ticket=ticket, linea_id=linea.pk)
            )
            lineas_a_borrar.append({'ID': linea.pk})
            linea.estado = 'C'

        notificar_lineas_borradas(lineas_a_procesar)

        Ticketlineas.objects.bulk_create(lineas_ticket_a_crear)
        Lineaspedido.objects.bulk_update(lineas_a_procesar, ['estado'])

        comunicar_cambios_devices("rm", "lineaspedido", lineas_a_borrar)
        
        if not Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).exists():
            remaining_lineas = Lineaspedido.objects.filter(infmesa__pk=uid)
            if remaining_lineas.exists():
                rm_list = [{'ID': int(i)} for i in remaining_lineas.values_list('id', flat=True)]
                comunicar_cambios_devices("rm", "lineaspedido", rm_list)
                notificar_lineas_borradas(remaining_lineas)

            mesa.delete()

        # --- Integración VeriFactu ---
        from gestion.services.verifactu_service import VerifactuService
        VerifactuService.generar_alta_factura(ticket, total)

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


class Factura(BaseModels):
    ticket = models.OneToOneField(Ticket, on_delete=models.CASCADE, related_name='factura')
    
    # Datos Cliente
    nombre_razon = models.CharField(max_length=200)
    nif = models.CharField(max_length=20)
    direccion = models.CharField(max_length=200)
    cp = models.CharField(max_length=10)
    poblacion = models.CharField(max_length=100)
    provincia = models.CharField(max_length=100, blank=True, null=True)
    email = models.EmailField(blank=True, null=True)
    
    # Datos VeriFactu (Propios de la Factura Completa)
    fecha_expedicion = models.DateTimeField(auto_now_add=True)
    numero_serie = models.CharField(max_length=20, default="")
    numero_factura = models.CharField(max_length=50, default="")
    tipo_factura = models.CharField(max_length=10, default="F2") # F2 = Factura Completa
    
    # Encadenamiento
    prev_hash = models.CharField(max_length=64, default="", blank=True)
    signature = models.CharField(max_length=64, default="", blank=True)
    
    # QR y Estado
    qr_code = models.TextField(default="", blank=True)
    sent_to_aeat = models.BooleanField(default=False)
    xml_firmado = models.TextField(
        blank=True, 
        null=True,
        help_text="XML firmado generado para VeriFactu"
    )

    class Meta:
        db_table = 'facturas'
