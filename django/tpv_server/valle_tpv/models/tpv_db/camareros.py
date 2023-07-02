from django.db import models
from django.forms.models import model_to_dict
from valle_tpv.tools.ws import comunicar_cambios_devices


class Camareros(models.Model):
    id = models.AutoField(primary_key=True)
    nombre = models.CharField(max_length=100)
    apellidos = models.CharField(max_length=100)
    activo = models.BooleanField(default=False)
    autorizado = models.BooleanField(default=False)
    fecha_creacion = models.DateTimeField(auto_now_add=True)
    password = models.CharField(max_length=200, default="")
    permisos = models.CharField(max_length=200, blank=True)
    

    def __str__(self):  
        return self.nombre + " " + self.apellidos

    @staticmethod
    def update_from_device(row):
        
        id = row.get("ID", row.get("id"))
        
        if id is None:
            return 

        c = Camareros.objects.filter(id=id).first()
        
        if c:
            # Solo actualiza los campos si existen en el diccionario
            if "autorizado" in row:
                c.autorizado = int(row["autorizado"])
            if "activo" in row:
                c.activo = int(row["activo"])
            if "nombre" in row:
                c.nombre = row["nombre"]
            if "apellidos" in row:
                c.apellidos = row["apellidos"]
            if "permisos" in row:
                c.permisos = row["permisos"]    
            if "password" in row:
                c.password = row["password"]

            c.save()
            comunicar_cambios_devices("md", "camareros", c.serialize())
            


            
    @staticmethod
    def delete_handler(filter):
        camareros = Camareros.objects.filter(**filter)
        result = []
        for camarero in camareros:
            #Borrammos el usuario que se ha creado para el camarero
            result.append(camarero.pk)
            camarero.delete()
        
        comunicar_cambios_devices("rm", "camareros", result)
        return result
       

    @staticmethod
    def add_handler(reg):
        nombre = reg["nombre"]
        apellido = reg["apellidos"]
        permisos = reg["permisos"]
       
        try:
            c = Camareros()
            c.nombre = nombre
            c.apellidos = apellido
            c.activo = 1    
            c.autorizado = 1
            c.permisos = "".join(permisos, ",")   
            c.save()    

            serializer = c.serialize()
        
            comunicar_cambios_devices("md", "camareros", serializer)
        except:
            pass
        return serializer
        
    @property
    def permisos_list(self):
        return [("borrar", "Puede borrar mesas y lineas."),
                ("cobrar", "Puede abrir cajon y cobrar"),
                ("Modificar", "Puede crear y modificar productos"),]

    def serialize(self):
        data = model_to_dict(self)
        data["permisos"] = self.permisos.split(",")
        return data
    
    class Meta:
        ordering = ["apellidos"]        
        db_table = "camareros"
        
    

