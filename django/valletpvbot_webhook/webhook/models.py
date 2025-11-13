from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
import uuid


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
        help_text="Acción autorizada"
    )
    empresa = models.CharField(
        max_length=100,
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
        db_table = 'webhook_telegram_autorizaciones'
        verbose_name = "Autorización Temporal"
        verbose_name_plural = "Autorizaciones Temporales"
        ordering = ['-created_at']

    def __str__(self):
        status = "✅ Usada" if self.usada else ("⏰ Expirada" if self.expirada else "🔓 Activa")
        return f"{status} - {self.accion} - {self.uid_dispositivo[:8]}... ({self.empresa})"

    def is_valida(self):
        """Verificar si la autorización es válida"""
        if self.usada:
            return False
        if self.expirada:
            return False
        if timezone.now() > self.expira_en:
            self.expirada = True
            self.save(update_fields=['expirada'])
            return False
        return True


class TPVInstance(models.Model):
    """
    Representa una instancia de servidor TPV conectado al webhook.
    Cada TPV se registra con un API key único y puede servir a múltiples usuarios.
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=200, help_text="Nombre identificativo del TPV")
    api_key = models.CharField(max_length=255, unique=True, help_text="Clave API para autenticación")
    endpoint_url = models.URLField(help_text="URL del servidor TPV para enviar instrucciones")
    
    # Configuración
    is_active = models.BooleanField(default=True)
    max_users = models.IntegerField(default=100, help_text="Máximo de usuarios que puede manejar")
    
    # Metadatos
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    last_heartbeat = models.DateTimeField(null=True, blank=True, help_text="Última conexión del TPV")
    
    # Información adicional
    version = models.CharField(max_length=50, blank=True, help_text="Versión del software TPV")
    metadata = models.JSONField(default=dict, blank=True, help_text="Metadatos adicionales del TPV")

    class Meta:
        verbose_name = "Instancia TPV"
        verbose_name_plural = "Instancias TPV"
        ordering = ['-created_at']

    def __str__(self):
        return f"{self.name} ({'Activo' if self.is_active else 'Inactivo'})"

    def is_online(self):
        """Verifica si el TPV está online (heartbeat en los últimos 5 minutos)"""
        if not self.last_heartbeat:
            return False
        return (timezone.now() - self.last_heartbeat).seconds < 300

    def update_heartbeat(self):
        """Actualiza el timestamp del último heartbeat"""
        self.last_heartbeat = timezone.now()
        self.save(update_fields=['last_heartbeat'])


class TelegramUser(models.Model):
    """
    Usuario de Telegram asociado a una instancia TPV específica.
    """
    telegram_id = models.BigIntegerField(unique=True, help_text="ID de usuario de Telegram")
    username = models.CharField(max_length=200, blank=True, null=True)
    first_name = models.CharField(max_length=200, blank=True)
    last_name = models.CharField(max_length=200, blank=True)
    
    # Asignación a TPV
    tpv_instance = models.ForeignKey(
        TPVInstance, 
        on_delete=models.SET_NULL, 
        null=True,
        related_name='users',
        help_text="Instancia TPV asignada a este usuario"
    )
    
    # Estado
    is_active = models.BooleanField(default=True)
    language_code = models.CharField(max_length=10, blank=True)
    
    # Metadatos
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    last_interaction = models.DateTimeField(auto_now=True)
    
    metadata = models.JSONField(default=dict, blank=True)

    class Meta:
        verbose_name = "Usuario Telegram"
        verbose_name_plural = "Usuarios Telegram"
        ordering = ['-last_interaction']

    def __str__(self):
        return f"@{self.username or self.telegram_id} -> {self.tpv_instance.name if self.tpv_instance else 'Sin asignar'}"


class Instruction(models.Model):
    """
    Instrucción enviada desde Telegram hacia una instancia TPV.
    El TPV consulta periódicamente por instrucciones pendientes.
    """
    STATUS_CHOICES = [
        ('pending', 'Pendiente'),
        ('sent', 'Enviado'),
        ('delivered', 'Entregado'),
        ('processing', 'Procesando'),
        ('completed', 'Completado'),
        ('failed', 'Fallido'),
    ]
    
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    tpv_instance = models.ForeignKey(
        TPVInstance, 
        on_delete=models.CASCADE, 
        related_name='instructions'
    )
    telegram_user = models.ForeignKey(
        TelegramUser, 
        on_delete=models.CASCADE, 
        related_name='instructions'
    )
    
    # Contenido de la instrucción
    command = models.CharField(max_length=100, help_text="Comando o acción a ejecutar")
    payload = models.JSONField(default=dict, help_text="Datos adicionales de la instrucción")
    
    # Estado
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending')
    priority = models.IntegerField(default=5, help_text="Prioridad (1=alta, 10=baja)")
    
    # Tiempos
    created_at = models.DateTimeField(auto_now_add=True)
    sent_at = models.DateTimeField(null=True, blank=True)
    delivered_at = models.DateTimeField(null=True, blank=True)
    completed_at = models.DateTimeField(null=True, blank=True)
    expires_at = models.DateTimeField(null=True, blank=True, help_text="Fecha de expiración")
    
    # Respuesta del TPV
    response = models.JSONField(null=True, blank=True, help_text="Respuesta del TPV")
    error_message = models.TextField(blank=True)
    
    # Retry
    retry_count = models.IntegerField(default=0)
    max_retries = models.IntegerField(default=3)

    class Meta:
        verbose_name = "Instrucción"
        verbose_name_plural = "Instrucciones"
        ordering = ['priority', '-created_at']
        indexes = [
            models.Index(fields=['tpv_instance', 'status']),
            models.Index(fields=['status', 'created_at']),
        ]

    def __str__(self):
        return f"{self.command} -> {self.tpv_instance.name} ({self.status})"

    def mark_as_sent(self):
        """Marca la instrucción como enviada"""
        self.status = 'sent'
        self.sent_at = timezone.now()
        self.save(update_fields=['status', 'sent_at'])

    def mark_as_delivered(self):
        """Marca la instrucción como entregada al TPV"""
        self.status = 'delivered'
        self.delivered_at = timezone.now()
        self.save(update_fields=['status', 'delivered_at'])

    def mark_as_completed(self, response=None):
        """Marca la instrucción como completada"""
        self.status = 'completed'
        self.completed_at = timezone.now()
        if response:
            self.response = response
        self.save(update_fields=['status', 'completed_at', 'response'])

    def mark_as_failed(self, error_message=''):
        """Marca la instrucción como fallida"""
        self.status = 'failed'
        self.error_message = error_message
        self.save(update_fields=['status', 'error_message'])


class AllowedOrigin(models.Model):
    """
    Orígenes CORS permitidos de forma dinámica.
    Permite agregar/quitar orígenes sin reiniciar el servidor.
    """
    origin = models.URLField(
        unique=True,
        help_text="URL del origen permitido (ej: https://tpvtest.valletpv.es)"
    )
    description = models.CharField(
        max_length=255,
        blank=True,
        help_text="Descripción del origen"
    )
    is_active = models.BooleanField(
        default=True,
        help_text="¿Está activo este origen?"
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = 'webhook_allowed_origins'
        verbose_name = "Origen CORS Permitido"
        verbose_name_plural = "Orígenes CORS Permitidos"
        ordering = ['origin']

    def __str__(self):
        status = "✅" if self.is_active else "❌"
        return f"{status} {self.origin}"

    @classmethod
    def get_active_origins(cls):
        """Obtiene lista de orígenes activos"""
        return list(cls.objects.filter(is_active=True).values_list('origin', flat=True))


class PushMessage(models.Model):
    """
    Mensaje push que un TPV quiere enviar a un usuario de Telegram.
    El webhook lo procesa y lo envía a través del bot.
    """
    STATUS_CHOICES = [
        ('pending', 'Pendiente'),
        ('sent', 'Enviado'),
        ('failed', 'Fallido'),
    ]
    
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    tpv_instance = models.ForeignKey(
        TPVInstance, 
        on_delete=models.CASCADE, 
        related_name='push_messages'
    )
    telegram_user = models.ForeignKey(
        TelegramUser, 
        on_delete=models.CASCADE, 
        related_name='push_messages'
    )
    
    # Contenido
    message_type = models.CharField(
        max_length=50, 
        default='text',
        choices=[
            ('text', 'Texto'),
            ('photo', 'Foto'),
            ('document', 'Documento'),
            ('location', 'Ubicación'),
        ]
    )
    text = models.TextField(blank=True, help_text="Texto del mensaje")
    data = models.JSONField(default=dict, blank=True, help_text="Datos adicionales (URL imagen, etc)")
    
    # Estado
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending')
    
    # Tiempos
    created_at = models.DateTimeField(auto_now_add=True)
    sent_at = models.DateTimeField(null=True, blank=True)
    
    # Resultado
    telegram_message_id = models.BigIntegerField(null=True, blank=True)
    error_message = models.TextField(blank=True)
    
    # Retry
    retry_count = models.IntegerField(default=0)
    max_retries = models.IntegerField(default=3)

    class Meta:
        verbose_name = "Mensaje Push"
        verbose_name_plural = "Mensajes Push"
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['status', 'created_at']),
            models.Index(fields=['tpv_instance', 'status']),
        ]

    def __str__(self):
        return f"Push {self.message_type} a @{self.telegram_user.username or self.telegram_user.telegram_id} ({self.status})"

    def mark_as_sent(self, telegram_message_id=None):
        """Marca el mensaje como enviado"""
        self.status = 'sent'
        self.sent_at = timezone.now()
        if telegram_message_id:
            self.telegram_message_id = telegram_message_id
        self.save(update_fields=['status', 'sent_at', 'telegram_message_id'])

    def mark_as_failed(self, error_message=''):
        """Marca el mensaje como fallido"""
        self.status = 'failed'
        self.error_message = error_message
        self.retry_count += 1
        self.save(update_fields=['status', 'error_message', 'retry_count'])
