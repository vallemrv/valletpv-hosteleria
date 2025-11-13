import logging
import logging.handlers
import os
from django.conf import settings

# Configuración inicial del directorio de logs
media_root = settings.MEDIA_ROOT
log_dir = os.path.join(media_root, 'logs')
os.makedirs(log_dir, exist_ok=True)

def configurar_logging(nombre_logger, nombre_archivo=None, tipo_logger=logging.DEBUG):
    """Configura un logger específico con un archivo de log dedicado."""
    # Crear un logger con un nombre único
    logger = logging.getLogger(nombre_logger)
    logger.setLevel(tipo_logger)  # Captura todo
    nombre_archivo = nombre_archivo or f"{nombre_logger}.log"

    # Remover handlers previos para este logger específico
    logger.handlers = []

    # Desactivar propagación al logger raíz (opcional, dependiendo de tus necesidades)
    logger.propagate = False

    # Formato del log
    formato = logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s'
    )

    # Ruta completa del archivo de log
    nombre_archivo_completo = os.path.join(log_dir, nombre_archivo)

    # Configurar el handler para rotación de archivos
    handler_archivo = logging.handlers.RotatingFileHandler(
        nombre_archivo_completo, maxBytes=1024 * 1024, backupCount=5
    )
    handler_archivo.setFormatter(formato)
    logger.addHandler(handler_archivo)
    
    
    return logger

# Configurar loggers distintos con nombres únicos
logger = configurar_logging("excepciones", "excepciones.log")
logger_sync = configurar_logging("sync", "sync_devices.log")
log_debug_chatbot = configurar_logging("varios", "debug_chatbot.log")
log_comunicaciones = configurar_logging("comunicaciones", "comunicaciones.log")


