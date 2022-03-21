# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T15:08:41+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-01T02:14:29+02:00
# @License: Apache License v2.0


from django.db.models import Q
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.db import connection
from api_android.tools import (send_imprimir_ticket, send_update_ws,
                               get_descripcion_ticket)
from gestion.models import (Mesasabiertas, Lineaspedido, Pedidos,
                            Infmesa, Ticket, Ticketlineas, Historialnulos)
from datetime import datetime
from uuid import uuid4
import json


@csrf_exempt
def ticket(request):
    return getTicket(request, -1)

@csrf_exempt
def get_cuenta(request):
    id = request.POST['mesa_id']
    m_abierta = Mesasabiertas.objects.filter(mesa__pk=id).first()
    lstArt = []
    if m_abierta:
        for l in Lineaspedido.objects.filter(infmesa__pk=m_abierta.infmesa.pk, estado='P'):
            obj = {
                'ID': l.pk,
                'IDPedido': l.pedido_id,
                'UID': m_abierta.infmesa.pk,
                'IDArt': l.idart,
                'Estado': l.estado,
                'Precio': l.precio,
                'Nombre': l.nombre,
                'IDMesa': m_abierta.mesa.pk
            }
            lstArt.append(obj)

    return JsonResponse(lstArt)


@csrf_exempt
def juntarmesas(request):
    if request.POST["idp"] != request.POST["ids"]:
        mesaP = Mesasabiertas.objects.filter(mesa__pk=request.POST["idp"]).first()
        mesaS = Mesasabiertas.objects.filter(mesa__pk=request.POST["ids"]).first()
        if mesaS:
            infmesa = mesaS.infmesa
            pedidos = Pedidos.objects.filter(infmesa__pk=infmesa.pk)
            for pedido in pedidos:
                pedido.infmesa_id = mesaP.infmesa.pk
                pedido.save()
                for l in pedido.lineaspedido_set.all():
                    l.infmesa_id = mesaP.infmesa.pk
                    l.save()
            infmesa.delete()


    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "mesasabiertas",
       "receptor": "comandas",
    }
    send_update_ws(request, update)


    return HttpResponse('success')

@csrf_exempt
def cambiarmesas(request):
    if request.POST["idp"] != request.POST["ids"]:
        mesaP = Mesasabiertas.objects.filter(mesa__pk=request.POST["idp"]).first()
        mesaS = Mesasabiertas.objects.filter(mesa__pk=request.POST["ids"]).first()
        if mesaS:
            infmesa = mesaS.infmesa
            mesaS.infmesa = mesaP.infmesa
            mesaS.save()
            mesaP.infmesa = infmesa;
            mesaP.save()
        else:
            mesaP.mesa_id = request.POST['ids']
            mesaP.save()

    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "mesasabiertas",
       "receptor": "comandas",
    }
    send_update_ws(request, update)

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

        #enviar notficacion de update
         update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
         }
         send_update_ws(request, update)

    return HttpResponse('success')

@csrf_exempt
def ls_aparcadas(request):
    mesas = Mesasabiertas.objects.all()
    lstArt = []
    for m in mesas:
        for l in Lineaspedido.objects.filter(infmesa__pk=m.infmesa.pk, estado='P'):
            obj = {
                'ID': l.pk,
                'IDPedido': l.pedido_id,
                'UID': m.infmesa.pk,
                'IDArt': l.idart,
                'Estado': l.estado,
                'Precio': l.precio,
                'Nombre': l.nombre,
                'IDMesa': m.mesa.pk
            }
            lstArt.append(obj)
    return JsonResponse(lstArt)

@csrf_exempt
def cuenta_aparcar(request):
    idm= request.POST["idm"]
    mesa = MesasAbiertas.objects.filter(infmesa__mesa__pk=idm).first()
    if mesa:
        uid = mesa.infmesa.pk
        Lineaspedido.objects.filter(uid=uid, estado='N').update(estado='P')
        return HttpResponse('success')

@csrf_exempt
def cuenta_add(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    lineas = json.loads(request.POST["pedido"])
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    if not mesa:
        infmesa = Infmesa()
        infmesa.ref = ""
        infmesa.camarero_id = idc
        infmesa.hora = datetime.now().strftime("%H:%M")
        infmesa.fecha = datetime.now().strftime("%Y/%m/%d")
        infmesa.uid = idm + '-' + str(uuid4())
        infmesa.save()

        mesa = Mesasabiertas()
        mesa.mesa_id = idm
        mesa.infmesa_id = infmesa.pk
        mesa.save()

        #enviar notficacion de update
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(request, update)

    pedido = Pedidos()
    pedido.infmesa_id = mesa.infmesa.pk
    pedido.hora = datetime.now().strftime("%H:%M")
    pedido.camarero_id = idc
    pedido.save()

    for pd in lineas:
        can = int(pd["Can"])
        for i in range(0, can):
            linea = Lineaspedido()
            linea.infmesa_id = mesa.infmesa.pk
            linea.idart = pd["IDArt"]
            linea.pedido_id = pedido.pk
            linea.nombre = pd["Nombre"]
            linea.precio = pd["Precio"]
            linea.estado = 'R' if pd['Precio'] == 0 else 'P'
            linea.save()

    return HttpResponse('success')

@csrf_exempt
def cuenta_cobrar(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    entrega = request.POST["entrega"]
    art = json.loads(request.POST["art"])
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    total = 0

    if mesa:
        uid = mesa.infmesa.pk
        ticket = Ticket()
        ticket.hora = datetime.now().strftime("%H:%M")
        ticket.fecha = datetime.now().strftime("%Y/%m/%d")
        ticket.camarero_id = idc
        ticket.uid = uid
        ticket.entrega = entrega
        ticket.mesa = mesa.mesa.nombre
        ticket.save()


        for l in art:
            can = int(l["Can"])
            reg = Lineaspedido.objects.filter(Q(infmesa__pk=uid) & Q(idart=l["IDArt"]) &
                                              Q(precio=l["Precio"]) &
                                              (Q(estado='P') | Q(estado='N')))[:can]

            for r in reg:
                total = total + r.precio
                linea = Ticketlineas()
                linea.ticket_id = ticket.pk
                linea.linea_id = r.pk
                linea.save()
                r.estado = 'C'
                r.save()

        numart = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='N')) & Q(infmesa__pk=uid)).count()
        if (numart <= 0):
            #enviar notficacion de update
            update = {
               "OP": "UPDATE",
               "Tabla": "mesasabiertas",
               "receptor": "comandas",
            }
            send_update_ws(request, update)
            Mesasabiertas.objects.filter(infmesa__pk=uid).delete()

    send_imprimir_ticket(request, ticket.id)
    return JsonResponse({"totalcobro": str(total), "entrega": entrega})

@csrf_exempt
def cuenta_rm(request):
    idm = request.POST["idm"]
    motivo = request.POST["motivo"]
    idc = request.POST["idc"]
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    if mesa:
        uid = mesa.infmesa.pk
        reg = Lineaspedido.objects.filter(Q(infmesa__pk=uid) & (Q(estado='R') |
                                          Q(estado='P') |
                                          Q(estado='N')))
        for r in reg:
            historial = Historialnulos()
            historial.lineapedido_id = r.pk
            historial.camarero_id = idc
            historial.motivo = motivo
            historial.hora = datetime.now().strftime("%H:%M")
            historial.save()
            r.estado = 'A'
            r.save()

        mesa.delete()

    #enviar notficacion de update
    update = {
       "OP": "UPDATE",
       "Tabla": "mesasabiertas",
       "receptor": "comandas",
    }
    send_update_ws(request, update)

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
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    if mesa:
        uid = mesa.infmesa.pk
        reg = Lineaspedido.objects.filter(infmesa__pk=uid, idart=idArt, estado=s, precio=p, nombre=n)[:can]

        for r in reg:
            if motivo != 'null':
                h = Historialnulos()
                h.lineapedido_id = r.id
                h.camarero_id = idc
                h.motivo = motivo
                h.hora = datetime.now().strftime("%H:%M")
                h.save()
                r.estado = 'A'
                r.save()
            else:
                r.delete()

    numart = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='N')) & Q(infmesa__pk=uid)).count()
    if (numart <= 0):
        #enviar notficacion de update
        update = {
           "OP": "UPDATE",
           "Tabla": "mesasabiertas",
           "receptor": "comandas",
        }
        send_update_ws(request, update)
        Mesasabiertas.objects.filter(infmesa__pk=uid).delete()
    return HttpResponse('success')

@csrf_exempt
def cuenta_ls_ticket(request):
    offset = request.POST['offset'] if 'offset' in request.POST else 0
    sql_pedido = ''.join([ 'SELECT ticket.ID, Fecha, Hora, Entrega, Mesa, SUM( Precio ) AS Total ',
                           'FROM ticket ',
                           'INNER JOIN ticketlineas ON ticket.ID=ticketlineas.IDTicket ',
                           'INNER JOIN lineaspedido ON lineaspedido.ID=ticketlineas.IDLinea ',
                           "GROUP BY ticket.ID ",
                           "ORDER BY ticket.ID DESC ",
                           "LIMIT {0}, {1} ",]).format(offset, 100)


    lineas = []
    with connection.cursor() as cursor:
        cursor.execute(sql_pedido)
        rows = cursor.fetchall()
        for r in rows:
            lineas.append({
               'ID': r[0],
               'Fecha': r[1],
               'Hora': r[2],
               'Entrega': r[3],
               'Mesa': r[4],
               'Total': r[5],
               })

    return JsonResponse(lineas)

@csrf_exempt
def cuenta_ls_linea(request):
    id = request.POST["id"]
    sql_pedido = ''.join(['SELECT COUNT(lineaspedido.IDArt) As Can, lineaspedido.IDArt,  lineaspedido.Precio, ',
                          'SUM( Precio ) AS Total, ',
                          'CASE lineaspedido.IDArt ',
                             'WHEN  0 THEN lineaspedido.Nombre ',
                             'ELSE teclas.Nombre ',
                           'END AS Nombre ',
                           'FROM lineaspedido ',
                           'LEFT JOIN teclas ON lineaspedido.IDArt=teclas.ID ',
                           "INNER JOIN ticketlineas ON lineaspedido.ID=ticketlineas.IDLinea ",
                           "INNER JOIN ticket ON ticket.ID=ticketlineas.IDTicket ",
                           "WHERE ticket.ID={0} ",
                           "GROUP BY lineaspedido.IDArt, lineaspedido.Nombre, lineaspedido.Precio ",
                           ]).format(id)

    lineas = []
    with connection.cursor() as cursor:
        cursor.execute(sql_pedido)
        rows = cursor.fetchall()
        for r in rows:
            lineas.append({
               'Can': r[0],
               'IDArt': r[1],
               'Precio': r[2],
               'Total': r[3],
               'Nombre': get_descripcion_ticket(r[1], r[4]),
               })



    sql_total = ''.join(['SELECT SUM( Precio ) AS Total ',
                         'FROM ticket ',
                         "INNER JOIN ticketlineas ON ticket.ID=ticketlineas.IDTicket ",
                         "INNER JOIN lineaspedido ON lineaspedido.ID=ticketlineas.IDLinea ",
                         "WHERE ticket.ID={0} "]).format(id)

    total = 0

    with connection.cursor() as cursor:
        cursor.execute(sql_total)
        row = cursor.fetchone()
        total = row[0]

    return JsonResponse({'lineas':lineas, 'total': total, 'IDTicket': id})

def getTicket(request, IDPedido):
    lstObj = {}
    idm = request.POST["idm"];
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    if mesa:
        uid = mesa.infmesa.uid
        sql_pedido = ''.join([ 'SELECT Precio, Estado, IDArt, COUNT(IDArt) as Can, (Precio * COUNT(IDArt)) as Total, Nombre ',
                               'FROM lineaspedido ',
                               " WHERE (Estado='P' OR Estado='N') AND UID='{0}' "
                               "GROUP BY IDArt, Precio, Nombre, Estado ",
                               "ORDER BY Estado, IDArt ",]).format(uid)

        lineas = []
        with connection.cursor() as cursor:
            cursor.execute(sql_pedido)
            rows = cursor.fetchall()
            for r in rows:

                lineas.append({
                   'Precio': r[0],
                   'Estado': r[1],
                   'IDArt': r[2],
                   'Can': r[3],
                   'Total': r[4],
                   'Nombre': get_descripcion_ticket(r[2],r[5]),
                   })



        sql_total = ''.join([' SELECT SUM(Precio) as ticket',
                             ' FROM lineaspedido',
                             " WHERE (Estado='P' OR Estado='N') AND UID='{0}'"]).format(uid)

        total = 0

        with connection.cursor() as cursor:
            cursor.execute(sql_total)
            row = cursor.fetchone()
            total = row[0]

        lstObj  = {
          "lineas": lineas,
          "total": total,
          "pedido": IDPedido
        }

    return JsonResponse(lstObj)
