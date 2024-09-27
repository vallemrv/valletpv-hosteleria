from django.db import models
from .mesasabiertas import  Mesasabiertas
from .basemodels import BaseModels

class Mesas(BaseModels):
    nombre = models.CharField(db_column='Nombre',  max_length=50)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        return Mesas.get_all_for_devices()
    
    
    @staticmethod
    def get_all_for_devices():
        lsMesas = []
        mesas = Mesas.objects.all()
        for m in mesas:
            lsMesas.append(m.serialize())
        return lsMesas

    def serialize(self):
        mz = self.mesaszona_set.first()
        obj = {
        'ID': self.id,
        'Nombre': self.nombre,
        'Orden': self.orden,
        'num': 0,
        'abierta': 0,
        'RGB': mz.zona.rgb if mz else "207,182,212",
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
        db_table = 'mesas'
        ordering = ['-orden']

class Zonas(BaseModels):
    nombre = models.CharField(db_column='Nombre', unique=True, max_length=50)  # Field name made lowercase.
    tarifa = models.IntegerField(db_column='Tarifa')  # Field name made lowercase.
    rgb = models.CharField("Color", db_column='RGB', max_length=50)  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        zonas = Zonas.objects.all()
        objs = []
        for z in zonas:
            objs.append(z.serialize())
        return  objs
        
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        db_table = 'zonas'

class Mesaszona(BaseModels):
    zona = models.ForeignKey('Zonas',  on_delete=models.CASCADE, db_column='IDZona')  # Field name made lowercase.
    mesa = models.ForeignKey('Mesas',  on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.

    def serialize(self):
        return self.mesa.serialize()

    class Meta:
        db_table = 'mesaszona'
