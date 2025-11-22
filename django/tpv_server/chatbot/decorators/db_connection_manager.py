"""
Decorador para manejar conexiones de base de datos en herramientas del chatbot.
"""
import functools
from django.db import connections
from django.db.utils import InterfaceError, OperationalError
from gestion.tools.config_logs import log_debug_chatbot as logger


def db_connection_handler(func):
    """
    Decorador que maneja las conexiones de base de datos para las herramientas.
    Cierra automáticamente las conexiones después de ejecutar la función.
    """
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        try:
            # Ejecutar la función
            result = func(*args, **kwargs)
            return result
        except (InterfaceError, OperationalError) as e:
            logger.error(f"Error de conexión DB en {func.__name__}: {e}", exc_info=True)
            # Cerrar conexiones problemáticas
            close_db_connections()
            # Reintentar una vez
            try:
                result = func(*args, **kwargs)
                return result
            except Exception as retry_error:
                logger.error(f"Error en reintento de {func.__name__}: {retry_error}", exc_info=True)
                return {"error": f"Error de conexión de base de datos en {func.__name__}"}
        except Exception as e:
            logger.error(f"Error general en {func.__name__}: {e}", exc_info=True)
            return {"error": f"Error interno en {func.__name__}"}
        finally:
            # Cerrar conexiones después de cada herramienta
            close_db_connections()
    
    return wrapper


def close_db_connections():
    """
    Cierra todas las conexiones de base de datos.
    """
    try:
        for conn in connections.all():
            if conn.connection and not conn.connection.closed:
                conn.close()
    except (InterfaceError, OperationalError) as e:
        # Estas excepciones son normales cuando las conexiones ya están cerradas
    except Exception as e:
        logger.error(f"Error al cerrar conexiones DB: {e}", exc_info=True)
