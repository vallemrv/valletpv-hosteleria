# gestion/middleware.py

from django.db import close_old_connections
from channels.db import database_sync_to_async

class DatabaseConnectionMiddleware:
    """
    Middleware para asegurar que las conexiones a la base de datos
    se cierren correctamente en un entorno asíncrono.
    """
    def __init__(self, app):
        self.app = app

    async def __call__(self, scope, receive, send):
        # Cierra las conexiones antiguas antes de que la aplicación maneje el evento.
        # Esto previene errores de "MySQL server has gone away".
        await database_sync_to_async(close_old_connections)()
        
        # Pasa el control a la siguiente capa de la aplicación.
        return await self.app(scope, receive, send)