from functools import wraps
from django.http import QueryDict
from django.views.decorators.csrf import csrf_exempt 
from tokenapi.http import JsonError, JsonResponseUnauthorized
from valle_tpv.models import Dispositivos
import json


def superuser_or_staff_required(view_func):
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        if request.user.is_authenticated:
            if request.user.is_superuser or request.user.is_staff:
                return view_func(request, *args, **kwargs)
        return JsonError('Permisos insuficientes')
    return _wrapped_view

def superuser_required(view_func):
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        if request.user.is_authenticated:
            if request.user.is_superuser:
                return view_func(request, *args, **kwargs)
        return JsonError('Permisos insuficientes')
    return _wrapped_view


def check_dispositivo(view_func):
    @csrf_exempt
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        # Si no es un método POST
        
        if request.method != "POST":
            return JsonError('Tiene que ser POST')

        try:
            data = json.loads(request.body.decode('utf-8'))

            # Convertir el JSON decodificado en un QueryDict mutable
            mutable_post = QueryDict('', mutable=True)
            for key, value in data.items():
                mutable_post[key] = value

            # Reemplazar request.POST con el nuevo QueryDict mutable
            request.POST = mutable_post

            # Ahora puedes acceder a los datos como si estuvieran en POST
            uid = request.POST['UID']
            codigo = request.POST['codigo']
        except json.JSONDecodeError:
            return JsonError( 'El cuerpo de la solicitud no es JSON válido')
        except KeyError:
            return JsonError( 'La cosulsta ha de contener UID y codigo del dispositivo')

        # Verificar en la base de datos
        if not Dispositivos.objects.filter(UID=uid, codigo=codigo).exists():
            return JsonResponseUnauthorized('Permiso denegado')
        
        # Si todo está bien, procede a la vista
        return view_func(request, *args, **kwargs)
    return _wrapped_view
