import json
from tokenapi.http import JsonResponse
from gestion.models import PeticionesAutoria, Camareros, Mesas


def lista_mens_autorizaciones(request, id):
    peticiones = PeticionesAutoria.objects.filter(idautorizado=id)
    mensajes = []
    for p in peticiones:
        if p.accion == "borrar_mesa":
            instrucciones = json.loads(p.instrucciones)
            camarero = Camareros.objects.get(id=instrucciones["idc"])
            mesa = Mesas.objects.get(id=instrucciones["idm"])
            motivo = instrucciones["motivo"]
            mensajes.append(camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar la mesa completa "
            + mesa.nombre +" por el motivo:  "+ motivo)
        elif p.accion == "borrar_linea":
            inst = json.loads(p.instrucciones)
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            nombre = inst["Nombre"]
            can = inst["can"]
            motivo = inst["motivo"]
            mensajes.append(camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar " + can +" "+nombre + " de la mesa "
            + mesa.nombre +" por el motivo:  "+ motivo)

    return JsonResponse(mensajes)
