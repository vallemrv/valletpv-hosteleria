from functools import wraps
from base64 import b64decode
from django.contrib.auth import authenticate
from django.views.decorators.csrf import csrf_exempt
from tokenapi.http import JsonResponseForbidden, JsonResponseUnauthorized
from gestion.models.dispositivos import Dispositivo
from gestion.tools.config_logs import configurar_logging
import json

logger = configurar_logging("uid_activo")

@csrf_exempt
def verificar_uid_activo(view_func):
    @csrf_exempt
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        
        # 1. Intentar autenticación por TOKEN (adaptado de token_required)
        basic_auth = request.META.get('HTTP_AUTHORIZATION')
        user = request.POST.get('user', request.GET.get('user'))
        token = request.POST.get('token', request.GET.get('token'))

        if not (user and token) and basic_auth:
            try:
                auth_method, auth_string = basic_auth.split(' ', 1)
                if auth_method.lower() == 'basic':
                    auth_string = b64decode(auth_string.strip())
                    user, token = auth_string.decode().split(':', 1)
            except Exception:
                pass # Ignorar errores de formateo en auth header

        if user and token:
            user_obj = authenticate(pk=user, token=token)
            if user_obj:
                request.user = user_obj
                return view_func(request, *args, **kwargs)
        
        # 2. Si no hay token válido, intentar autenticación por UID (Original)
        # Asegurarse de que la petición sea solo POST para este método
        if request.method == "POST":
            uid = None
            
            # Primero, intenta obtener el UID del cuerpo de la petición.
            if 'application/json' in request.content_type:
                try:
                    # Leemos el cuerpo UNA SOLA VEZ
                    data = json.loads(request.body)
                    # Adjuntamos los datos al request para que la vista los pueda usar
                    request.json_data = data
                    uid = data.get('uid')
                except (json.JSONDecodeError, TypeError):
                    logger.warning("[VERIFICAR_UID] El cuerpo parecía JSON pero no se pudo parsear")
                    pass 
            
            if not uid:
                uid = request.POST.get('uid')
            
            if not uid:
                uid = request.GET.get('uid')

            if uid:
                # Verificar si el UID existe en la base de datos y está activo
                try:
                    dispositivo = Dispositivo.objects.get(uid=uid)
                    if not dispositivo.activo:
                        return JsonResponseForbidden({"error": "Dispositivo no activo"})
                    
                    # Opcional: Adjuntar el objeto dispositivo al request
                    request.dispositivo = dispositivo
                    return view_func(request, *args, **kwargs)

                except Dispositivo.DoesNotExist:
                    return JsonResponseForbidden({"error": "Dispositivo no existe"})
        
        # Si no se cumplió ninguna autenticación
        return JsonResponseForbidden({"error": "Autenticación fallida: Se requiere UID activo (POST) o Token válido"})
    
    return _wrapped_view