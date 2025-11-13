# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-31T16:25:12+01:00
# @License: Apache License v2.0

from django.http import JsonResponse
from api.decorators.uid_activo import verificar_uid_activo
from comunicacion.tools import comunicar_cambios_devices
from gestion.models.teclados import TeclasAgotadas, Teclas
from datetime import date



@verificar_uid_activo
def agregar_articulo(request):
    """
    Vista para bloquera teclas en el tpv por falta de existencias..
    """
    if request.method == 'POST':
        try:
            id_tecla = request.POST.get('IDTecla')  # Cambiar clave a 'IDTecla'
            if not id_tecla:
                return JsonResponse({'error': 'El campo IDTecla es obligatorio'}, status=400)
            
            TeclasAgotadas.objects.filter(tecla_id=id_tecla).delete()
            tecla = Teclas.objects.filter(id=id_tecla).first()
            if not tecla:
                return JsonResponse({'error': 'Tecla no encontrada'}, status=404)
            registro = TeclasAgotadas(tecla=tecla, fecha=date.today().strftime("%Y/%m/%d"))
            registro.save()
            
            return JsonResponse({'message': 'Artículo agregado correctamente', 'registro': str(registro)}, status=201)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)
    return JsonResponse({'error': 'Método no permitido'}, status=405)

@verificar_uid_activo
def borrar_articulo(request):
    """
    Vista para desbloquear la tecla bloqueada por ya hay existencias.
    """
    if request.method == 'POST':
        try:
            id_tecla = request.POST.get('IDTecla')  # Cambiar clave a 'IDTecla'
            if not id_tecla:
                return JsonResponse({'error': 'El campo IDTecla es obligatorio'}, status=400)
            
            # Borrar todas las entradas asociadas a IDTecla
            registros_eliminados = TeclasAgotadas.objects.filter(tecla_id=id_tecla).delete()
            
            # Crear un registro en ExistenciaTecla
            tecla = Teclas.objects.filter(id=id_tecla).first()
            if not tecla:
                return JsonResponse({'error': 'Tecla no encontrada'}, status=404)
            
            else:
                # Comunicar cambios de la tecla relacionada (afecta sección)
                comunicar_cambios_devices("md", "teclas", tecla.serialize())

            
            return JsonResponse({'message': 'Entradas eliminadas correctamente', 'registros_eliminados': registros_eliminados[0]}, status=200)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)
    return JsonResponse({'error': 'Método no permitido'}, status=405)


