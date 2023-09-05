# Version: 0.1

from tokenapi.http import JsonResponse
from django.db.models import  Sum, Count
from valle_tpv.decorators import check_dispositivo
from valle_tpv.models import (Ticket)
import json


@check_dispositivo
def ticket_lista(request):
    offset = int(request.POST['offset']) if 'offset' in request.POST else 0

    rows = Ticket.objects.all().order_by("-id")[offset:10]
    
    lineas = []
    
    for r in rows:
        total = r.ticketlineas_set.aggregate(total=Sum("linea__precio"))["total"]
        
        lineas.append({
            'id': r.id,
            'fecha': r.fecha.strftime("%d/%m/%Y"),
            'hora': r.hora,
            'entrega': float(r.entrega),
            'nomMesa': r.mesa,
            'total': float(total),
            'camarero': r.camarero.nombre+ " " + r.camarero.apellidos,
            })
        
   
    return JsonResponse({'lineas':json.dumps(lineas)})

@check_dispositivo
def ticket_lista_lineas(request):
    id = request.POST["id"]
    ticket = Ticket.objects.get(pk=id)
    rows = ticket.ticketlineas_set.values("linea__tecla_id",  
                                          "linea__descripcion_t",
                                          "linea__precio").annotate(can=Count('linea__tecla_id'),
                                                                    total=Sum("linea__precio"))

    lineas = []
    for r in rows:
        lineas.append({
            "tecla_id": r["linea__tecla_id"],
            "descripcion": r["linea__descripcion_t"],
            "precio": float(r["linea__precio"]),
            "total": float(r["total"]),
            "cantidad": r["can"]
        })

    return JsonResponse({'lineas':json.dumps(lineas)})
