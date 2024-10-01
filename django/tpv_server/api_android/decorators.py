# @Author: Manuel Rodriguez <valle>
# @Date:   20-Jul-2017
# @Email:  valle.mrv@gmail.com
# @Filename: decorators.py
# @Last modified by:   valle
# @Last modified time: 2019-01-16T23:49:19+01:00
# @License: Apache license vesion 2.0


from functools import wraps
from django.views.decorators.csrf import csrf_exempt
from gestion.models.camareros import Camareros
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
        
        # 1. Intentar obtener el UID desde request.POST
        uid = request.POST.get('uid')
        
        # 2. Si no está en POST, buscar en request.GET
        if not uid:
            uid = request.GET.get('uid')
        
        # 3. Si tampoco está en GET, intentar obtenerlo del cuerpo JSON
        if not uid:
            try:
                data = json.loads(request.body)
                uid = data.get('uid')
            except (json.JSONDecodeError, TypeError):
                pass  # Ignorar si el cuerpo no es un JSON válido

        # Verificar si el UID fue proporcionado
        if not uid:
            return JsonResponseForbidden({"error": "UID no proporcionado"})
        
        # Verificar si el UID existe en la base de datos y está activo
        try:
            dispositivo = Dispositivo.objects.get(uid=uid)
            if not dispositivo.activo:
                return JsonResponseForbidden({"error": "UID no activo"})
        except Dispositivo.DoesNotExist:
            return JsonResponseForbidden({"error": "UID no válido"})

        # Si todo está correcto, proceder con la vista
        return view_func(request, *args, **kwargs)
    
    return _wrapped_view
