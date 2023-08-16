from valle_tpv.decorators import check_dispositivo
from tokenapi.http import JsonResponse, JsonError
from valle_tpv.models import Camareros
from valle_tpv.tools.ws import comunicar_cambios_devices

@check_dispositivo
def add(request):
    nombre = request.POST.get('nombre', None)
    apellido = request.POST.get('apellido', None)
    if Camareros.objects.filter(nombre=nombre, apellido=apellido).exists():
        return JsonError({'error': 'Ya existe un camarero con ese nombre y apellido'})
    camarero = Camareros(nombre=nombre, apellido=apellido)
    camarero.save()
    comunicar_cambios_devices("insert", "camareros", camarero.serialize())
    return JsonResponse({})


@check_dispositivo
def set_password(request):
    camarero = Camareros.objects.get(pk=request.POST.get('id', None))
    camarero.password = request.POST.get('password', None)
    camarero.save()
    return JsonResponse({})