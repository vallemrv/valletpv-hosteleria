from functools import wraps
from django.http import QueryDict
from django.views.decorators.csrf import csrf_exempt 
from tokenapi.http import JsonError, JsonResponseUnauthorized
from valle_tpv.models import Dispositivos
import json

def check_dispositivo(view_func):
    @csrf_exempt
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        # Si no es un método POST
        
        if request.method != "POST":
            return JsonError({'error': 'Tiene que ser POST'})

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
            return JsonError({'error': 'El cuerpo de la solicitud no es JSON válido'})

        # Verificar en la base de datos
        if not Dispositivos.objects.filter(UID=uid, codigo=codigo).exists():
            return JsonResponseUnauthorized({'error': 'Permiso denegado'})
        
        # Si todo está bien, procede a la vista
        return view_func(request, *args, **kwargs)
    return _wrapped_view
