from django.db import models
from django.utils import timezone


class TokenCallbackMapping(models.Model):
    """
    Mapeo de tokens temporales a URLs de callback.
    Los TPVs registran aquí sus notificaciones con el token y callback_url,
    permitiendo al webhook enrutar directamente sin probar múltiples servidores.
    """
    token = models.CharField(
        max_length=100,
        unique=True,
        primary_key=True,
        help_text="Token único de la notificación"
    )
    callback_url = models.URLField(
        help_text="URL completa del endpoint de callback en el TPV"
    )
    empresa = models.CharField(
        max_length=100,
        help_text="Identificador de la empresa/instancia TPV"
    )
    accion = models.CharField(
        max_length=50,
        help_text="Acción que ejecutará el callback (activate_device, etc)"
    )
    telegram_user_id = models.BigIntegerField(
        help_text="ID del usuario de Telegram"
    )
    telegram_message_id = models.BigIntegerField(
        null=True,
        blank=True,
        help_text="ID del mensaje enviado a Telegram"
    )
    uid_dispositivo = models.CharField(
        max_length=255,
        blank=True,
        help_text="UID del dispositivo (si aplica)"
    )
    metadata = models.JSONField(
        default=dict,
        blank=True,
        help_text="Datos adicionales de contexto"
    )
    expira_en = models.DateTimeField(
        help_text="Fecha y hora de expiración del token"
    )
    created_at = models.DateTimeField(auto_now_add=True)
    usada = models.BooleanField(
        default=False,
        help_text="¿Se ejecutó el callback?"
    )
    usada_en = models.DateTimeField(
        null=True,
        blank=True,
        help_text="Cuándo se ejecutó el callback"
    )

    class Meta:
        db_table = 'webhook_token_callbacks'
        verbose_name = "Mapeo Token-Callback"
        verbose_name_plural = "Mapeos Token-Callback"
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['expira_en', 'usada']),
            models.Index(fields=['empresa', 'created_at']),
        ]

    def __str__(self):
        status = "✅ Usada" if self.usada else "🔓 Activa"
        return f"{status} - {self.accion} - {self.empresa}"

    def is_valida(self):
        """Verifica si el token aún es válido"""
        if self.usada:
            return False
        if timezone.now() > self.expira_en:
            return False
        return True

    def marcar_usada(self):
        """Marca el token como usado"""
        self.usada = True
        self.usada_en = timezone.now()
        self.save(update_fields=['usada', 'usada_en'])
