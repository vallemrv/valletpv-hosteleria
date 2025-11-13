import json
from datetime import datetime
from api.decorators.uid_activo import verificar_uid_activo
from tokenapi.http import JsonResponse
from comunicacion.tools import comunicar_cambios_devices
from gestion.models.camareros import Camareros
from gestion.models.pedidos import Lineaspedido
from gestion.models.familias import Receptores
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.peticionesautoria import PeticionesAutoria
from gestion.models.ticket import Ticket
from comunicacion.tools import send_mensaje_impresora
from api.tools.impresion import send_imprimir_ticket

@verificar_uid_activo
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


@verificar_uid_activo
def pedir_autorizacion(request):
    id = request.POST["idautorizado"]
    
    peticion = PeticionesAutoria()
    peticion.accion = request.POST["accion"]
    peticion.instrucciones = request.POST["instrucciones"]
    peticion.idautorizado = Camareros.objects.filter(id=id).first()
    
    peticion.save()
    
    comunicar_cambios_devices("men", "mensajes", peticion.serialize())
   
    return JsonResponse("success")


@verificar_uid_activo
def get_lista_autorizaciones(request):
    id = -1 if "idautorizado" not in request.POST else request.POST["idautorizado"]
    return JsonResponse(get_lista_men(id))

def get_lista_men(id): 
    if int(id) > 0:  
        peticiones = PeticionesAutoria.objects.filter(idautorizado=id)
    else:
        peticiones = PeticionesAutoria.objects.all()
    mensajes = []
    for p in peticiones:
        mensajes.append(p.serialize())
    
    return mensajes


@verificar_uid_activo
def gestionar_peticion(request):
    aceptada = str(request.POST["aceptada"])
    idpeticion = request.POST["idpeticion"]
    p = PeticionesAutoria.objects.filter(id=idpeticion).first()
    if p and aceptada == "1":
        inst = json.loads(p.instrucciones)
        if p.accion == "borrar_mesa":
            Mesasabiertas.borrar_mesa_abierta(int(inst["idm"]), int(inst["idc"]), inst["motivo"])
        elif p.accion == "borrar_linea":
            Lineaspedido.borrar_linea_pedido_by_ids(int(inst["idm"]), inst["ids"], int(inst["idc"]), inst["motivo"])
        elif p.accion == "abrir_cajon":
            obj = {
                "op": "open",
                "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
                "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
                "receptor_activo": True,
            }
            send_mensaje_impresora(obj)
        elif p.accion == "cobrar_ticket":
            total, id = Ticket.cerrar_cuenta(inst["idm"], inst["idc"], inst["entrega"], json.loads(inst["art"]))                    
            if (id > 0):
                send_imprimir_ticket(request, id)


    if p:
        p.delete()
        

    return JsonResponse("susccess")
