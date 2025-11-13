from functools import wraps
from django.views.decorators.csrf import csrf_exempt
from tokenapi.http import JsonResponseForbidden
from gestion.models.dispositivos import Dispositivo
import json

@csrf_exempt
def verificar_uid_activo(view_func):
    @csrf_exempt
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        
        # Asegurarse de que la petición sea solo POST
        if request.method != "POST":
            return JsonResponseForbidden({"error": "Solo se permiten peticiones POST"})
        
        uid = None
        
        # Primero, intenta obtener el UID del cuerpo de la petición.
        # Esto depende del Content-Type.
        if 'application/json' in request.content_type:
            try:
                # Leemos el cuerpo UNA SOLA VEZ
                data = json.loads(request.body)
                # Adjuntamos los datos al request para que la vista los pueda usar
                request.json_data = data
                uid = data.get('uid')
            except (json.JSONDecodeError, TypeError):
                print("[VERIFICAR_UID] El cuerpo parecía JSON pero no se pudo parsear")
                pass # Si falla el parseo, se intentarán otros métodos
        
        # Si no se encontró UID en el cuerpo JSON (o no era JSON),
        # intentar obtenerlo de request.POST (para form-data)
        if not uid:
            uid = request.POST.get('uid')
        
        # Como último recurso, buscar en request.GET (aunque es POST, a veces se usa)
        if not uid:
            uid = request.GET.get('uid')

        # Verificar si el UID fue proporcionado
        if not uid:
            return JsonResponseForbidden({"error": "UID no proporcionado"})
        
        
        # Verificar si el UID existe en la base de datos y está activo
        try:
            dispositivo = Dispositivo.objects.get(uid=uid)
            if not dispositivo.activo:
                return JsonResponseForbidden({"error": "Dispositivo no activo"})
            
            # Opcional: Adjuntar el objeto dispositivo al request para no volver a buscarlo
            request.dispositivo = dispositivo

        except Dispositivo.DoesNotExist:
            return JsonResponseForbidden({"error": "Dispositivo no existe"})

        # Si todo está correcto, proceder con la vista
        return view_func(request, *args, **kwargs)
    
    return _wrapped_view