from django.db import models
from django.db.models import Q
from valle_tpv.models import Mesasabiertas, Lineaspedido
from valle_tpv.tools.ws import comunicar_cambios_devices
from datetime import datetime

class Ticket(models.Model):
    id = models.AutoField(primary_key=True) 
    fecha = models.DateField() 
    camarero = models.ForeignKey("Camareros", on_delete=models.SET_NULL, null=True, blank=True) 
    hora = models.CharField( max_length=5) 
    entrega = models.DecimalField( max_digits=6, decimal_places=2) 
    uid = models.CharField( max_length=100) 
    mesa = models.CharField( max_length=40) 
    url_factura = models.CharField(max_length=140, default="") 
    
    class Meta:
        default_permissions = ()


    @staticmethod
    def cobrar_cuenta(idm, idc, entrega, ids):
        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        total = 0
        numart = -1
        id = -1
     
        if mesa_abierta:
            uid = mesa_abierta.infmesa.pk
            ticket = Ticket()
            ticket.hora = datetime.now().strftime("%H:%M")
            ticket.fecha = datetime.now() 
            ticket.camarero_id = idc
            ticket.uid = uid
            ticket.entrega = entrega
            ticket.mesa = mesa_abierta.mesa.nombre
            ticket.save()
            id = ticket.id


            for pk in ids:
                reg = Lineaspedido.objects.filter(pk=pk).first()

                total = total + reg.precio
                linea = Ticketlineas()
                linea.ticket_id = ticket.pk
                linea.linea_id = reg.pk
                linea.save()
                reg.estado = 'C'
                reg.save()
                
                comunicar_cambios_devices("delete", "lineaspedido", ids)

            numart = Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).count()
            
            if numart <= 0:
                mesa_abierta.delete()
                s = mesa_abierta.mesa.serialize()
                comunicar_cambios_devices("update", "mesas", [s])
              
      

        return (total, id)



class Ticketlineas(models.Model):
    id = models.AutoField( primary_key=True) 
    ticket = models.ForeignKey(Ticket,  on_delete=models.CASCADE) 
    linea = models.ForeignKey(Lineaspedido,  on_delete=models.CASCADE) 

    class Meta:
        default_permissions = ()

