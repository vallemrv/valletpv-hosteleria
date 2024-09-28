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


@csrf_exempt
def verificar_uid_activo(view_func):
    @csrf_exempt
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        # Verificar si la solicitud es POST
        if request.method == "POST":
            uid = request.POST.get('uid')  # Obtener el UID del cuerpo del POST
            
            if not uid:
                return JsonResponseForbidden("UID no proporcionado")
            
            # Verificar si el UID existe en la base de datos y está activo
            try:
                dispositivo = Dispositivo.objects.get(uid=uid)
                if not dispositivo.activo:
                     return JsonResponseForbidden("UID no activo")
            except Dispositivo.DoesNotExist:
                return JsonResponseForbidden("UID no válido")

        # Si todo está correcto, proceder con la vista
        return view_func(request, *args, **kwargs)
    
    return _wrapped_view
