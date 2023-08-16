from functools import wraps
from tokenapi.http import JsonResponse, JsonError, JsonResponseUnauthorized
from valle_tpv.models import Dispositivos

def check_dispositivo(view_func):
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        # Si no es un método POST
        if request.method != "POST":
            return JsonError({'error': 'Tiene que ser POST'})

        # Obtener UID y código del cuerpo del POST
        uid = request.POST.get('UID')
        codigo = request.POST.get('codigo')

        # Verificar en la base de datos
        if not Dispositivos.objects.filter(UID=uid, codigo=codigo).exists():
            return JsonResponseUnauthorized({'error': 'Permiso denegado'})
        
        # Si todo está bien, procede a la vista
        return view_func(request, *args, **kwargs)
    return _wrapped_view
