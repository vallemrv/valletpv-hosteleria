from .utils import get_total_by_horas, get_total
from django.db.models import F, Sum, Count

from tokenapi.http import JsonResponse
from gestion.models import (Arqueocaja, Camareros, Historialnulos,
                            Lineaspedido, 
                            Infmesa, Mesas, Ticket, Cierrecaja, Ticketlineas)
from tokenapi.decorators import token_required
from gestion.models import Mesasabiertas

@token_required
def get_estado_ventas(request):
    ultimo_ticket = Cierrecaja.objects.latest("pk").ticketfinal

    # Obtener los tickets cuyo PK es mayor que el valor de ticketfinal del último cierre de caja
    tickets_no_cerrados = Ticket.objects.filter(pk__gt=ultimo_ticket)

    # Obtener todas las Ticketlinea de los tickets no cerrados
    ticketlineas_no_cerrados = Ticketlineas.objects.filter(ticket__id__in=tickets_no_cerrados)
    linea_ids = ticketlineas_no_cerrados.values_list('id', flat=True)


    # Obtener todas las Lineaspedido de las Ticketlinea no cerradas y agregar columnas extra
    lineaspedido_no_cerrados = Lineaspedido.objects.filter(pk__in=linea_ids).annotate(
        can=Count('tecla_id'),
        total=F('cantidad') * F('precio')
    )
    suma_total_c = lineaspedido_no_cerrados.filter(estado="C").aggregate(Sum('total'))['total__sum'] or 0
    suma_total_p = lineaspedido_no_cerrados.filter(estado="P").aggregate(Sum('total'))['total__sum'] or 0
    suma_total_n = lineaspedido_no_cerrados.filter(estado="N").aggregate(Sum('total'))['total__sum'] or 0

    # Crear un JSON con los resultados
    resultados_json = {
        "cobrado": suma_total_c,
        "pedido": suma_total_p,
        "borrado": suma_total_n
    }
    

    return JsonResponse(resultados_json)


@token_required
def get_pedidos_by_hora(request):
    last_id = Ticket.get_last_id_linea()
    res = []   
    res.append({"pedido": get_total_by_horas({'estado':'P'})})
    res.append({"cobrado": get_total_by_horas({'id__gt':last_id, 'estado':'C'})})
    res.append({"borrado":get_total_by_horas({'id__gt':last_id, 'estado':'A'})})
    return JsonResponse(res)

@token_required
def datasets(request):
    last_id = Ticket.get_last_id_linea()
    return JsonResponse( get_total("estado", {'id__gt':last_id}))


@token_required
def cuenta_rm(request):
    idm = request.POST["idm"]
    motivo = request.POST["motivo"]
    idc = Camareros.objects.first().id
    Mesasabiertas.borrar_mesa_abierta(idm,idc,motivo)
    return JsonResponse({})

@token_required
def send_cobrar_mesa(request):
    pk = request.POST["pk"]
    idc = Camareros.objects.first().id
    entrega = request.POST["entrega"]
    mesa = Mesasabiertas.objects.filter(pk=pk).first()
    art = []
    for o in mesa.infmesa.lineaspedido_set.filter(estado='P'):
        obj = o.serialize()
        obj["Can"] = 1
        art.append(obj)

    Ticket.cerrar_cuenta(mesa.mesa.id, idc, entrega, art)
   
    return JsonResponse({})

@token_required
def get_infomesa(request):
    pk = request.POST["pk"]
    infmesa = None
    if "-" not in pk:
        m = Mesasabiertas.objects.filter(id=pk).first()
        if m: infmesa = m.infmesa
    else:
        infmesa = Infmesa.objects.filter(pk=pk).first()
        
    pedidos = []
    if infmesa:
        pedidos = infmesa.get_pedidos()
    return JsonResponse(pedidos)


@token_required
def get_nulos(request):
    nulos = Historialnulos.objects.all()[:200]
    objs = []
    obj = None
    linea = None
    for n in nulos:

        if (not obj or obj["PK"] !=  n.lineapedido.infmesa_id):
            split = n.lineapedido.infmesa_id.split('-')
            mesa = Mesas.objects.filter(pk=split[0]).first()
            nomMesa = mesa.nombre if mesa else ''
            camarero = n.lineapedido.infmesa.camarero
            obj = {
                "PK": n.lineapedido.infmesa_id,
                "nomMesa":nomMesa,
                "lineas": [],
                "hora": n.lineapedido.infmesa.hora,
                "camarero": camarero.nombre + " " + camarero.apellidos
                }
            objs.append(obj)
            linea = None   

        if (linea and linea["descripcion"] == n.lineapedido.descripcion and
                      linea["precio"] == n.lineapedido.precio):
            linea["can"] += 1
        else: 
            linea = {
                "descripcion": n.lineapedido.descripcion,
                "hora": n.hora,
                "motivo": n.motivo,
                "precio": n.lineapedido.precio,
                "can": 1
            }
            obj["lineas"].append(linea)
        
    return JsonResponse(objs)

@token_required
def get_list_mesas(request):
    lista = Infmesa.objects.all()[:50]
    r = []
    for l in lista:
        r.append(l.serialize())
    return JsonResponse(r)

@token_required
def get_list_arqueos(request):
    lista = Arqueocaja.objects.all()[1:2]
    r = []
    for l in lista:
        r.append(l.serialize())
    return JsonResponse(r)