#modelo para la los datos de una empresa, cif, nombre, etc
import django.db.models as models
from django.forms.models import model_to_dict
from django.conf import settings
import os

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
            file = empresa.logo
            if file:
                if os.path.isfile(os.path.join(settings.MEDIA_ROOT, file.name)):
                    os.remove(os.path.join(settings.MEDIA_ROOT, file.name))
            empresa.logo = data["logo"]
        if "logo_small" in data:
            file = empresa.logo_small
            if file:
                if os.path.isfile(os.path.join(settings.MEDIA_ROOT, file.name)):
                    os.remove(os.path.join(settings.MEDIA_ROOT, file.name))
            empresa.logo_small = data["logo_small"]

        if "email" in data:
            empresa.email = data["email"]
        if "iva" in data:
            empresa.iva = data["iva"]

        empresa.save()
        return empresa.serialize()

    def __str__(self):
        return self.nombre
    
    def __unicode__(self):
        return self.nombre
    
    class Meta:
        db_table = 'empresa'
        verbose_name_plural = "Empresas"
        ordering = ['-id']