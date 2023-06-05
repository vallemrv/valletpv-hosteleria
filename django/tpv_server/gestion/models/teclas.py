from django.db import models
from django.forms.models import model_to_dict

import json


class Receptores(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=40)  # Field name made lowercase.
    nomimp = models.CharField(db_column='nomImp', max_length=40)  # Field name made lowercase.
    activo = models.BooleanField(db_column='Activo', default=True)  # Field name made lowercase.
    descripcion = models.CharField(db_column='Descripcion', max_length=200, default="")  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        rows = Receptores.objects.all()
        objs = []
        for r in rows:
            objs.append(model_to_dict(r))
        return objs

    def __unicode__(self):
        return self.nombre +'  ' +self.descripcion

    def __str__(self):
        return self.nombre + ' ' +self.descripcion

    class Meta:
        db_table = 'receptores'


class Familias(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=40)  # Field name made lowercase.
    composicion = models.CharField(db_column='Tipo', max_length=150)  # Field name made lowercase.
    cantidad = models.IntegerField("Numero de tapas", db_column='NumTapas', default=0, null=True, blank=True)  # Field name made lowercase.
    receptor = models.ForeignKey('Receptores',  on_delete=models.CASCADE, db_column='IDReceptor')  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        rows = Familias.objects.all()
        objs = []
        for r in rows:
            objs.append(model_to_dict(r))
        return objs


    def compToList(self):
        try:
            return json.loads(self.composicion.replace("'", '"'))
        except:
            return []


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'familias'
        ordering = ['-id']


class Secciones(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=50)  # Field name made lowercase.
    rgb = models.CharField("Color", db_column='RGB', max_length=11)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase.
    
    @staticmethod
    def update_for_devices():
        rows = Secciones.objects.all()
        objs = []
        for r in rows:
            objs.append(model_to_dict(r))
        return objs

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'secciones'
        ordering = ['-orden']

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

class IconChoices(models.Model):
    @staticmethod
    def update_for_devices():
        obj = []
        for r in ICON_CHOICES:
            obj.append({"choices":r[1], "keys":r[0]})
        return obj;

class SeccionesCom(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(db_column='Nombre', max_length=11)  # Field name made lowercase.
    es_promocion = models.BooleanField(db_column='Es_promocion', blank=True, null=True, default="False")
    descuento = models.IntegerField(db_column='Descuento', blank=True, null=True,  default='0')
    icono = models.CharField(db_column='Icono', max_length=15, choices=ICON_CHOICES, default="bar")

    @staticmethod
    def update_for_devices():
        a = []
        for obj in SeccionesCom.objects.all():
            a.append(model_to_dict(obj))
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

class Subteclas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
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

    def serialize(self):
        obj = model_to_dict(self)
        obj["incremento"] = float(self.incremento)
        return obj

  

    class Meta:
        db_table = 'subteclas'
        ordering = ['-orden']

class Sugerencias(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla', default=-1)  # Field name made lowercase.
    sugerencia = models.CharField(db_column='Sugerencia', max_length=300)  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        a = []
        for obj in Sugerencias.objects.all():
            a.append(model_to_dict(obj))
        return a

    class Meta:
        db_table = 'sugerencias'

TIPO_TECLA_CHOICE = [
    ("SP", "SIMPLE"),
    ("CM", "COMPUESTA")
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
        objs = []
        for r in rows: 
            objs.append(r.serialize())
        return objs

    def serialize(self):
        r = self
        teclasseccion = r.teclaseccion_set.all()
        row = model_to_dict(r)
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

class ComposicionTeclas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.
    composicion = models.CharField(max_length=300)  # Field name made lowercase.
    cantidad = models.IntegerField()  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        rows = ComposicionTeclas.objects.all()
        objs = []
        for r in rows: 
            objs.append(r.serialize())
        return objs


    def compToList(self):
        try:
            return json.loads(self.composicion.replace("'", '"'))
        except:
            return []

    def serialize(self):
        r = self
        aux = model_to_dict(r)
        aux["nombre"] = r.tecla.nombre
        return aux

    
    class Meta:
        db_table = 'composicionteclas'
        ordering = ['id']

class LineasCompuestas(models.Model):
    id = models.AutoField(primary_key=True)
    linea_principal = models.ForeignKey('Lineaspedido', models.CASCADE)
    linea_compuesta = models.IntegerField()
    composicion = models.ForeignKey(ComposicionTeclas, models.CASCADE)

class Teclascom(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.SET_NULL, db_column='IDTecla', null=True)  # Field name made lowercase.
    seccion = models.ForeignKey(SeccionesCom,  on_delete=models.CASCADE, db_column='IDSeccion')  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase.
   

    class Meta:
        db_table = 'teclascom'
        ordering = ['-orden']

class Teclaseccion(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    seccion = models.ForeignKey('Secciones',  on_delete=models.CASCADE, db_column='IDSeccion')  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.

    class Meta:
        db_table = 'teclaseccion'