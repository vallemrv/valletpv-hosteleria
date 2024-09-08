# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-22T00:11:47+02:00
# @License: Apache license vesion 2.0
from __future__ import unicode_literals
from django.db import models
from .basemodels import BaseModels


ICON_CHOICES = (
    ("bar", "Bar"),
    ("bocadillo", "Bocadillo"),
    ("carne", "Carne"),
    ("cocktel", "Cocktel"),
    ("copa_con_limon", "Copa con rodaja de limon"),
    ("copa_vino", "Copa de vino"),
    ("cubalibre", "Cubalibre"),
    ("donut", "Donut"),
    ("jarra_cerveza", "Jarra de cerveza"),
    ("llevar", "Icono para llevar"),
    ("magdalena", "Magdalena"),
    ("menu", "Menu"),
    ("pescado", "Pescado"),
    ("pincho", "Pincho"),
    ("pizza", "Pizza"),
    ("plato", "Plato humeante"),
    ("plato_combinado", "Plato combinado"),
    ("sopa", "Plato sopa"),
    ("sopa_cuchara", "Plato sopa con cuchara"),
    ("tarta", "Tarta"),
    ("taza_cafe", "Taza cafe"),
    ("aperitivo", "Plato doritos"),
)

class IconChoices(BaseModels):
    @staticmethod
    def update_for_devices():
        obj = []
        for r in ICON_CHOICES:
            obj.append({"choices":r[1], "keys":r[0]})
        return obj;

class SeccionesCom(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=11)  # Field name made lowercase.
    es_promocion = models.BooleanField(db_column='Es_promocion', blank=True, null=True, default="False")
    descuento = models.IntegerField(db_column='Descuento', blank=True, null=True,  default='0')
    icono = models.CharField(db_column='Icono', max_length=15, choices=ICON_CHOICES, default="bar")

    @staticmethod
    def update_for_devices():
        a = []
        for obj in SeccionesCom.objects.all():
            a.append(obj.serialize())
        return a

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
        if self.descuento and int(self.descuento) > 100:
            self.descuento = 100
        elif not self.descuento  or  int(self.descuento) < 0:
            self.descuento = 0

        super(SeccionesCom, self).save(*args, **kwargs)


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'secciones_com'

class Subteclas(BaseModels):
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.
    nombre = models.CharField(max_length=100, null=True, blank=True)
    incremento = models.DecimalField(blank=True, null=True, max_digits=4, decimal_places=2, default='0')
    descripcion_r = models.CharField("Descripción recepción", db_column='Descripcion_r', max_length=300, null=True, blank=True)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
    orden = models.IntegerField(db_column='Orden', default=0, blank=True)  # Field name made lowercase.
    

    @staticmethod
    def update_for_devices():
        a = []
        for sub in Subteclas.objects.all():
            a.append(sub.serialize())
        return a


    class Meta:
        db_table = 'subteclas'
        ordering = ['-orden']

class Sugerencias(models.Model):
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla', default=-1)  # Field name made lowercase.
    sugerencia = models.CharField(db_column='Sugerencia', max_length=300)  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        a = []
        for obj in Sugerencias.objects.all():
            a.append(obj.serialize())
        return a

    
    class Meta:
        db_table = 'sugerencias'

TIPO_TECLA_CHOICE = [
    ("SP", "SIMPLE"),
    ("CM", "COMPUESTA")
    ]

class Teclas(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=50)  # Field name made lowercase.
    p1 = models.DecimalField(db_column='P1', max_digits=6, decimal_places=2)  # Field name made lowercase.
    p2 = models.DecimalField(db_column='P2', max_digits=6, decimal_places=2)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0, blank=True)  # Field name made lowercase.
    familia = models.ForeignKey('Familias',  on_delete=models.CASCADE, db_column='IDFamilia')  # Field name made lowercase.
    tag = models.CharField(db_column='Tag', max_length=100, default='', blank=True)  # Field name made lowercase.
    ttf = models.CharField("TTF", db_column='TTF', max_length=50, default='', blank=True)  # Field name made lowercase.
    descripcion_r = models.CharField("Descripción recepción", db_column='Descripcion_r', max_length=300, null=True, blank=True)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
    tipo = models.CharField(max_length=2, choices=TIPO_TECLA_CHOICE, default="SP")


    @staticmethod
    def update_for_devices():
        rows = Teclas.objects.all()
        objs = []
        for r in rows: 
            objs.append(r.serialize())
        return objs

    def serialize(self):
        r = self
        teclasseccion = r.teclaseccion_set.all()
        row = super().serialize()
        row["p1"] = float(r.p1)
        row["p2"] = float(r.p2)
        row["Precio"] = float(r.p1) 
        row['RGB'] = teclasseccion[0].seccion.rgb if teclasseccion.count() > 0 else "207,182,212"
        row['IDSeccion'] = teclasseccion[0].seccion.id if teclasseccion.count() > 0 else -1
        row["IDSec2"] = teclasseccion[1].seccion.id if teclasseccion.count() > 1 else -1
        seccioncom = r.teclascom_set.first()
        row["IDSeccionCom"] = seccioncom.seccion.id if seccioncom else -1
        row["OrdenCom"] = seccioncom.orden if seccioncom else -1
        row["nombreFam"] = r.familia.nombre
        
        return row
   

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'teclas'
        ordering = ['-orden']

class Teclascom(BaseModels):
    tecla = models.ForeignKey(Teclas,  on_delete=models.SET_NULL, db_column='IDTecla', null=True)  # Field name made lowercase.
    seccion = models.ForeignKey(SeccionesCom,  on_delete=models.CASCADE, db_column='IDSeccion')  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase.
   

    class Meta:
        db_table = 'teclascom'
        ordering = ['-orden']

class Teclaseccion(models.Model):
    seccion = models.ForeignKey('Secciones',  on_delete=models.CASCADE, db_column='IDSeccion')  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.

    
    class Meta:
        db_table = 'teclaseccion'
