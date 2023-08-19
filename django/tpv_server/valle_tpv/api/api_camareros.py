from valle_tpv.decorators import check_dispositivo
from tokenapi.http import JsonResponse, JsonError
from valle_tpv.models import Camareros
from valle_tpv.tools.ws import comunicar_cambios_devices

@check_dispositivo
def add(request):
    try:
        nombre = request.POST.get('nombre', None)
        apellido = request.POST.get('apellido', None)
        if Camareros.objects.filter(nombre=nombre, apellidos=apellido).exists():
            return JsonError( 'Ya existe un camarero con ese nombre y apellido')
        camarero = Camareros(nombre=nombre, apellidos=apellido, autorizado=True, activo=True)
        camarero.save()
        comunicar_cambios_devices("insert", "camareros", camarero.serialize())
        return JsonResponse({})
    except:
        print("Error al crear el camarero")
        return JsonError( 'Error al crear el camarero')


@check_dispositivo
def set_password(request):
    try:
        camarero = Camareros.objects.get(pk=request.POST.get('id', None))
        camarero.password = request.POST.get('password', None)
        camarero.save()
        return JsonResponse({})
    except:
        return JsonError('No existe el camarero')

@check_dispositivo
def set_autorizado(request):
    try:
        camarero = Camareros.objects.get(pk=request.POST.get('id', None))
        camarero.autorizado = True if request.POST.get('autorizado', None).lower() == "true" else False
        camarero.save()
        return JsonResponse({})
    except:
        return JsonError('No existe el camarero')