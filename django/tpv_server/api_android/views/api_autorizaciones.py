
from gettext import install
import json
from django.views.decorators.csrf import csrf_exempt
from tokenapi.http import JsonResponse
from gestion.models import Camareros, Lineaspedido, Mesas, Mesasabiertas, PeticionesAutoria


@csrf_exempt
def pedir_autorizacion(request):
    peticion = PeticionesAutoria()
    peticion.accion = request.POST["accion"]
    peticion.instrucciones = request.POST["instrucciones"]
    peticion.idautorizado = Camareros.objects.get(id=request.POST["idautorizado"])
    peticion.save()
    return JsonResponse("success")


@csrf_exempt
def get_lista_autorizaciones(request):
    peticiones = PeticionesAutoria.objects.filter(idautorizado=request.POST["idautorizado"])
    mensajes = []
    for p in peticiones:
        if p.accion == "borrar_mesa":
            inst = json.loads(p.instrucciones)
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            motivo = inst["motivo"]
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


@csrf_exempt
def gestionar_peticion(request):
    aceptada = request.POST["aceptada"]
    idpeticion = request.POST["idpeticion"]
    p = PeticionesAutoria.objects.get(id=idpeticion)
    if aceptada == "1":
        inst = json.loads(p.instrucciones)
        if p.accion == "borrar_mesa":
            Mesasabiertas.borrar_mesa_abierta(inst["idm"], inst["idc"], inst["motivo"])
        elif p.accion == "borrar_linea":
            Lineaspedido.borrar_linea_pedido(inst["idm", 
                inst["Precio"], inst["idArt"], inst["can"], 
                inst["idc"], inst["motivo"], inst["Estado"], inst["Nombre"]])

    p.delete()
        

    return JsonResponse("susccess")
