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
from django.db import connection
from api_android.tools import (send_imprimir_ticket, send_update_ws)
from gestion.models import (Mesasabiertas, Lineaspedido, Pedidos, 
                            Infmesa, Servidos, Sync, Ticket, Ticketlineas, Historialnulos)
from datetime import datetime
from uuid import uuid4
import json



@csrf_exempt
def get_cuenta(request):
    id = request.POST['mesa_id']
    m_abierta = Mesasabiertas.objects.filter(mesa__pk=id).first()
    lstArt = []
    if m_abierta:
        lstArt = m_abierta.get_lineaspedido()

    return JsonResponse(lstArt)

@csrf_exempt
def juntarmesas(request):
    Mesasabiertas.juntar_mesas_abiertas( request.POST["idp"], request.POST["ids"])


    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "mesasabiertas",
       "receptor": "comandas",
    }
    send_update_ws(update)


    return HttpResponse('success')

@csrf_exempt
def cambiarmesas(request):
    Mesasabiertas.cambiar_mesas_abiertas(request.POST["idp"], request.POST["ids"])

    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "mesasabiertas",
       "receptor": "comandas",
    }
    send_update_ws(update)

    return HttpResponse('success')

@csrf_exempt
def mvlinea(request):
    idm = request.POST["idm"];
    idLinea = request.POST["idLinea"];
    linea = Lineaspedido.objects.get(pk=idLinea)
    if linea:
         pedido = Pedidos.objects.get(pk=linea.pedido_id)
         idc = pedido.camarero_id;
         uid = linea.infmesa.uid;
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


         pedido = Pedidos()
         pedido.infmesa_id = mesa.infmesa.pk
         pedido.hora = datetime.now().strftime("%H:%M")
         pedido.camarero_id = idc
         pedido.save()

         linea.infmesa_id =  mesa.infmesa.pk
         linea.pedido_id = pedido.pk
         linea.save()

         numart = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='R')) & Q(infmesa__uid=uid)).count()
         if numart<=0:
            Mesasabiertas.objects.filter(infmesa__uid=uid).delete()
            Sync.actualizar(Mesasabiertas._meta.db_table)
            

        #enviar notficacion de update
         update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
         }
         send_update_ws(update)

    return HttpResponse('success')


@csrf_exempt
def cuenta_add(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    lineas = json.loads(request.POST["pedido"])
    is_updatable = Pedidos.agregar_nuevas_lineas(idm,idc,lineas)
    
    if is_updatable:
        #enviar notficacion de update
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(update)

    

    return HttpResponse('success')

@csrf_exempt
def cuenta_cobrar(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    entrega = request.POST["entrega"]
    
    art = json.loads(request.POST["art"])
   

    numart, total, id = Ticket.cerrar_cuenta(idm, idc, entrega, art)
    
    if (numart <= 0):
        #enviar notficacion de update
        update = {
            "OP": "UPDATE",
            "Tabla": "mesasabiertas",
            "receptor": "comandas",
        }
        send_update_ws(update)
            
    if (id > 0):
        send_imprimir_ticket(request, id)
        
    return JsonResponse({"totalcobro": str(total), "entrega": entrega})

@csrf_exempt
def cuenta_rm(request):
    idm = request.POST["idm"]
    motivo = request.POST["motivo"]
    idc = request.POST["idc"]
    Mesasabiertas.borrar_mesa_abierta(idm,idc,motivo)

    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "mesasabiertas",
       "receptor": "comandas",
    }
    send_update_ws(update)

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
    n = request.POST["Nombre"]
    
    
    numart = Lineaspedido.borrar_linea_pedido(idm, p, idArt, can, idc, motivo, s, n)

    if (numart <= 0):
        #enviar notficacion de update
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(update)
        

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