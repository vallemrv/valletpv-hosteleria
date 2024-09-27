import uuid
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from gestion.models.dispositivos import Dispositivo

@csrf_exempt
def create_uid(request):
    # Generar un UID único usando uuid4
    uid = str(uuid.uuid4())

    # Crear el objeto Dispositivo con el UID generado, activo=False y sin descripción
    dispositivo = Dispositivo.objects.create(uid=uid, activo=False)

    # Retornar una respuesta en formato JSON con el UID generado
    return JsonResponse({'uid': dispositivo.uid})
