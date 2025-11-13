import inspect
import sys
import os
from functools import wraps
from gestion.tools.config_logs import configurar_logging


def log_excepciones(nombre_archivo="excepciones.log"):
    """
    Decorador que loguea excepciones con su archivo de log personalizado.
    
    Args:
        nombre_archivo: Nombre del archivo de log (ej: "dispositivos.log", "sync.log")
    
    Uso:
        @log_excepciones()  # Usa "excepciones.log" por defecto
        @log_excepciones("dispositivos.log")  # Log personalizado
    """
    def decorator(func):
        # Crear logger específico basado en el nombre del archivo
        logger_name = nombre_archivo.replace('.log', '')
        log = configurar_logging(logger_name, nombre_archivo)
        
        @wraps(func)
        def wrapper(*args, **kwargs):
            try:
                return func(*args, **kwargs)
            except Exception as e:
                nombre_funcion = func.__name__
                parametros = inspect.signature(func).bind(*args, **kwargs).arguments
                exc_type, exc_obj, exc_tb = sys.exc_info()
                nombre_archivo_exc = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
                linea_numero = exc_tb.tb_lineno

                log.exception(
                    f"Excepción en {nombre_funcion} (Parámetros: {parametros}), "
                    f"Archivo: {nombre_archivo_exc}, Línea: {linea_numero}: {e}"
                )
                raise  # Re-lanza la excepción
        return wrapper
    
    # Permitir usar el decorador con o sin paréntesis
    if callable(nombre_archivo):
        # Llamado como @log_excepciones sin paréntesis
        func = nombre_archivo
        nombre_archivo = "excepciones.log"
        return decorator(func)
    
    return decorator