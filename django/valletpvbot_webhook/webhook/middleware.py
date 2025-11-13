"""
Middleware personalizado para CORS dinámico desde base de datos.
"""

from django.utils.deprecation import MiddlewareMixin
from django.core.cache import cache


class DynamicCorsMiddleware(MiddlewareMixin):
    """
    Middleware que permite orígenes CORS desde la base de datos.
    Cachea los orígenes por 60 segundos para evitar consultas constantes.
    """
    
    CACHE_KEY = 'allowed_cors_origins'
    CACHE_TIMEOUT = 60  # 60 segundos
    
    def process_request(self, request):
        """Procesa la petición y configura los headers CORS"""
        origin = request.META.get('HTTP_ORIGIN')
        
        if not origin:
            return None
        
        # Obtener orígenes permitidos del cache o BD
        allowed_origins = cache.get(self.CACHE_KEY)
        
        if allowed_origins is None:
            from .models import AllowedOrigin
            allowed_origins = AllowedOrigin.get_active_origins()
            cache.set(self.CACHE_KEY, allowed_origins, self.CACHE_TIMEOUT)
        
        # Guardar en request para usar en process_response
        request._cors_allowed_origins = allowed_origins
        request._cors_origin = origin
        
        return None
    
    def process_response(self, request, response):
        """Agrega headers CORS si el origen está permitido"""
        
        if not hasattr(request, '_cors_origin'):
            return response
        
        origin = request._cors_origin
        allowed_origins = getattr(request, '_cors_allowed_origins', [])
        
        # Verificar si el origen está permitido
        if origin in allowed_origins:
            response['Access-Control-Allow-Origin'] = origin
            response['Access-Control-Allow-Credentials'] = 'true'
            response['Access-Control-Allow-Methods'] = 'GET, POST, PUT, PATCH, DELETE, OPTIONS'
            response['Access-Control-Allow-Headers'] = 'Content-Type, Authorization, X-Requested-With'
            response['Access-Control-Max-Age'] = '3600'
        
        return response
