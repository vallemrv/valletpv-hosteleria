import json
from datetime import datetime
from django.views.decorators.csrf import csrf_exempt
from tokenapi.http import JsonResponse
from comunicacion.tools import comunicar_cambios_devices
from gestion.models import (Camareros, Lineaspedido,  Receptores,
                            Mesasabiertas, PeticionesAutoria, Ticket)
from api_android.tools import (send_imprimir_ticket, send_mensaje_devices, send_mensaje_impresora)

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

    comunicar_cambios_devices("men", "mensajes", peticion.serialize())

    return JsonResponse("success")


@csrf_exempt
def pedir_autorizacion(request):
    id = request.POST["idautorizado"]
    
    peticion = PeticionesAutoria()
    peticion.accion = request.POST["accion"]
    peticion.instrucciones = request.POST["instrucciones"]
    peticion.idautorizado = Camareros.objects.filter(id=id).first()
    
    peticion.save()
    
    comunicar_cambios_devices("men", "mensajes", peticion.serialize())
   
    return JsonResponse("success")


@csrf_exempt
def get_lista_autorizaciones(request):
    return JsonResponse(get_lista_men(request.POST["idautorizado"]))

def get_lista_men(id):   
    peticiones = PeticionesAutoria.objects.filter(idautorizado=id)
    mensajes = []
    for p in peticiones:
        mensajes.append(p.serialize())
    
    return mensajes


@csrf_exempt
def gestionar_peticion(request):
    aceptada = request.POST["aceptada"]
    idpeticion = request.POST["idpeticion"]
    p = PeticionesAutoria.objects.filter(id=idpeticion).first()
    if p and aceptada == "1":
        inst = json.loads(p.instrucciones)
        if p.accion == "borrar_mesa":
            Mesasabiertas.borrar_mesa_abierta(int(inst["idm"]), int(inst["idc"]), inst["motivo"])
        elif p.accion == "borrar_linea":
            Lineaspedido.borrar_linea_pedido(int(inst["idm"]), inst["Precio"], int(inst["idArt"]), int(inst["can"]), 
                int(inst["idc"]), inst["motivo"], inst["Estado"], inst["Descripcion"])
        elif p.accion == "abrir_cajon":
            obj = {
                "op": "open",
                "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
                "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
                "receptor_activo": True,
            }
            send_mensaje_impresora(obj)
        elif p.accion == "cobrar_ticket":
            numart, total, id = Ticket.cerrar_cuenta(inst["idm"], inst["idc"], inst["entrega"], json.loads(inst["art"]))
    
            if (numart <= 0):
                #enviar notficacion de update
                update = {
                    "OP": "UPDATE",
                    "Tabla": "mesasabiertas",
                    "receptor": "comandas",
                }
                send_mensaje_devices(update)
                    
            if (id > 0):
                send_imprimir_ticket(request, id)


    if p:
        p.delete()
        

    return JsonResponse("susccess")
