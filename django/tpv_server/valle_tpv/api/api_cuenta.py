# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T15:08:41+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-01T02:14:29+02:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse
from django.db.models import Q, Sum, Count
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from valle_tpv.decorators import check_dispositivo
from valle_tpv.tools.impresion import send_imprimir_ticket
from valle_tpv.tools.ws import comunicar_cambios_devices
from valle_tpv.models import (Mesasabiertas, Lineaspedido, Pedidos, 
                            Infmesa, Ticket)
from datetime import datetime
from uuid import uuid4
import json


@check_dispositivo
def cuenta_rm(request):
    idm = request.POST["idm"]
    motivo = request.POST["motivo"]
    idc = request.POST["idc"]
    Mesasabiertas.borrar_mesa_abierta(idm,idc,motivo)
    return JsonResponse({})



@check_dispositivo
def cuenta_cobrar(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    entrega = request.POST["entrega"]
    ids = json.loads(request.POST["ids"])

    total, id = Ticket.cobrar_cuenta(idm, idc, entrega, ids)
           
    if (id > 0):
        send_imprimir_ticket(request, id)
        
    return JsonResponse({"totalcobro": str(total), "entrega": entrega})




@check_dispositivo
def editar_cuenta(request):
    Lineaspedido.borrar_linea_pedido(json.loads(request.POST["ids"]), request.POST["idc"], 
                                     request.POST["motivo"], request.POST["idm"])
    return JsonResponse({})


@csrf_exempt
def get_cuenta(request):
    id = request.POST['mesa_id']
    m_abierta = Mesasabiertas.objects.filter(mesa__pk=id).first()
    lstArt = []
    if m_abierta:
        lstArt = m_abierta.get_lineaspedido()

    
    return JsonResponse(lstArt)

@check_dispositivo
def juntarmesas(request):
    Mesasabiertas.juntar_mesas_abiertas( request.POST["idOrg"], request.POST["idDest"])
    return JsonResponse({})

@check_dispositivo
def cambiarmesas(request):
    Mesasabiertas.cambiar_mesas_abiertas(request.POST["idOrg"], request.POST["idDest"])
    return JsonResponse({})

@csrf_exempt
def mvlinea(request):
    idm = request.POST["idm"];
    idLinea = request.POST["idLinea"];
    linea = Lineaspedido.objects.filter(pk=idLinea).first()
    if linea:
         pedido = Pedidos.objects.get(pk=linea.pedido_id)
         idc = pedido.camarero_id;
         uid = linea.infmesa.uid;
         linea.modifiar_composicion()
         infmesa_aux = linea.infmesa
         
         mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
         if not mesa:
             infmesa = Infmesa()
             infmesa.camarero_id = idc
             infmesa.hora = datetime.now().strftime("%H:%M")
             infmesa.fecha = datetime.now().strftime("%Y/%m/%d")
             infmesa.uid = idm + '-' + str(uuid4())
             infmesa.save()
             mesa = Mesasabiertas()
             mesa.infmesa_id = infmesa.pk
             mesa.mesa_id = idm
             mesa.save()
             comunicar_cambios_devices("md", "mesasabiertas", mesa.serialize())


         pedido = Pedidos()
         pedido.infmesa_id = mesa.infmesa.pk
         pedido.hora = datetime.now().strftime("%H:%M")
         pedido.camarero_id = idc
         pedido.save()

         linea.infmesa_id =  mesa.infmesa.pk
         linea.pedido_id = pedido.pk
         linea.save()
         
         infmesa_aux.componer_articulos()
         pedido.infmesa.componer_articulos()
             
         comunicar_cambios_devices("md", "lineaspedido", linea.serialize())

         numart = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='R')) & Q(infmesa__uid=uid)).count()
         if numart<=0:
            for m in Mesasabiertas.objects.filter(infmesa__uid=uid):
                obj = m.serialize()
                obj["abierta"] = 0
                obj["num"] = 0
                comunicar_cambios_devices("md", "mesasabiertas", obj, {"op": "mv_linea"})
                m.delete()
           
         

    return HttpResponse('success')

