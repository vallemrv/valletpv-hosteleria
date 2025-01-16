from django.db import models
from django.forms.models import model_to_dict
from valle_tpv.tools.ws import comunicar_cambios_devices


class Camareros(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase
    nombre = models.CharField(db_column='Nombre', max_length=100)
    apellidos = models.CharField(db_column='Apellidos', max_length=100)
    email = models.CharField(db_column='Email', max_length=50, null=True) 
    activo = models.IntegerField(db_column='Activo', default=1)
    autorizado = models.IntegerField(db_column='Autorizado', default=1)
    fecha_creacion = models.DateTimeField(auto_now_add=True)
    password = models.CharField(db_column='Pass', max_length=200, default="")
    permisos = models.CharField(db_column='Permisos', max_length=200, blank=True)
    
    def __str__(self):  
        return self.nombre + " " + self.apellidos

            
    @staticmethod
    def delete_handler(filter):
        camareros = Camareros.objects.filter(**filter)
        result = []
        for camarero in camareros:
            result.append(camarero.pk)
            camarero.delete()
        
        comunicar_cambios_devices("delete", "camareros", result)
        return result
       

    @staticmethod
    def add_handler(reg):
        nombre = reg["nombre"]
        apellido = reg["apellidos"]
        permisos = reg["permisos"]
      
    
        c = Camareros()
        c.nombre = nombre
        c.apellidos = apellido
        c.activo = True    
        c.autorizado = True
        c.permisos = permisos   
        c.save()    

        serializer = c.serialize()
    
        comunicar_cambios_devices("create", "camareros", [serializer])
       
        return serializer
        
    @staticmethod
    def permisos_list():
        return [("borrar", "Puede borrar mesas y lineas."),
                ("cobrar", "Puede abrir cajon y cobrar"),
                ("Modificar", "Puede crear y modificar productos"),]

    def serialize(self):
        data = model_to_dict(self)
        data["permisos"] = self.permisos
        return data
    
    class Meta:
        ordering = ["apellidos"]        
        db_table = "camareros"
        
    

