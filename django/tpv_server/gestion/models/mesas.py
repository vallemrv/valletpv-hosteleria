from django.db import models
from .mesasabiertas import  Mesasabiertas
from .basemodels import BaseModels
from django.db.models import Exists
from django.db.models import Subquery, OuterRef, IntegerField
from comunicacion.tools import comunicar_cambios_devices
             

class Mesas(BaseModels):
    nombre = models.CharField(db_column='Nombre',  max_length=50)  # Field name made lowercase.
    orden = models.IntegerField(db_column='Orden', default=0)  # Field name made lowercase.

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None

        # Realizar acciones específicas para nuevas inserciones
        if is_new:
            pass

        # Guardar el objeto
        super().save(*args, **kwargs)

        self.arefresh_from_db = True  # Forzar refresco desde DB después de guardar
        
        
        # Comunicar cambios a devices
        if is_new:
            comunicar_cambios_devices("insert", "mesas", self.serialize())
        else:
            comunicar_cambios_devices("md", "mesas", self.serialize())
    
    def delete(self, *args, **kwargs):
        
        # Obtener ID antes de borrar
        mesa_id = self.id
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "mesas", {"id": mesa_id})


    def serialize(self):
        mz = self.mesaszona_set.first()
        obj = {
            'ID': self.id, 'Nombre': self.nombre, 
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
            obj["abierta"] = 1
            if obj["num"] > 0:
                obj["RGB"] = "255,0,0"
                
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

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        self.arefresh_from_db = True  # Forzar refresco desde DB después de guardar
        
        # Comunicar cambios a devices
        zona_data = {
            'id': self.id,
            'nombre': self.nombre,
            'tarifa': self.tarifa,
            'rgb': self.rgb
        }
        
        if is_new:
            comunicar_cambios_devices("insert", "zonas", zona_data)
        else:
            comunicar_cambios_devices("md", "zonas", zona_data)
            # También notificar cambios en todas las mesas de esta zona
            for mesaszona in self.mesaszona_set.all():
                comunicar_cambios_devices("md", "mesas", mesaszona.mesa.serialize())
    
    def delete(self, *args, **kwargs):
        
        # Obtener ID y mesas asociadas antes de borrar
        zona_id = self.id
        mesas_asociadas = list(self.mesaszona_set.all())
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "zonas", {"id": zona_id})
        
        # Notificar cambios en las mesas que estaban asociadas
        for mesaszona in mesas_asociadas:
            comunicar_cambios_devices("md", "mesas", mesaszona.mesa.serialize())

            
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre


    class Meta:
        db_table = 'zonas'

class Mesaszona(BaseModels):
    zona = models.ForeignKey('Zonas',  on_delete=models.CASCADE, db_column='IDZona' )  
    mesa = models.ForeignKey('Mesas',  on_delete=models.CASCADE, db_column='IDMesa') 

    def save(self, *args, **kwargs):
        
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        self.arefresh_from_db = True  # Forzar refresco desde DB después de guardar
        
        # Comunicar cambios a devices - notificar cambios en las mesas
        if is_new:
            comunicar_cambios_devices("insert", "mesas", self.mesa.serialize())
        else:
            comunicar_cambios_devices("md", "mesas", self.mesa.serialize())
    
    def delete(self, *args, **kwargs):
        
        # Obtener datos antes de borrar
        mesa_serializada = self.mesa.serialize()
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("md", "mesas", mesa_serializada)

    def serialize(self):
        return self.mesa.serialize()

    class Meta:
        db_table = 'mesaszona'
       
        