from django.forms import model_to_dict
from gestion.models import Camareros
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt


@csrf_exempt
def listado_camareros(request):
    camareros = Camareros.objects.filter(autorizado=1)
    objres = []
    for c in camareros:
        
        objres.append(model_to_dict(c))

    return JsonResponse(objres)