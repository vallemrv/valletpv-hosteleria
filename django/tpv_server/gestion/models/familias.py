from django.db import models
from .basemodels import BaseModels
import json

class Receptores(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=40)  # Field name made lowercase.
    nomimp = models.CharField(db_column='nomImp', max_length=40)  # Field name made lowercase.
    activo = models.BooleanField(db_column='Activo', default=True)  # Field name made lowercase.
    descripcion = models.CharField(db_column='Descripcion', max_length=200, default="")  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        rows = Receptores.objects.all()
        objs = []
        for r in rows:
            objs.append(r.serialize())
        return objs


    def __unicode__(self):
        return self.nombre +'  ' +self.descripcion

    def __str__(self):
        return self.nombre + ' ' +self.descripcion

    class Meta:
        db_table = 'receptores'

class Familias(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=40)  # Field name made lowercase.
    composicion = models.CharField(db_column='Tipo', max_length=150)  # Field name made lowercase.
    cantidad = models.IntegerField("Numero de tapas", db_column='NumTapas', default=0, null=True, blank=True)  # Field name made lowercase.
    receptor = models.ForeignKey('Receptores',  on_delete=models.CASCADE, db_column='IDReceptor')  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        rows = Familias.objects.all()
        objs = []
        for r in rows:
            objs.append(r.serialize())
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

class Secciones(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=50)  # Field name made lowercase.
    rgb = models.CharField("Color", db_column='RGB', max_length=11)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase.
    
    @staticmethod
    def update_for_devices():
        rows = Secciones.objects.all()
        objs = []
        for r in rows:
            objs.append(r.serialize())
        return objs


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'secciones'
        ordering = ['-orden']