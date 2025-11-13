# chatbot/apps.py

from django.apps import AppConfig


class ChatbotConfig(AppConfig):
    """
    Configuración de la aplicación Chatbot
    
    Esta aplicación maneja:
    - Mensajes guardados del chat
    - Sesiones de chat 
    - Plantillas de mensajes
    - APIs REST para el frontend
    """
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'chatbot'
    verbose_name = 'Chatbot ValleTPV'
    
    def ready(self):
        """
        Código que se ejecuta cuando la app está lista
        """
        # Importar señales si las hubiera
        # import chatbot.signals
        pass
