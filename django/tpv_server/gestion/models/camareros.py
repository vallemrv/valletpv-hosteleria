from django.db import models
from comunicacion.tools import comunicar_cambios_devices
from .basemodels import BaseModels

PERMISOS_CHOICES = ["imprimir_factura", 
"abrir_cajon", 
"cobrar_ticket", 
"borrar_linea", 
"borrar_mesa",
"comandos_voz",]

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
    
    def serialize(self):
        obj = super().serialize()
        return obj
    
    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
         # ¡Línea clave! Refrescar el objeto desde la BBDD
        self.refresh_from_db()
        
        # Comunicar cambios a devices
        if is_new:
            comunicar_cambios_devices("insert", "camareros", self.serialize())
        else:
            comunicar_cambios_devices("md", "camareros", self.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener ID antes de borrar
        camarero_id = self.id
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "camareros", {"id": camarero_id})
    


    class Meta:
        db_table = 'camareros'
        ordering = ["apellidos"]