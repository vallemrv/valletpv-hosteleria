# push_telegram/models.py
# Sistema de Push Notifications para Telegram

from django.db import models
from django.utils import timezone


class TelegramUser(models.Model):
    """
    Usuarios de Telegram registrados en el sistema.
    Permite buscar por nombre en lugar de recordar IDs numéricos.
    """
    nombre = models.CharField(
        max_length=100,
        unique=True,
        help_text="Nombre del usuario (ej: Valle, Admin, Camarero1)"
    )
    telegram_user_id = models.BigIntegerField(
        unique=True,
        help_text="ID de usuario de Telegram (número)"
    )
    descripcion = models.CharField(
        max_length=200,
        blank=True,
        help_text="Descripción o rol del usuario (opcional)"
    )
    activo = models.BooleanField(
        default=True,
        help_text="¿Está activo este usuario?"
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'telegram_users'
        verbose_name = "Usuario de Telegram"
        verbose_name_plural = "Usuarios de Telegram"
        ordering = ['nombre']

    def __str__(self):
        return f"{self.nombre} ({self.telegram_user_id})"


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
    Suscripciones: qué usuario de Telegram recibe qué eventos
    """
    usuario = models.ForeignKey(
        TelegramUser,
        on_delete=models.CASCADE,
        related_name='subscriptions',
        help_text="Usuario de Telegram suscrito"
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
    filtros = models.JSONField(
        default=dict,
        blank=True,
        help_text="Filtros adicionales para la suscripción (ej: {'zonas': [1, 2, 3]}, {'camareros': [1, 5]}, etc.)"
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'telegram_subscriptions'
        verbose_name = "Suscripción Push"
        verbose_name_plural = "Suscripciones Push"
        unique_together = ['usuario', 'event_type']

    def __str__(self):
        filtros_str = f" (filtros: {self.filtros})" if self.filtros else ""
        return f"{self.usuario.nombre} -> {self.event_type.code}{filtros_str}"
    
    def match_filtros(self, **kwargs):
        """
        Verifica si los datos del evento coinciden con los filtros de la suscripción.
        Ejemplo: match_filtros(zona_id=5) devuelve True si 5 está en filtros['zonas']
        """
        if not self.filtros:
            return True  # Sin filtros = recibe todos los eventos de este tipo
        
        # Filtro por zonas
        if 'zonas' in self.filtros and 'zona_id' in kwargs:
            zonas_vigiladas = self.filtros['zonas']
            if isinstance(zonas_vigiladas, list):
                return kwargs['zona_id'] in zonas_vigiladas
            elif isinstance(zonas_vigiladas, int):
                return kwargs['zona_id'] == zonas_vigiladas
        
        return True  # Si no hay filtros aplicables, permitir


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

