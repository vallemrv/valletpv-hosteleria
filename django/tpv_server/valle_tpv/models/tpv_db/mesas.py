from django.db import models
from django.forms.models import model_to_dict

class Mesas(models.Model):
    id = models.AutoField( primary_key=True)  # Field name made lowercase.
    nombre = models.CharField( max_length=50)  # Field name made lowercase.
    orden = models.IntegerField( default=0)  # Field name made lowercase.
    zona = models.ForeignKey('Zonas',  on_delete=models.CASCADE)  # Field name made lowercase.


    def serialize(self):
        from .mesasabiertas import Mesasabiertas
        mz = self.mesaszona_set.first()
        obj = {
        'id': self.id,
        'nombre': self.nombre,
        'orden': self.orden,
        'num': 0,
        'abierta': 0,
        'color': mz.zona.rgb if mz else "207,182,212",
        'IDZona': mz.zona.id if mz else -1,
        "Tarifa": mz.zona.tarifa if mz else 1,
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
    id = models.AutoField( primary_key=True)  # Field name made lowercase.
    nombre = models.CharField( unique=True, max_length=50)  # Field name made lowercase.
    tarifa = models.IntegerField()  # Field name made lowercase.
    color = models.CharField("Color", max_length=50)  # Field name made lowercase.
    orden = models.IntegerField( default=0)  # Field name made lowercase.

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


    class Meta:
        db_table = 'zonas'
        ordering = ['orden']

