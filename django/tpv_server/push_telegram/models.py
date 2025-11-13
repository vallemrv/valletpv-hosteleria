# push_telegram/models.py
# Sistema de Push Notifications para Telegram

from django.db import models


class TelegramEventType(models.Model):
    """
    Tipos de eventos/hooks disponibles para notificaciones
    Ejemplo: nuevo_dispositivo, pedido_completado, error_sistema, etc.
    """
    code = models.CharField(
        max_length=50,
        unique=True,
        help_text="Código único del evento (ej: nuevo_dispositivo)"
    )
    nombre = models.CharField(
        max_length=100,
        help_text="Nombre descriptivo del evento"
    )
    descripcion = models.TextField(
        blank=True,
        help_text="Descripción del evento"
    )
    activo = models.BooleanField(
        default=True,
        help_text="¿Está activo este evento?"
    )
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'telegram_event_types'
        verbose_name = "Tipo de Evento Push"
        verbose_name_plural = "Tipos de Eventos Push"

    def __str__(self):
        return f"{self.code} - {self.nombre}"


class TelegramSubscription(models.Model):
    """
    Suscripciones: qué ID de Telegram recibe qué eventos
    Se gestiona manualmente desde el admin de Django (por ahora)
    """
    telegram_user_id = models.BigIntegerField(
        help_text="ID de usuario de Telegram (número)"
    )
    nombre_usuario = models.CharField(
        max_length=100,
        blank=True,
        help_text="Nombre del usuario (para referencia)"
    )
    event_type = models.ForeignKey(
        TelegramEventType,
        on_delete=models.CASCADE,
        related_name='subscriptions',
        help_text="Evento al que está suscrito"
    )
    activo = models.BooleanField(
        default=True,
        help_text="¿Está activa esta suscripción?"
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'telegram_subscriptions'
        verbose_name = "Suscripción Push"
        verbose_name_plural = "Suscripciones Push"
        unique_together = ['telegram_user_id', 'event_type']

    def __str__(self):
        return f"{self.telegram_user_id} -> {self.event_type.code}"


class TelegramNotificationLog(models.Model):
    """
    Log de notificaciones enviadas
    """
    event_type = models.ForeignKey(
        TelegramEventType,
        on_delete=models.CASCADE,
        help_text="Tipo de evento"
    )
    telegram_user_id = models.BigIntegerField(
        help_text="ID de usuario que recibió la notificación"
    )
    mensaje = models.TextField(
        help_text="Contenido del mensaje enviado"
    )
    enviado = models.BooleanField(
        default=False,
        help_text="¿Se envió correctamente?"
    )
    error = models.TextField(
        blank=True,
        null=True,
        help_text="Mensaje de error si falló"
    )
    metadata = models.JSONField(
        default=dict,
        blank=True,
        help_text="Datos adicionales del evento"
    )
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'telegram_notification_logs'
        verbose_name = "Log de Notificación"
        verbose_name_plural = "Logs de Notificaciones"
        ordering = ['-created_at']

    def __str__(self):
        status = "✅" if self.enviado else "❌"
        return f"{status} {self.event_type.code} -> {self.telegram_user_id}"


class TelegramAutorizacion(models.Model):
    """
    Autorizaciones temporales para acceder a APIs protegidas
    """
    token = models.CharField(
        max_length=100,
        unique=True,
        help_text="Token único de autorización (UUID)"
    )
    uid_dispositivo = models.CharField(
        max_length=255,
        help_text="UID del dispositivo asociado"
    )
    telegram_message_id = models.BigIntegerField(
        help_text="ID del mensaje de Telegram que generó esta autorización"
    )
    telegram_user_id = models.BigIntegerField(
        help_text="ID del usuario de Telegram que autorizó"
    )
    accion = models.CharField(
        max_length=50,
        default='activate_device',
        help_text="Acción autorizada (ej: activate_device, delete_device)"
    )
    empresa = models.CharField(
        max_length=100,
        default='testTPV',
        help_text="Identificador de la empresa/instancia TPV"
    )
    usada = models.BooleanField(
        default=False,
        help_text="¿Se ha usado esta autorización?"
    )
    expirada = models.BooleanField(
        default=False,
        help_text="¿Ha expirado esta autorización?"
    )
    expira_en = models.DateTimeField(
        help_text="Fecha y hora de expiración"
    )
    usada_en = models.DateTimeField(
        null=True,
        blank=True,
        help_text="Fecha y hora en que se usó"
    )
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'telegram_autorizaciones'
        verbose_name = "Autorización Temporal"
        verbose_name_plural = "Autorizaciones Temporales"
        ordering = ['-created_at']

    def __str__(self):
        status = "✅ Usada" if self.usada else ("⏰ Expirada" if self.expirada else "🔓 Activa")
        return f"{status} - {self.accion} - {self.uid_dispositivo[:8]}..."

    def is_valida(self):
        """Verificar si la autorización es válida"""
        from django.utils import timezone
        if self.usada:
            return False
        if self.expirada:
            return False
        if timezone.now() > self.expira_en:
            self.expirada = True
            self.save(update_fields=['expirada'])
            return False
        return True