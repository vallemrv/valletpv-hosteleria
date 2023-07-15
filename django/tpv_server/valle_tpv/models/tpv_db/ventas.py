from django.db import models
from django.contrib.auth.models import User
from django.db.models import Count, Sum, F
from django.db.models.fields import DecimalField
from valle_tpv.models import Ticket, Ticketlineas


class HorarioUsr(models.Model):
    id = models.AutoField( primary_key=True) 
    hora_ini = models.CharField( max_length=5) 
    hora_fin = models.CharField( max_length=5) 
    usurario = models.ForeignKey(User,  on_delete=models.CASCADE) 

    class Meta:
        db_table = 'horario_usr'



class Arqueocaja(models.Model):
    id = models.AutoField(primary_key=True) 
    cierre = models.ForeignKey('Cierrecaja', on_delete=models.CASCADE) 
    cambio = models.FloatField()
    descuadre = models.FloatField() 

    class Meta:
        db_table = 'arqueocaja'
        ordering = ['-id']

    def serialize(self):
        f = self.cierre.fecha.strftime("%d/%m/%Y")
        return {
            "id": self.id,
            "hora": self.cierre.hora,
            "fecha": f,
            "cambio": self.cambio,
            "descuadre": self.descuadre,
            'totaltarjeta': float(self.cierre.get_total_tarjeta()),
            'totalefectivo_ticado': float(self.cierre.get_efectivo_tickado()),
            'totalefectivo': self.get_efectivo(),
            'gastos': self.get_gastos(),
            'desglose_efectivo': self.get_desglose_efectivo(),
            "des_gastos": self.get_desglose_gastos()
        }

 

    def get_desglose_cierre(self):
        totaltarjeta = float(self.cierre.get_total_tarjeta())
        totalefectivo_ticado = float(self.cierre.get_efectivo_tickado())
        totalefectivo = self.get_efectivo()
        gastos = self.get_gastos()
        ex = {
            "ID": self.pk,
            "Descuadre": self.descuadre,
            "IDCierre": self.cierre.pk,
            "Cambio": self.cambio,
            "gastos": gastos,
            "Fecha": self.cierre.fecha.strftime("%d/%m/%Y") +" - "+self.cierre.hora,
            "TotalTarjeta":  totaltarjeta,
            "TotalEfectivo": totalefectivo_ticado,
            "TotalCaja": float(totaltarjeta) + float(totalefectivo_ticado),
            "CajaReal": float(totalefectivo) + float(totaltarjeta) + float(gastos),
            "des_ventas": self.cierre.get_desglose_ventas(),
            "des_gastos": self.get_desglose_gastos()
        } 

        return ex
    
    def get_efectivo(self):
        total = Efectivo.objects.filter(arqueo__pk=self.pk).aggregate(total=Sum(F('Can') * F('moneda')))['total']
        total = total if total is not None else 0.00
        return float(total) - self.cambio

    def get_gastos(self):
        total = Gastos.objects.filter(arqueo__pk=self.pk).aggregate(total=Sum('Importe'))['total']
        total = total if total is not None else 0.00
        return float(total)

    def get_desglose_gastos(self):
        dg = []
        for g in self.gastos_set.all():
            dg.append(g.serialize())
        return dg

    def get_desglose_efectivo(self):
        efectivo = self.efectivo_set.all().values("moneda").annotate(can = Sum("can")).order_by("-moneda")
        total_conteo =  self.efectivo_set.all().aggregate(total=Sum(F("can") * F("moneda"), output_field=DecimalField()))['total']
        if total_conteo:
            retirar = float(total_conteo) - float(self.cambio)
            lineas_retirar = []
            lineas_cambio = []
            parcial = 0
            for linea in efectivo:
                can = linea["can"]
                moneda = linea["moneda"]
            
                if retirar <= parcial:
                    if can > 0:
                        lineas_cambio.append({"titulo": "Cambio", 'can':can,'tipo':float(moneda), 'texto_tipo': self.get_texto_desglose(can, moneda) })
                elif retirar > ((can * float(moneda)) + parcial):
                    parcial = parcial + float((can * moneda))
                    if can > 0:
                        lineas_retirar.append({"titulo": "Retirar", 'can':can,'tipo':float(moneda), 'texto_tipo': self.get_texto_desglose(can, moneda) })
                else:
                    diferencia = retirar - parcial
                    can_parcial = int(diferencia/float(moneda))
                    parcial = parcial + (can_parcial * float(moneda))
                    if can_parcial > 0:
                        lineas_retirar.append({"titulo": "Retirar", 'can':can_parcial,'tipo':float(moneda), 'texto_tipo': self.get_texto_desglose(can_parcial, moneda) })
                    if can - can_parcial > 0:
                        lineas_cambio.append({"titulo": "Cambio", 'can':can - can_parcial, 'tipo':float(moneda), 'texto_tipo': self.get_texto_desglose(can - can_parcial, moneda) })

        return {"lineas_cambio":lineas_cambio, "lineas_retirar":lineas_retirar}
        
    def get_texto_desglose(self, can, moneda):
        texto = "moneda" if moneda < 5 else "billete"
        texto = texto + "s" if can > 1 else texto
        return texto


class Cierrecaja(models.Model):
    id = models.AutoField( primary_key=True) 
    ticketcom = models.IntegerField() 
    ticketfinal = models.IntegerField() 
    fecha = models.DateField() 
    hora = models.CharField( max_length=5) 

    class Meta:
        db_table = 'cierrecaja'
        ordering = ['-id']
    

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



    def get_efectivo_tickado(self):
        total = Ticket.objects.filter(
            ID__range=(self.ticketcom, self.ticketfinal), 
            Entrega__gt=0,
            ticketlineas__lineaspedido__isnull=False
        ).aggregate(
            total=Sum('ticketlineas__lineaspedido__Precio')
        )['total']

        total = total if total is not None else 0.00
        return total

    def get_total_tarjeta(self):
        total = Ticket.objects.filter(
            ID__range=(self.ticketcom, self.ticketfinal), 
            Entrega=0,
            ticketlineas__lineaspedido__isnull=False
        ).aggregate(
            total=Sum('ticketlineas__lineaspedido__Precio')
        )['total']

        total = total if total is not None else 0.00
        return total


    def get_desglose_ventas(self):
        lineas = Ticket.objects.filter(
            ID__range=(self.ticketcom, self.ticketfinal),
            ticketlineas__lineaspedido__isnull=False
        ).values(
            'ticketlineas__lineaspedido__IDArt', 
            'ticketlineas__lineaspedido__Descripcion_t',
            'ticketlineas__lineaspedido__Precio'
        ).annotate(
            Can=Count('ticketlineas__lineaspedido__IDArt'),
            Total=Sum('ticketlineas__lineaspedido__Precio')
        ).order_by('ticketlineas__lineaspedido__Descripcion_t')

        return list(lineas)
 
class Efectivo(models.Model):
    id = models.AutoField( primary_key=True) 
    arqueo = models.ForeignKey(Arqueocaja,  on_delete=models.CASCADE,) 
    can = models.IntegerField() 
    moneda = models.DecimalField( max_digits=5, decimal_places=2) 

    class Meta:
       db_table = 'efectivo'

class Gastos(models.Model):
    id = models.AutoField( primary_key=True) 
    arqueo = models.ForeignKey(Arqueocaja,  on_delete=models.CASCADE, ) 
    descripcion = models.CharField(max_length=100) 
    importe = models.DecimalField(max_digits=6, decimal_places=2) 

    def serialize(self):
        return {
            "ID": self.id,
            "IDArqueo": self.arqueo_id,
            "Descripcion": self.descripcion,
            "Importe": self.importe
            }
    

    class Meta:
        db_table = 'gastos'