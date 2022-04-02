# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-22T00:11:47+02:00
# @License: Apache license vesion 2.0

from __future__ import unicode_literals
from datetime import datetime
from turtle import update
from uuid import uuid4

from django.db.models import Q
from django.db import models
from django.db import connection
from django.contrib.auth.models import User
from django.forms.models import model_to_dict

from app.utility import rgbToHex



class PeticionesAutoria(models.Model):
    idautorizado = models.ForeignKey("Camareros", on_delete=models.CASCADE)
    accion = models.CharField(max_length=150)
    instrucciones = models.CharField(max_length=300)

class Sync(models.Model):
    nombre = models.CharField(max_length=50) 
    last = models.CharField(max_length=26)

    @staticmethod
    def actualizar(tb_name):
        sync = Sync.objects.filter(nombre=tb_name).first()
        if not sync:
            sync = Sync()
        sync.nombre = tb_name
        sync.last = datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
        sync.save()
        


class Arqueocaja(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    cierre = models.ForeignKey('Cierrecaja', on_delete=models.CASCADE, db_column='IDCierre')  # Field name made lowercase.
    cambio = models.FloatField(db_column='Cambio')  # Field name made lowercase.
    descuadre = models.FloatField(db_column='Descuadre')  # Field name made lowercase.

    class Meta:
        db_table = 'arqueocaja'
        ordering = ['-id']

    def save(self, *args, **kwargs):
        sync = Sync.objects.filter(nombre=self._meta.db_table).first()
        if not sync:
            sync = Sync()
        sync.nombre = self._meta.db_table
        sync.save()
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        sync = Sync.objects.filter(nombre=self._meta.db_table).first()
        if not sync:
            sync = Sync()
        sync.nombre = self._meta.db_table
        sync.save()
        return super().delete( *args, **kwargs)



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
            "des_gastos": []
        }

        for g in self.gastos_set.all():
            ex["des_gastos"].append({
                 "ID": g.id,
                 "IDArqueo": self.pk,
                 "Descripcion": g.descripcion,
                 "Importe": g.importe
            })

        return ex


class Camareros(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=100)  # Field name made lowercase.
    apellidos = models.CharField(db_column='Apellidos', max_length=100)  # Field name made lowercase.
    email = models.CharField(db_column='Email', max_length=50, null=True)  # Field name made lowercase.
    pass_field = models.CharField(db_column='Pass', max_length=100, null=True)  # Field name made lowercase. Field renamed because it was a Python reserved word.
    activo = models.IntegerField(db_column='Activo', default=1)  # Field name made lowercase.
    autorizado = models.IntegerField(db_column='Autorizado', default=1)  # Field name made lowercase.
    permisos = models.CharField(db_column='Permisos', max_length=100, null=True, default="")  # Field name made lowercase. Field renamed because it was a Python reserved word.
    
    @staticmethod
    def update_from_device(row):
        c = Camareros.objects.filter(id=row["ID"]).first()
        if not c:
            c = Camareros()
            
        c.autorizado = int(row["autorizado"])
        c.pass_field = row["Pass"]
        c.permisos = row["permisos"]
        c.save()
        

    @staticmethod
    def update_for_devices():
        rows = Camareros.objects.filter(activo=1)
        tb = []
        for r in rows:
            tb.append({
                "ID":r.id,
                "Nombre": r.nombre,
                "Apellidos": r.apellidos,
                "Pass": r.pass_field,
                "autorizado": r.autorizado,
                "permisos": r.permisos
            })
        return tb
    
    
    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)



    class Meta:
        db_table = 'camareros'
        ordering = ["apellidos"]


class Cierrecaja(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    ticketcom = models.IntegerField(db_column='TicketCom')  # Field name made lowercase.
    ticketfinal = models.IntegerField(db_column='TicketFinal')  # Field name made lowercase.
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.



    class Meta:
        db_table = 'cierrecaja'
        ordering = ['-id']


    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


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
        sql_pedido = ''.join(['SELECT COUNT(IDArt) as Can, Nombre, SUM(lp.Precio) AS Total ',
                              'FROM ticketlineas AS tk ',
                              'INNER JOIN lineaspedido AS lp ON tk.IDLinea=lp.ID ',
                              'INNER JOIN ticket ON ticket.ID=tk.IDTicket ',
                              "WHERE ticket.ID >= {0} AND ticket.ID <= {1} " ,
                              " GROUP BY lp.IDArt, lp.Nombre, lp.Precio ",
                              " ORDER BY lp.Nombre"
                            ]).format(self.ticketcom, self.ticketfinal)

        lineas = []
        with connection.cursor() as cursor:
            cursor.execute(sql_pedido)
            rows = cursor.fetchall()
            for r in rows:
                lineas.append({
                   'Can': r[0],
                   'Nombre': r[1],
                   'Total': r[2],
                   })
        return lineas
    

class Efectivo(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    arqueo = models.ForeignKey(Arqueocaja,  on_delete=models.CASCADE, db_column='IDArqueo')  # Field name made lowercase.
    can = models.IntegerField(db_column='Can')  # Field name made lowercase.
    moneda = models.DecimalField(db_column='Moneda', max_digits=5, decimal_places=2)  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'efectivo'


class Familias(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=40)  # Field name made lowercase.
    tipo = models.CharField(db_column='Tipo', max_length=6)  # Field name made lowercase.
    numtapas = models.IntegerField("Numero de tapas", db_column='NumTapas', null=True, blank=True)  # Field name made lowercase.
    receptor = models.ForeignKey('Receptores',  on_delete=models.CASCADE, db_column='IDReceptor')  # Field name made lowercase.


    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'familias'
        ordering = ['-id']


class Gastos(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    arqueo = models.ForeignKey(Arqueocaja,  on_delete=models.CASCADE, db_column='IDArqueo')  # Field name made lowercase.
    descripcion = models.CharField(db_column='Descripcion', max_length=100)  # Field name made lowercase.
    importe = models.DecimalField(db_column='Importe', max_digits=6, decimal_places=2)  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'gastos'


class Historialnulos(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    lineapedido = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE, db_column='IDLPedido')  # Field name made lowercase.
    camarero = models.ForeignKey(Camareros,  on_delete=models.CASCADE, db_column='IDCam')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    motivo = models.CharField(db_column='Motivo', max_length=200)  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'historialnulos'
        ordering = ['-id']


class HorarioUsr(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    hora_ini = models.CharField(db_column='Hora_ini', max_length=5)  # Field name made lowercase.
    hora_fin = models.CharField(db_column='Hora_fin', max_length=5)  # Field name made lowercase.
    usurario = models.ForeignKey(User,  on_delete=models.CASCADE, db_column='IDUsr')  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'horario_usr'


class Infmesa(models.Model):
    uid = models.CharField(db_column='UID', primary_key=True, unique=True, max_length=100)  # Field name made lowercase.
    camarero = models.ForeignKey(Camareros,  on_delete=models.CASCADE, db_column='IDCam')  # Field name made lowercase.
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    ref = models.CharField(db_column='Ref', max_length=100)  # Field name made lowercase.
    numcopias = models.IntegerField(db_column='NumCopias', default=0)  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'infmesa'
        ordering = ['-fecha']


class Lineaspedido(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    pedido = models.ForeignKey('Pedidos',  on_delete=models.CASCADE, db_column='IDPedido')  # Field name made lowercase.
    infmesa = models.ForeignKey(Infmesa, on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    idart = models.IntegerField(db_column='IDArt')  # Field name made lowercase.
    estado = models.CharField(db_column='Estado', max_length=1)  # Field name made lowercase.
    precio = models.DecimalField(db_column='Precio', max_digits=6, decimal_places=2)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=400)  # Field name made lowercase.
    tecla = models.ForeignKey('Teclas', on_delete=models.SET_NULL, null=True)  # Field name made lowercase.
    
    @staticmethod
    def update_for_devices():
        mesas = Mesasabiertas.objects.all()
        lineas = []
        for m in mesas:
            for l in Lineaspedido.objects.filter(infmesa__pk=m.infmesa.pk, estado='P'):
                obj = {
                    'ID': l.pk,
                    'IDPedido': l.pedido_id,
                    'UID': m.infmesa.pk,
                    'IDArt': l.idart,
                    'Estado': l.estado,
                    'Precio': l.precio,
                    'Nombre': l.nombre,
                    'IDMesa': m.mesa.pk
                }
                lineas.append(obj)
        return lineas

    def borrar_linea_pedido(idm, p, idArt, can, idc, motivo, s, n):
        num = -1
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        if mesa:
            uid = mesa.infmesa.pk
            reg = Lineaspedido.objects.filter(infmesa__pk=uid, idart=idArt, estado=s, precio=p, nombre=n)[:can]
    
            for r in reg:
                if motivo != 'null':
                    h = Historialnulos()
                    h.lineapedido_id = r.id
                    h.camarero_id = idc
                    h.motivo = motivo
                    h.hora = datetime.now().strftime("%H:%M")
                    h.save()
                    r.estado = 'A'
                    r.save()
                else:
                    r.delete()

            num = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='N')) & Q(infmesa__pk=uid)).count()
           
            if num <= 0:
                Mesasabiertas.objects.filter(infmesa__pk=uid).delete()

        return num

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'lineaspedido'


class Mesas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', unique=True, max_length=50)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden')  # Field name made lowercase.

    @staticmethod
    def update_from_device(row):
        pass
        

    @staticmethod
    def update_for_devices():
        return Mesas.get_all_for_devices()
    
    
    @staticmethod
    def get_all_for_devices():
        lsMesas = []
        mesas = Mesas.objects.all()
        for m in mesas:
            zona =  m.mesaszona_set.all().first()
            if zona:
                zona = zona.zona
                obj = {
                'ID': m.id,
                'Nombre': m.nombre,
                'Orden': m.orden,
                'num': 0,
                'abierta': False,
                'RGB': zona.rgb,
                'IDZona': zona.id,
                "Tarifa": zona.tarifa,
                }
   
                mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=m.id).first()
                if mesa_abierta:
                    obj["num"] = mesa_abierta.infmesa.numcopias
                    obj["abierta"] = True;

            lsMesas.append(obj)
        return lsMesas

    def get_color(self, hex=False):
        s = self.mesaszona_set.all().first()
        color = "244,155,123"
        if s:
           color = s.zona.rgb

        return color if not hex else rgbToHex(color)


    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        db_table = 'mesas'
        ordering = ['-orden']


class Mesasabiertas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    infmesa = models.ForeignKey(Infmesa, on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    mesa = models.ForeignKey(Mesas, on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.


    @staticmethod
    def update_for_devices():
        mesas = Mesasabiertas.objects.all()
        objs = []
        for m in mesas:
            obj = {
                "ID": m.mesa_id,
                "num": m.infmesa.numcopias
            }
            objs.append(obj)
        return objs


    @staticmethod
    def borrar_mesa_abierta(idm, idc, motivo):
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        if mesa:
            uid = mesa.infmesa.pk
            reg = Lineaspedido.objects.filter(Q(infmesa__pk=uid) & (Q(estado='R') |
                                              Q(estado='P') |
                                              Q(estado='N')))
            for r in reg:
                historial = Historialnulos()
                historial.lineapedido_id = r.pk
                historial.camarero_id = idc
                historial.motivo = motivo
                historial.hora = datetime.now().strftime("%H:%M")
                historial.save()
                r.estado = 'A'
                r.save()

            mesa.delete()


    @staticmethod
    def cambiar_mesas_abiertas(idp, ids):
        if idp != ids:
            mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
            mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()
            if mesaS:
                infmesa = mesaS.infmesa
                mesaS.infmesa = mesaP.infmesa
                mesaS.save()
                mesaP.infmesa = infmesa;
                mesaP.save()
            else:
                mesaP.mesa_id = ids
                mesaP.save()

    @staticmethod
    def juntar_mesas_abiertas(idp, ids):
        if idp != ids:
            mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
            mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()
            if mesaS:
                infmesa = mesaS.infmesa
                pedidos = Pedidos.objects.filter(infmesa__pk=infmesa.pk)
                for pedido in pedidos:
                    pedido.infmesa_id = mesaP.infmesa.pk
                    pedido.save()
                    for l in pedido.lineaspedido_set.all():
                        l.infmesa_id = mesaP.infmesa.pk
                        l.save()
                infmesa.delete()

   
    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'mesasabiertas'
        ordering = ['-id']


class Mesaszona(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    zona = models.ForeignKey('Zonas',  on_delete=models.CASCADE, db_column='IDZona')  # Field name made lowercase.
    mesa = models.ForeignKey(Mesas,  on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        super().delete(*args, **kwargs)


    class Meta:
        db_table = 'mesaszona'

class Pedidos(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    infmesa = models.ForeignKey(Infmesa,  on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    mensaje =  models.CharField(db_column='Mensaje', max_length=300, default="", null=True)  # Field name made lowercase.

    @staticmethod
    def agregar_nuevas_lineas(idm, idc, lineas):
        is_mesa_nueva = False
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        if not mesa:
            infmesa = Infmesa()
            infmesa.ref = ""
            infmesa.camarero_id = idc
            infmesa.hora = datetime.now().strftime("%H:%M")
            infmesa.fecha = datetime.now().strftime("%Y/%m/%d")
            infmesa.uid = idm + '-' + str(uuid4())
            infmesa.save()

            mesa = Mesasabiertas()
            mesa.mesa_id = idm
            mesa.infmesa_id = infmesa.pk
            mesa.save()
            is_mesa_nueva = True

        pedido = Pedidos()
        pedido.infmesa_id = mesa.infmesa.pk
        pedido.hora = datetime.now().strftime("%H:%M")
        pedido.camarero_id = idc
        pedido.save()

        for pd in lineas:
            can = int(pd["Can"])
            for i in range(0, can):
                linea = Lineaspedido()
                linea.infmesa_id = mesa.infmesa.pk
                linea.idart = pd["IDArt"]
                linea.pedido_id = pedido.pk
                linea.nombre = pd["Nombre"]
                linea.precio = pd["Precio"]
                linea.estado = 'R' if pd['Precio'] == 0 else 'P'
                linea.save()

        return is_mesa_nueva

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'pedidos'
        ordering = ['-id']

class Receptores(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=40)  # Field name made lowercase.
    nomimp = models.CharField(db_column='nomImp', max_length=40)  # Field name made lowercase.
    activo = models.BooleanField(db_column='Activo', default=True)  # Field name made lowercase.
    descripcion = models.CharField(db_column='Descripcion', max_length=200, default="")  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    def __unicode__(self):
        return self.nombre +'  ' +self.descripcion

    def __str__(self):
        return self.nombre + ' ' +self.descripcion

    class Meta:
        db_table = 'receptores'

class Secciones(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=50)  # Field name made lowercase.
    color = models.CharField(db_column='Color', max_length=20, null=True)  # Field name made lowercase.
    rgb = models.CharField("Color", db_column='RGB', max_length=11)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden')  # Field name made lowercase.
    
    @staticmethod
    def update_for_devices():
        rows = Secciones.objects.all()
        objs = []
        for r in rows:
            objs.append(model_to_dict(r))
        return objs

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'secciones'
        ordering = ['-orden']


ICON_CHOICES = (
    ("bar", "Bar"),
    ("bebida", "Bebida"),
    ("bocadillos", "Bocadillos"),
    ("carne", "Carne"),
    ("comida", "Comidas"),
    ("desayunos", "Desayunos"),
    ("meriendas", "Meriendas"),
    ("pasteles", "Pasteles"),
    ("pescado", "Pescados"),
    ("tapas", "Tapas"),
    ("pizzas", "Pizzas"),
    ("tartas", "Tartas"),
    ("copas", "Copas"),
)


class SeccionesCom(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=11)  # Field name made lowercase.
    es_promocion = models.BooleanField(db_column='Es_promocion', blank=True, null=True, default="False")
    descuento = models.DecimalField(db_column='Descuento', blank=True, null=True, max_digits=4, decimal_places=2, default='0')
    icono = models.CharField(db_column='Icono', max_length=11, choices=ICON_CHOICES, default="bar")

    

    @staticmethod
    def get_all_for_devices():
        sec = SeccionesCom.objects.all()

        lstObj = []
        for s in sec:
            obj = {
                "ID": s.id,
                "Nombre": s.nombre,
                "Es_promocion": s.es_promocion,
                "Descuento": s.descuento,
                "Icono": s.icono,
            }
            lstObj.append(obj)

        return lstObj

        
    def save(self, *args, **kwargs):
        if self.descuento and self.descuento > 1:
            self.descuento = 1
        elif not self.descuento  or  self.descuento < 0:
            self.descuento = 0

        Sync.actualizar(self._meta.db_table)
        super(SeccionesCom, self).save(*args, **kwargs)

      

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'secciones_com'

class Servidos(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    linea = models.ForeignKey(Lineaspedido,  on_delete=models.CASCADE, db_column='IDLinea')  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'servidos'
        ordering = ['-id']

class Subteclas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.
    tecla_child = models.ForeignKey('Teclas',  on_delete=models.SET_NULL, null=True, related_name='teclas_child')  # Field name made lowercase.
    nombre = models.CharField(max_length=100, null=True, blank=True)
    incremento = models.DecimalField(blank=True, null=True, max_digits=4, decimal_places=2, default='0')
    descripcion_r = models.CharField("Descripción recepción", db_column='Descripcion_r', max_length=300, null=True, blank=True)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
    orden = models.IntegerField(db_column='Orden', default=0, blank=True)  # Field name made lowercase.
    
    def get_color(self, hex=False):
        if self.tecla_child:
            return self.tecla_child.get_color(hex)
        else:
            return self.tecla.get_color(hex)

        
    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'subteclas'
        ordering = ['-orden']

class Sugerencias(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla', default=-1)  # Field name made lowercase.
    sugerencia = models.CharField(db_column='Sugerencia', max_length=300)  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'sugerencias'

TIPO_TECLA_CHOICE = [
    ("SP", "SIMPLE"),
    ("ML", "COMPUESTA"),
    ("GR", "GRUPO")
    ]

class Teclas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=50)  # Field name made lowercase.
    p1 = models.DecimalField(db_column='P1', max_digits=6, decimal_places=2)  # Field name made lowercase.
    p2 = models.DecimalField(db_column='P2', max_digits=6, decimal_places=2)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0, blank=True)  # Field name made lowercase.
    familia = models.ForeignKey(Familias,  on_delete=models.CASCADE, db_column='IDFamilia')  # Field name made lowercase.
    tag = models.CharField(db_column='Tag', max_length=100, default='', blank=True)  # Field name made lowercase.
    ttf = models.CharField("TTF", db_column='TTF', max_length=50, default='', blank=True)  # Field name made lowercase.
    descripcion_r = models.CharField("Descripción recepción", db_column='Descripcion_r', max_length=300, null=True, blank=True)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
    tipo = models.CharField(max_length=2, choices=TIPO_TECLA_CHOICE, default="SP")

    @staticmethod
    def update_for_devices():
        rows = Teclas.objects.all()
        row = None
        objs = []
        for r in rows:
            teclasseccion = r.teclaseccion_set.all()
            row = model_to_dict(r)
            row["Precio"] = r.p1 
            row['RGB'] = teclasseccion[0].seccion.rgb if teclasseccion.count() > 0 else "150,150,150"
            row['IDSeccion'] = teclasseccion[0].seccion.id if teclasseccion.count() > 0 else -1
            row["IDSec2"] = teclasseccion[1].seccion.id if teclasseccion.count() > 1 else -1
            objs.append(row)
        return objs
   
    def get_color(self, hex=False):
        s = self.teclaseccion_set.all().first()
        color = "244,155,123"
        if s:
           color = s.seccion.rgb
        return color if not hex else rgbToHex(color)

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)

    def get_items_edit(self, orden=None, show_secciones=False):
        if orden == None:
            orden = self.orden

        result = ""
        if show_secciones:
            secciones = self.teclaseccion_set.all().values_list("seccion__nombre", flat=True)
            result= ", ".join(secciones)
        else:
           result = self.get_tipo_display()

        obj = {
            'cols': [self.nombre, "%.2f" % float(self.p1), "%.2f" % float(self.p2), result, orden],
            'botones': [{'tipo':'edit', 'icon':'fas fa-edit'},
                        {'tipo':'edit_orden', 'icon': 'fas fa-sort-amount-down'},
                        {'tipo':'sugerencias', 'icon':'fas fa-comment'}],
            "obj": {"id": self.id,"nombre":self.nombre, "orden":self.orden,
                    "p1": "%.2f" % float(self.p1), "p2": "%.2f" % float(self.p2),
                    "familia": self.familia.id,
                    "tag": self.tag if self.tag else "", "ttf": self.ttf if self.ttf else "",
                    "descripcion_r": self.descripcion_r if self.descripcion_r else "",
                    "descripcion_t": self.descripcion_t if self.descripcion_t else "",
                    "tipo": self.tipo, 'color': self.get_color(hex=True)
                    }
            }
        
        if self.tipo == "ML":
            obj['botones'].append({'tipo':'ML', 'icon':'fas fa-grip-vertical'})
        elif self.tipo == "GR":
            obj['botones'].append({'tipo':'GR', 'icon':'fas fa-grip-vertical'})
                   
        obj['botones'].append({'tipo':'borrar', 'icon':'far fa-trash'})
      
        return obj

    def get_items_add(self, show_secciones=False):
        result = self.get_tipo_display()
        if show_secciones:
            secciones = self.teclaseccion_set.all().values_list("seccion__nombre", flat=True)
            result= ", ".join(secciones)
        else:
            result = self.get_tipo_display()

        return {'cols': [self.nombre, "%.2f" % float(self.p1), "%.2f" % float(self.p2), result, self.orden],
                'botones': [{'tipo':'add', 'icon':'fa-plus'}],
                'obj': {"id": self.id, "nombre": self.nombre}
               }
        


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'teclas'
        ordering = ['-orden']


class Teclascom(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.SET_NULL, db_column='IDTecla', null=True)  # Field name made lowercase.
    seccion = models.ForeignKey(SeccionesCom,  on_delete=models.CASCADE, db_column='IDSeccion')  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden')  # Field name made lowercase.
   
    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'teclascom'
        ordering = ['-orden']


class Teclaseccion(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    seccion = models.ForeignKey(Secciones,  on_delete=models.CASCADE, db_column='IDSeccion')  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'teclaseccion'


class Ticket(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    entrega = models.DecimalField(db_column='Entrega', max_digits=6, decimal_places=2)  # Field name made lowercase.
    uid = models.CharField(db_column='UID', max_length=100)  # Field name made lowercase.
    mesa = models.CharField(db_column='Mesa', max_length=40)  # Field name made lowercase.

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
            ticket.fecha = datetime.now().strftime("%Y/%m/%d")
            ticket.camarero_id = idc
            ticket.uid = uid
            ticket.entrega = entrega
            ticket.mesa = mesa.mesa.nombre
            ticket.save()
            id = ticket.id


            for l in art:
                can = int(l["Can"])
                reg = Lineaspedido.objects.filter(Q(infmesa__pk=uid) & Q(idart=l["IDArt"]) &
                                                Q(precio=l["Precio"]) &
                                                (Q(estado='P') | Q(estado='N')))[:can]

                for r in reg:
                    total = total + r.precio
                    linea = Ticketlineas()
                    linea.ticket_id = ticket.pk
                    linea.linea_id = r.pk
                    linea.save()
                    r.estado = 'C'
                    r.save()

            numart = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='N')) & Q(infmesa__pk=uid)).count()
            if numart <= 0:
                Mesasabiertas.objects.filter(infmesa__pk=uid).delete()

        return (numart, total, id)

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'ticket'


class Ticketlineas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    ticket = models.ForeignKey(Ticket,  on_delete=models.CASCADE, db_column='IDTicket')  # Field name made lowercase.
    linea = models.ForeignKey(Lineaspedido,  on_delete=models.CASCADE, db_column='IDLinea')  # Field name made lowercase.

    

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete( *args, **kwargs)


    class Meta:
        db_table = 'ticketlineas'


class Zonas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', unique=True, max_length=50)  # Field name made lowercase.
    tarifa = models.IntegerField(db_column='Tarifa')  # Field name made lowercase.
    rgb = models.CharField("Color", db_column='RGB', max_length=50)  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        zonas = Zonas.objects.all()
        objs = []
        for z in zonas:
            objs.append(model_to_dict(z))
        return  objs
        

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    

    def save(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        Sync.actualizar(self._meta.db_table)
        return super().delete(*args, **kwargs)


    class Meta:
        db_table = 'zonas'

