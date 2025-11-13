# push_telegram/apps.py

from django.apps import AppConfig


class PushTelegramConfig(AppConfig):
    """
    Sistema de Push Notifications para Telegram
    
    Envía notificaciones push basadas en eventos/hooks del sistema.
    No requiere bot corriendo constantemente.
    """
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'push_telegram'
    verbose_name = 'Push Telegram - Notificaciones'