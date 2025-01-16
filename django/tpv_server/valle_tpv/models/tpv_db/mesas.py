from django.db import models
from django.forms.models import model_to_dict

class Mesas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase
    nombre = models.CharField(db_column='Nombre',  max_length=50)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase. 
    
    def serialize(self):
        from .mesasabiertas import Mesasabiertas
        obj = {
        'id': self.id,
        'nombre': self.nombre,
        'orden': self.orden,
        'num': 0,
        'abierta': False,
        'color': self.zona.color, 
        'zonaId': self.zona.id,
        "tarifa": self.zona.tarifa, 
        }

        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=self.id).first()
        if mesa_abierta:
            obj["num"] = mesa_abierta.infmesa.numcopias
            obj["abierta"] = True
        
        return obj


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        db_table = 'mesas'
        ordering = ['-orden']


class Zonas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase 
    nombre = models.CharField(db_column='Nombre', unique=True, max_length=50)  # Field name made lowercase.
    tarifa = models.IntegerField(db_column='Tarifa')  # Field name made lowercase.
    rgb = models.CharField("Color", db_column='RGB', max_length=50)  # Field name made lowercase.

        
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        db_table = 'zonas'
        ordering = ['orden']

class Mesaszona(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase 
    zona = models.ForeignKey('Zonas',  on_delete=models.CASCADE, db_column='IDZona')  # Field name made lowercase.
    mesa = models.ForeignKey('Mesas',  on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.

    def serialize(self):
        return self.mesa.serialize()

    class Meta:
        db_table = 'mesaszona'
