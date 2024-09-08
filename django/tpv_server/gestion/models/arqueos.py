from django.db import models
from django.db import connection
from django.db.models.fields import DecimalField
from django.db.models import  Sum, F
from .basemodels import BaseModels
      
class Arqueocaja(BaseModels):
    cierre = models.ForeignKey('Cierrecaja', on_delete=models.CASCADE, db_column='IDCierre')  # Field name made lowercase.
    cambio = models.FloatField(db_column='Cambio')  # Field name made lowercase.
    descuadre = models.FloatField(db_column='Descuadre')  # Field name made lowercase.
    stacke = models.FloatField(db_column='Stacke')
    cambio_real = models.FloatField(db_column="CambioReal", default=0)

    class Meta:
        db_table = 'arqueocaja'
        ordering = ['-id']

    def serialize(self):
        f = self.cierre.fecha.split("/")
        return {
            "id": self.id,
            "hora": self.cierre.hora,
            "fecha": f[2]+'/'+f[1]+'/'+f[0],
            "cambio": self.cambio,
            "descuadre": self.descuadre,
            'totaltarjeta': float(self.cierre.get_total_tarjeta()),
            'totalefectivo_ticado': float(self.cierre.get_efectivo_tickado()),
            'totalefectivo': self.get_efectivo(),
            'gastos': self.get_gastos(),
            'desglose_efectivo': self.get_desglose_efectivo(),
            "des_gastos": self.get_desglose_gastos()
        }



    def get_efectivo(self):
        sql_total = ''.join(['SELECT SUM(tr.subTotal) AS Total ',
                             'FROM (SELECT Can*moneda AS subTotal FROM efectivo WHERE IDArqueo={0}) AS tr',
                             ]).format(self.pk)
        total = 0

        with connection.cursor() as cursor:
            cursor.execute(sql_total)
            row = cursor.fetchone()
            total = row[0] if row[0] != None else 0.00
        return float(total) - self.cambio

    def get_gastos(self):
        sql_total = ''.join(['SELECT SUM(Importe) AS Total ',
                             'FROM gastos ',
                             'WHERE IDArqueo={0}'
                             ]).format(self.pk)
        total = 0

        with connection.cursor() as cursor:
            cursor.execute(sql_total)
            row = cursor.fetchone()
            total = row[0] if row[0] != None else 0.00
        return float(total)

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
            "Fecha": self.cierre.fecha +" - "+self.cierre.hora,
            "TotalTarjeta":  totaltarjeta,
            "TotalEfectivo": totalefectivo_ticado,
            "TotalCaja": float(totaltarjeta) + float(totalefectivo_ticado),
            "CajaReal": float(totalefectivo) + float(totaltarjeta) + float(gastos),
            "des_ventas": self.cierre.get_desglose_ventas(),
            "des_gastos": self.get_desglose_gastos()
        }

        

        return ex

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

class Cierrecaja(BaseModels):
    ticketcom = models.IntegerField(db_column='TicketCom')  # Field name made lowercase.
    ticketfinal = models.IntegerField(db_column='TicketFinal')  # Field name made lowercase.
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.

    class Meta:
        db_table = 'cierrecaja'
        ordering = ['-id']


    def get_efectivo_tickado(self):
        sql_total = ''.join(['SELECT SUM(Precio) AS Total ',
                             'FROM ticketlineas AS tk ',
                             'INNER JOIN lineaspedido AS lp ON tk.IDLinea=lp.ID ',
                             'INNER JOIN ticket ON ticket.ID=tk.IDTicket ',
                             "WHERE ticket.ID >= {0} AND ticket.ID <= {1} AND ticket.Entrega > 0 ",
                             ]).format(self.ticketcom, self.ticketfinal)
        total = 0

        with connection.cursor() as cursor:
            cursor.execute(sql_total)
            row = cursor.fetchone()
            total = row[0] if row[0] != None else 0

        return total

    def get_total_tarjeta(self):
        sql_total = ''.join(['SELECT SUM(lp.Precio) AS Total ',
                             'FROM ticketlineas AS tk ',
                             'INNER JOIN lineaspedido AS lp ON tk.IDLinea=lp.ID ',
                             'INNER JOIN ticket ON ticket.ID=tk.IDTicket ',
                             "WHERE ticket.ID >= {0} AND ticket.ID <= {1} AND ticket.Entrega = 0 ",
                             ]).format(self.ticketcom, self.ticketfinal)
        total = 0

        with connection.cursor() as cursor:
            cursor.execute(sql_total)
            row = cursor.fetchone()
            total = row[0] if row[0] != None else 0

        return total

    def get_desglose_ventas(self):
        sql_pedido = ''.join(['SELECT COUNT(IDArt) as Can, lp.Descripcion_t, SUM(lp.Precio) AS Total ',
                              'FROM ticketlineas AS tk ',
                              'INNER JOIN lineaspedido AS lp ON tk.IDLinea=lp.ID ',
                              'INNER JOIN ticket ON ticket.ID=tk.IDTicket ',
                              "WHERE ticket.ID >= {0} AND ticket.ID <= {1} " ,
                              " GROUP BY lp.IDArt, lp.Descripcion_t, lp.Precio ",
                              " ORDER BY lp.descripcion_t"
                            ]).format(self.ticketcom, self.ticketfinal)

        lineas = []
        with connection.cursor() as cursor:
            cursor.execute(sql_pedido)
            rows = cursor.fetchall()
            for r in rows:
                nombre = r[1] if r[1] else ""
                lineas.append({
                   'Can': r[0],
                   'Nombre': nombre,
                   'Total': r[2],
                   })
        return lineas
 
class Efectivo(BaseModels):
    arqueo = models.ForeignKey(Arqueocaja,  on_delete=models.CASCADE, db_column='IDArqueo')  # Field name made lowercase.
    can = models.IntegerField(db_column='Can')  # Field name made lowercase.
    moneda = models.DecimalField(db_column='Moneda', max_digits=10, decimal_places=2)  # Field name made lowercase.


    class Meta:
        db_table = 'efectivo'

class Gastos(BaseModels):
    arqueo = models.ForeignKey('Arqueocaja',  on_delete=models.CASCADE, db_column='IDArqueo')  # Field name made lowercase.
    descripcion = models.CharField(db_column='Descripcion', max_length=100)  # Field name made lowercase.
    importe = models.DecimalField(db_column='Importe', max_digits=6, decimal_places=2)  # Field name made lowercase.


    class Meta:
        db_table = 'gastos'