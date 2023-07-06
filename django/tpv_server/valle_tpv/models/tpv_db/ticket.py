from django.db import models
from django.db.models import Q
from valle_tpv.models import Mesasabiertas, Lineaspedido
from valle_tpv.tools.ws import comunicar_cambios_devices
from datetime import datetime

class Ticket(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    fecha = models.DateField(db_column='Fecha')  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    entrega = models.DecimalField(db_column='Entrega', max_digits=6, decimal_places=2)  # Field name made lowercase.
    uid = models.CharField(db_column='UID', max_length=100)  # Field name made lowercase.
    mesa = models.CharField(db_column='Mesa', max_length=40)  # Field name made lowercase.
    url_factura = models.CharField(max_length=140, default="")  # Field name made lowercase.
    
    class Meta:
        default_permissions = ()


    @staticmethod
    def cerrar_cuenta(idm, idc, entrega, art):
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        total = 0
        numart = -1
        id = -1
     
        if mesa:
            uid = mesa.infmesa.pk
            ticket = Ticket()
            ticket.hora = datetime.now().strftime("%H:%M")
            ticket.fecha = datetime.now() 
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
      

        return (total, id)



class Ticketlineas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    ticket = models.ForeignKey(Ticket,  on_delete=models.CASCADE, db_column='IDTicket')  # Field name made lowercase.
    linea = models.ForeignKey(Lineaspedido,  on_delete=models.CASCADE, db_column='IDLinea')  # Field name made lowercase.

    class Meta:
        default_permissions = ()
