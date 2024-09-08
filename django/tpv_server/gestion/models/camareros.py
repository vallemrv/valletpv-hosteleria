from django.db import models
from comunicacion.tools import comunicar_cambios_devices
from .basemodels import BaseModels

PERMISOS_CHOICES = ["imprimir_factura", 
"abrir_cajon", 
"cobrar_ticket", 
"borrar_linea", 
"borrar_mesa"]

class PermisosChoices(BaseModels):
    @staticmethod
    def update_for_devices():
        obj = []
        for r in PERMISOS_CHOICES:
            obj.append({"choices":r})
        return obj;        

class Camareros(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=100)  # Field name made lowercase.
    apellidos = models.CharField(db_column='Apellidos', max_length=100)  # Field name made lowercase.
    email = models.CharField(db_column='Email', max_length=50, null=True)  # Field name made lowercase.
    pass_field = models.CharField(db_column='Pass', max_length=100, null=True)  # Field name made lowercase. Field renamed because it was a Python reserved word.
    activo = models.IntegerField(db_column='Activo', default=1)  # Field name made lowercase.
    autorizado = models.IntegerField(db_column='Autorizado', default=1)  # Field name made lowercase.
    permisos = models.CharField(db_column='Permisos', max_length=150, null=True, default="")  # Field name made lowercase. Field renamed because it was a Python reserved word.
    
    @staticmethod
    def update_from_device(row):
        c = Camareros.objects.filter(id=row["ID"]).first()
        if c:
            c.autorizado = int(row["autorizado"])
            c.pass_field = row["pass_field"] if "pass_field" in row else row["Pass"]
            c.permisos = row["permisos"]
            c.save()
            comunicar_cambios_devices("md", "camareros", c.serialize())
       

    @staticmethod
    def update_for_devices():
        rows = Camareros.objects.all()
        tb = []
        for r in rows:
            tb.append(r.serialize())
        return tb

    class Meta:
        db_table = 'camareros'
        ordering = ["apellidos"]