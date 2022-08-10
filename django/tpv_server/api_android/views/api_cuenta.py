# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T15:08:41+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-01T02:14:29+02:00
# @License: Apache License v2.0


from django.db.models import Q, Sum, Count
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from api_android.tools import (send_imprimir_ticket)
from comunicacion.tools import comunicar_cambios_devices
from gestion.models import (Mesasabiertas, Lineaspedido, Pedidos, 
                            Infmesa,  Sync, Ticket)
from datetime import datetime
from uuid import uuid4
import json



@csrf_exempt
def get_cuenta(request):
    id = request.POST['mesa_id']
    m_abierta = Mesasabiertas.objects.filter(mesa__pk=id).first()
    lstArt = []
    if m_abierta:
        #m_abierta.infmesa.componer_articulos()
        #m_abierta.infmesa.unir_en_grupos()
        lstArt = m_abierta.get_lineaspedido()

    return JsonResponse(lstArt)

@csrf_exempt
def juntarmesas(request):
    Mesasabiertas.juntar_mesas_abiertas( request.POST["idp"], request.POST["ids"])
    return HttpResponse('success')

@csrf_exempt
def cambiarmesas(request):
    Mesasabiertas.cambiar_mesas_abiertas(request.POST["idp"], request.POST["ids"])
    return HttpResponse('success')

@csrf_exempt
def mvlinea(request):
    idm = request.POST["idm"];
    idLinea = request.POST["idLinea"];
    linea = Lineaspedido.objects.filter(pk=idLinea).first()
    if linea:
         pedido = Pedidos.objects.get(pk=linea.pedido_id)
         idc = pedido.camarero_id;
         uid = linea.infmesa.uid;

         linea.infmesa.modifiar_composicion(linea)
         
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
            Sync.actualizar(Mesasabiertas._meta.db_table)
         

    return HttpResponse('success')


@csrf_exempt
def cuenta_add(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    lineas = json.loads(request.POST["pedido"])
    Pedidos.agregar_nuevas_lineas(idm, idc, lineas)
    return HttpResponse('success')

@csrf_exempt
def cuenta_cobrar(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    entrega = request.POST["entrega"]
    
    art = json.loads(request.POST["art"])
    total, id = Ticket.cerrar_cuenta(idm, idc, entrega, art)
    
            
    if (id > 0):
        send_imprimir_ticket(request, id)
        
    return JsonResponse({"totalcobro": str(total), "entrega": entrega})

@csrf_exempt
def cuenta_rm(request):
    idm = request.POST["idm"]
    motivo = request.POST["motivo"]
    idc = request.POST["idc"]
    Mesasabiertas.borrar_mesa_abierta(idm,idc,motivo)
    return HttpResponse('success')

@csrf_exempt
def cuenta_rm_linea(request):
    idm = request.POST["idm"]
    p = request.POST["Precio"]
    idArt = request.POST["idArt"]
    can = int(request.POST["can"])
    idc = request.POST["idc"]
    motivo = request.POST["motivo"]
    s = request.POST["Estado"]
    n = request.POST["Descripcion"]
    Lineaspedido.borrar_linea_pedido(idm, p, idArt, can, idc, motivo, s, n)
    return HttpResponse('success')


@csrf_exempt
def cuenta_ls_ticket(request):
    offset = request.POST['offset'] if 'offset' in request.POST else 0

    rows = Ticket.objects.all().order_by("-id")[offset:100]
    
    lineas = []
    
    for r in rows:
        total = r.ticketlineas_set.aggregate(total=Sum("linea__precio"))["total"]
        lineas.append({
            'ID': r.id,
            'Fecha': r.fecha,
            'Hora': r.hora,
            'Entrega': r.entrega,
            'Mesa': r.mesa,
            'Total': total,
            })

    return JsonResponse(lineas)

@csrf_exempt
def cuenta_ls_linea(request):
    id = request.POST["id"]
    ticket = Ticket.objects.get(pk=id)
    rows = ticket.ticketlineas_set.values("linea__idart",  
                                          "linea__descripcion_t",
                                          "linea__precio").annotate(can=Count('linea__idart'),
                                                                    total=Sum("linea__precio"))

    lineas = []
    for r in rows:
        lineas.append({
            "idart": r["linea__idart"],
            "Nombre": r["linea__descripcion_t"],
            "Precio": r["linea__precio"],
            "Total": r["total"],
            "Can": r["can"]
        })


    total = ticket.ticketlineas_set.aggregate(total=Sum("linea__precio"))["total"]
    return JsonResponse({'lineas':lineas, 'total': total, 'IDTicket': id})