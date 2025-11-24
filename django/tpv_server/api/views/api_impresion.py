# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-31T16:11:02+01:00
# @License: Apache License v2.0

from api.tools import send_imprimir_ticket
from api.tools.smart_receptor import enviar_urgente_smart_receptor
from tokenapi.http import  JsonResponse
from comunicacion.tools import (comunicar_cambios_devices, 
                                send_mensaje_impresora)

from gestion.models.camareros import Camareros
from gestion.models.pedidos import Pedidos
from gestion.models.teclados import Teclas

from gestion.models.familias import Receptores
from gestion.models.mesasabiertas import Mesasabiertas
   

from django.http import HttpResponse
from django.db.models import Count, Sum
from api.decorators.uid_activo import verificar_uid_activo
from datetime import datetime


@verificar_uid_activo
def preimprimir(request):
    idm = request.POST["idm"]
    mesa_abierta = Mesasabiertas.objects.filter(mesa_id=idm).first()
    if mesa_abierta:
        infmesa = mesa_abierta.infmesa
        infmesa.numcopias = infmesa.numcopias + 1
        infmesa.save()
       
        camareo = infmesa.camarero
        mesa = mesa_abierta.mesa
        lineas = infmesa.lineaspedido_set.filter(estado="P")
        lineas = lineas.values("idart", 
                            "descripcion_t", 
                            "precio").annotate(can=Count('idart'),
                                                totallinea=Sum("precio"))
        

        receptor = Receptores.objects.get(nombre='Ticket')
        lineas_ticket = []
        for l in lineas:
            lineas_ticket.append(l)

        obj = {
        "op": "preticket",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": receptor.nomimp,
        "receptor_activo": receptor.activo,
        "camarero": camareo.nombre + " " + camareo.apellidos,
        "mesa": mesa.nombre,
        "numcopias": infmesa.numcopias,
        "lineas": lineas_ticket,
        'total': infmesa.lineaspedido_set.filter(estado="P").aggregate(Total=Sum("precio"))['Total']
        }

        if infmesa.numcopias <= 1:
            comunicar_cambios_devices("md", "mesasabiertas", 
                                    mesa_abierta.serialize(), 
                                    {"op": "preimprimir"})

        send_mensaje_impresora(obj)
    return JsonResponse({})


@verificar_uid_activo
def reenviarpedido(request):
    idp = request.POST["idp"];
    idr = request.POST.get("idr");
    pedido = Pedidos.objects.get(pk=idp);
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa_a = pedido.infmesa.mesasabiertas_set.first()
    
    # Obtener líneas del receptor (todas si idr es null)
    if idr:
        lineas_receptor = pedido.lineaspedido_set.filter(tecla__familia__receptor__pk=idr).select_related('tecla__familia__receptor')
    else:
        lineas_receptor = pedido.lineaspedido_set.all().select_related('tecla__familia__receptor')
    
    # Borrar todos los registros de servido para estas líneas
    from gestion.models.pedidos import Servidos
    for linea in lineas_receptor:
        Servidos.objects.filter(linea=linea).delete()
        # Notificar cambio a devices
        comunicar_cambios_devices("md", "lineaspedido", linea.serialize())
    
    lineas = lineas_receptor.values("idart",
                                     "descripcion",
                                     "estado",
                                     "pedido_id").annotate(can=Count('idart'))
    
    
    # Notificar a impresoras tradicionales y smart receptors
    send_urgente(lineas, pedido.hora, camarero, mesa_a)
    enviar_urgente_smart_receptor(lineas_receptor)
    
    return HttpResponse("success")

@verificar_uid_activo
def reenviarlinea(request):
    idp = request.POST["idp"];
    id = request.POST["id"];
    nombre = request.POST["Descripcion"];
    pedido = Pedidos.objects.get(pk=idp)
    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    mesa_a = pedido.infmesa.mesasabiertas_set.first()
    
    # Obtener líneas que coincidan
    lineas_reenviar = pedido.lineaspedido_set.filter(idart=id, descripcion=nombre).select_related('tecla__familia__receptor')
    
    # Borrar todos los registros de servido para estas líneas
    from gestion.models.pedidos import Servidos
    for linea in lineas_reenviar:
        Servidos.objects.filter(linea=linea).delete()
        # Notificar cambio a devices
        comunicar_cambios_devices("md", "lineaspedido", linea.serialize())
    
    lineas = lineas_reenviar.values("idart",
                                     "descripcion",
                                     "estado",
                                     "pedido_id").annotate(can=Count('idart'))
    
    # Notificar a impresoras tradicionales y smart receptors
    send_urgente(lineas, pedido.hora, camarero, mesa_a)
    enviar_urgente_smart_receptor(lineas_reenviar)
    
    return HttpResponse("success")

@verificar_uid_activo
def abrircajon(request):
    obj = {
        "op": "open",
        "fecha": datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f"),
        "receptor": Receptores.objects.get(nombre='Ticket').nomimp,
        "receptor_activo": True,
    }
    send_mensaje_impresora(obj)
    return HttpResponse("success")

@verificar_uid_activo
def imprimir_ticket(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id)
    return HttpResponse("success")


@verificar_uid_activo
def imprimir_factura(request):
    id = request.POST["id"]
    send_imprimir_ticket(request, id, True)
    return HttpResponse("success")


def send_urgente(lineas, hora, camarero, mesa_a):
    if mesa_a:
        mesa = mesa_a.mesa
        receptores = {}
        for l in lineas:
            receptor = Teclas.objects.get(id=l['idart']).familia.receptor
            if receptor.nombre not in receptores:
                receptores[receptor.nombre] = {
                    "op": "urgente",
                    "hora": hora,
                    "receptor_activo": receptor.activo,
                    "receptor": receptor.nomimp,
                    "nom_receptor": receptor.nombre,
                    "camarero": camarero.nombre + " " + camarero.apellidos,
                    "mesa": mesa.nombre,
                    "lineas": []
                }
            receptores[receptor.nombre]['lineas'].append(l)
    
        send_mensaje_impresora(receptores)
    return HttpResponse("success")