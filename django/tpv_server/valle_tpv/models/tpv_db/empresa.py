#modelo para la los datos de una empresa, cif, nombre, etc
import django.db.models as models
from django.forms.models import model_to_dict
from uuid import uuid4
import random


class Dispositivos(models.Model):
    nombre = models.CharField(max_length=100, default="")
    UID = models.CharField(max_length=100, default="")
    codigo = models.CharField(max_length=6, default="")
    descripcion = models.CharField(max_length=100, default="")
    puede_enviar = models.BooleanField(default=True)
   
    def serialize(self):
        return {
            "nombre": self.nombre,
            "UID": self.UID,
            "codigo": self.codigo,
            "descripcion": self.descripcion,
            "puede_enviar": self.puede_enviar,
        }

    def __str__(self):
        return self.nombre
    
    def __unicode__(self):
        return self.nombre
    
    @staticmethod
    def add_handler():
        dispositivo = Dispositivos()
        #crear un codigo de UID unico para cada dispositivo
        dispositivo.UID = uuid4().hex
        
       
        #buscar un codigo de 6 digitos que no exista
        codigo = ""
        while True:
            codigo = str(random.randint(100000, 999999))
            if not Dispositivos.objects.filter(codigo=codigo).exists():
                break
        dispositivo.codigo = codigo
        dispositivo.save()
        return dispositivo.serialize()

    
    class Meta:
        db_table = 'dispositivos'
        ordering = ['-id']

class Empresa(models.Model):
    nombre = models.CharField(max_length=100, default="")
    razonsocial = models.CharField(max_length=100, default="")
    cif = models.CharField(max_length=20,  default="")
    direccion = models.CharField(max_length=100,    default="")
    cp = models.CharField(max_length=10,    default="")
    localidad = models.CharField(max_length=100,    default="")
    provincia = models.CharField(max_length=100,   default="")
    telefono = models.CharField(max_length=20,  default="")
    logo = models.ImageField(upload_to="logos/", null=True, blank=True)
    logo_small = models.ImageField(upload_to="logos/", null=True, blank=True)
    email = models.CharField(max_length=100)
    iva = models.FloatField(default=10)
   

    def serialize(self):
        data = model_to_dict(self)
       
        data["logo"] = {
            'name': self.logo.name.replace("logos/","") if self.logo else "",
            'url': self.logo.url if self.logo else "",
            'size': self.logo.size if self.logo else 0,
        }

        data["logo_small"] = {
            'name': self.logo_small.name.replace("logos/","") if self.logo_small else "",
            'url': self.logo_small.url if self.logo_small else "",
            'size': self.logo_small.size if self.logo_small else 0,
        }
        return data

    @staticmethod
    def add_handler(data):
        Empresa.modifcar_handler(data)


    @staticmethod
    def modifcar_handler(data, filter=None):
        empresa = Empresa.objects.first()
        if not empresa:
            empresa = Empresa()
    
        if "nombre" in data:
            empresa.nombre = data["nombre"]
        if "razonsocial" in data:
            empresa.razonsocial = data["razonsocial"]
        if "cif" in data:
            empresa.cif = data["cif"]
        if "direccion" in data:
            empresa.direccion = data["direccion"]
        if "cp" in data:
            empresa.cp = data["cp"]
        if "localidad" in data:
            empresa.localidad = data["localidad"]
        if "provincia" in data:
            empresa.provincia = data["provincia"]
        if "telefono" in data:
            empresa.telefono = data["telefono"]
        if "logo" in data:
            empresa.logo = data["logo"]
        if "logo_small" in data:
            empresa.logo_small = data["logo_small"]
        if "email" in data:
            empresa.email = data["email"]
        if "iva" in data:
            empresa.iva = data["iva"]

        empresa.save()
        return empresa.serialize()
    

    def save(self, *args, **kwargs):
        try:
            # Obtener el objeto Secciones desde la base de datos
            obj = Empresa.objects.get(id=self.id)
            # Verificar si el campo 'icono' ha cambiado
            if obj.logo != self.logo:
                # Si ha cambiado, eliminar el archivo antiguo
                obj.logo.delete(save=False)
            if obj.logo_small != self.logo_small:
                # Si ha cambiado, eliminar el archivo antiguo
                obj.logo_small.delete(save=False)
        except Empresa.DoesNotExist:
            # Si es una nueva instancia, simplemente continuar
            pass
        super(Empresa, self).save(*args, **kwargs)

    def __str__(self):
        return self.nombre
    
    def __unicode__(self):
        return self.nombre
    
    class Meta:
        db_table = 'empresa'
        verbose_name_plural = "Empresas"
        ordering = ['-id']