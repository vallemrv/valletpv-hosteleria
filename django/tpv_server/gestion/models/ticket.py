from django.db import models
from django.db.models import Q
from .mesasabiertas import  Mesasabiertas
from .arqueos import Cierrecaja
from .basemodels import BaseModels, Sync
from comunicacion.tools import comunicar_cambios_devices
from datetime import datetime     


class Ticket(BaseModels):
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    entrega = models.DecimalField(db_column='Entrega', max_digits=6, decimal_places=2)  # Field name made lowercase.
    uid = models.CharField(db_column='UID', max_length=100)  # Field name made lowercase.
    mesa = models.CharField(db_column='Mesa', max_length=40)  # Field name made lowercase.
    url_factura = models.CharField(max_length=140, default="")  # Field name made lowercase.

    @staticmethod
    def cerrar_cuenta(idm, idc, entrega, art):
        from .pedidos import Lineaspedido
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        total = 0
        numart = -1
        id = -1
     
        if mesa:
            uid = mesa.infmesa.pk
            ticket = Ticket()
            ticket.hora = datetime.now().strftime("%H:%M")
            ticket.fecha = datetime.now().strftime("%Y/%m/%d")
            ticket.camarero_id = idc
            ticket.uid = uid
            ticket.entrega = entrega
            ticket.mesa = mesa.mesa.nombre
            ticket.save()
            id = ticket.id


            for l in art:
                can = int(l["Can"])
                reg = Lineaspedido.objects.filter(Q(infmesa__pk=uid) & Q(idart=l["IDArt"]) &  Q(precio=l["Precio"]) &
                                                  Q(descripcion_t=l["descripcion_t"]) & (Q(estado="P")))[:can]

                for r in reg:
                    total = total + r.precio
                    linea = Ticketlineas()
                    linea.ticket_id = ticket.pk
                    linea.linea_id = r.pk
                    linea.save()
                    r.estado = 'C'
                    r.save()
                    
                    comunicar_cambios_devices("rm", "lineaspedido", {"ID":r.id}, {"op": "cobrado", "precio": float(r.precio)})

            numart = Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).count()
           
            if numart <= 0:
                for l in Mesasabiertas.objects.filter(infmesa__pk=uid):
                    s = l.serialize()
                    s["abierta"] = 0
                    s["num"] = 0
                    comunicar_cambios_devices("md", "mesasabiertas", s)
                    l.delete()
                Sync.actualizar(Mesasabiertas._meta.db_table)

        return (total, id)


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

