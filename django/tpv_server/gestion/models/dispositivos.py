from django.db import models
from .basemodels import BaseModels
import asyncio
import logging
from django.conf import settings
from gestion.tools.config_logs import configurar_logging

logger = configurar_logging('dispositivos')

class Dispositivo(BaseModels):
    uid = models.CharField(max_length=255, unique=True)  # UID único para el dispositivo
    descripcion = models.CharField(max_length=255, blank=True, null=True)  # Descripción del dispositivo
    activo = models.BooleanField(default=False)  # Estado activo/inactivo del dispositivo (por defecto inactivo)
    created_at = models.DateTimeField(auto_now_add=True)  # Fecha de creación (opcional)
    
    class Meta:
        db_table = 'dispositivos'  # Nombre de la tabla en la base de datos

    def __str__(self):
        return f"Dispositivo {self.uid} - {'Activo' if self.activo else 'Inactivo'}"
    
    def save(self, *args, **kwargs):
        """
        Override del método save para enviar notificación cuando se crea un nuevo dispositivo
        """
        # Verificar si es un nuevo dispositivo (no tiene pk)
        is_new = self.pk is None
        
        # Guardar primero el dispositivo
        super().save(*args, **kwargs)
        
        # Si es nuevo y no está activo, enviar notificación push
        if is_new and not self.activo:
            try:
                from push_telegram.push_sender import notificar_nuevo_dispositivo
                notificar_nuevo_dispositivo(self.uid, self.descripcion)
                logger.info(f"Push enviado para nuevo dispositivo: {self.uid}")
            except Exception as e:
                logger.error(f"Error enviando push de nuevo dispositivo: {e}")
                # No lanzar excepción para no interrumpir la creación del dispositivo
