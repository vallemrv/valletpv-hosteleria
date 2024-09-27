from django.db import models
from .basemodels import BaseModels

class Dispositivo(BaseModels):
    uid = models.CharField(max_length=255, unique=True)  # UID único para el dispositivo
    descripcion = models.CharField(max_length=255, blank=True, null=True)  # Descripción del dispositivo
    activo = models.BooleanField(default=True)  # Estado activo/inactivo del dispositivo
    created_at = models.DateTimeField(auto_now_add=True)  # Fecha de creación (opcional)
    
    class Meta:
        db_table = 'dispositivos'  # Nombre de la tabla en la base de datos

    def __str__(self):
        return f"Dispositivo {self.uid} - {'Activo' if self.activo else 'Inactivo'}"
