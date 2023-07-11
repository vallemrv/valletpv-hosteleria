from django.db import models
from django.forms.models import model_to_dict

class Mesas(models.Model):
    id = models.AutoField( primary_key=True) 
    nombre = models.CharField( max_length=50) 
    orden = models.IntegerField( default=0) 
    zona = models.ForeignKey('Zonas',  on_delete=models.CASCADE) 


    def serialize(self):
        from .mesasabiertas import Mesasabiertas
        obj = {
        'id': self.id,
        'nombre': self.nombre,
        'orden': self.orden,
        'num': 0,
        'abierta': 0,
        'color': self.zona.color, 
        'zona_id': self.zona.id,
        "tarifa": self.zona.tarifa, 
        }

        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=self.id).first()
        if mesa_abierta:
            obj["num"] = mesa_abierta.infmesa.numcopias
            obj["abierta"] = 1;
        return obj


    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        default_permissions = ()
        ordering = ['-orden']


class Zonas(models.Model):
    id = models.AutoField( primary_key=True) 
    nombre = models.CharField( unique=True, max_length=50) 
    tarifa = models.IntegerField() 
    color = models.CharField("Color", max_length=50) 
    orden = models.IntegerField( default=0) 

        
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        db_table = 'zonas'
        ordering = ['orden']

