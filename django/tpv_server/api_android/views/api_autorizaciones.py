
from gettext import install
import json
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from tokenapi.http import JsonResponse
from gestion.models import Camareros, Lineaspedido, Mesas, Mesasabiertas, PeticionesAutoria


@csrf_exempt
def send_informacion(request):
    idreceptor = request.POST["idreceptor"]
    autor = request.POST["autor"]
    men = request.POST["mensaje"]
    ids = []
   
    if int(idreceptor) == -1:
        ids = Camareros.objects.filter(autorizado = 1).values_list("id", flat=True)
    else:
        ids.append(idreceptor)

    
    for i in ids:
        peticion = PeticionesAutoria()
        peticion.accion = request.POST["accion"]
        peticion.instrucciones = json.dumps({"mensaje":men, "autor": autor})
        peticion.idautorizado = Camareros.objects.get(id=i)
        peticion.save()
    return JsonResponse("success")


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
        inst = json.loads(p.instrucciones)
        if p.accion == "borrar_mesa":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            motivo = inst["motivo"]
            mensajes.append({"tipo": "peticion", "idpeticion": p.pk, "mensaje":camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar la mesa completa "
            + mesa.nombre +" por el motivo:  "+ motivo})
        elif p.accion == "borrar_linea":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            nombre = inst["Nombre"]
            can = inst["can"]
            motivo = inst["motivo"]
            mensajes.append({"tipo": "peticion", "idpeticion": p.pk, "mensaje":camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar " + can +" "+nombre + " de la mesa "
            + mesa.nombre +" por el motivo:  "+ motivo})
        elif p.accion == "informacion":
            mensajes.append({"tipo": "informacion", "idpeticion": p.pk, "mensaje": "Mensaje de "+inst["autor"]+": \n "+ inst["mensaje"]})

    return JsonResponse(mensajes)


@csrf_exempt
def gestionar_peticion(request):
    aceptada = request.POST["aceptada"]
    idpeticion = request.POST["idpeticion"]
    p = PeticionesAutoria.objects.get(id=idpeticion)
    if aceptada == "1":
        inst = json.loads(p.instrucciones)
        if p.accion == "borrar_mesa":
            Mesasabiertas.borrar_mesa_abierta(int(inst["idm"]), int(inst["idc"]), inst["motivo"])
        elif p.accion == "borrar_linea":
            Lineaspedido.borrar_linea_pedido(int(inst["idm"]), inst["Precio"], int(inst["idArt"]), int(inst["can"]), 
                int(inst["idc"]), inst["motivo"], inst["Estado"], inst["Nombre"])

    p.delete()
        

    return JsonResponse("susccess")
