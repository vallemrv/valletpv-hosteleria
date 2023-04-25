from django.db.models import F, Q, Sum, Count, Value, FloatField
from django.db.models.functions import Concat
from tokenapi.decorators import token_required
from tokenapi.http import JsonResponse
from gestion.models import (Cierrecaja, Infmesa, Pedidos,
                            Lineaspedido, Camareros)

@token_required
def get_estado_ventas_by_cam(request):
    # Obtener la fecha y hora del último cierre de caja
    ultimo_cierre = Cierrecaja.objects.latest('pk')
    dt_ultimo_cierre = ultimo_cierre.fecha +" "+ ultimo_cierre.hora

    # Filtrar las Infmesa que cumplan con la condición
    infmesas = Infmesa.objects.annotate(
        datetime_fecha_hora=Concat(
            F('fecha'), Value(' '), F('hora'))
    ).filter(
        datetime_fecha_hora__gt=dt_ultimo_cierre
    )

    # Obtener la lista de camareros
    camareros = Camareros.objects.filter(activo=1)

    resultado = []

    # Realizar la consulta para cada camarero
    for camarero in camareros:
        pedidos_camarero = Pedidos.objects.filter(
            camarero_id=camarero.pk,
            infmesa_id__in=infmesas
        )
        
        total_vendido = Lineaspedido.objects.filter(
           Q(pedido_id__in=pedidos_camarero) & (Q(estado='P') | Q(estado='C'))
        ).annotate(
            can=Count('idart'),
            subtotal=F('can') * F('precio')
        ).aggregate(total=Sum('subtotal'))['total'] or 0
        
        if total_vendido > 0:
            resultado.append({
                "nombre": camarero.nombre,
                "total_vendido": total_vendido
            })
    
  

    return JsonResponse(resultado)

@token_required
def get_estado_ventas(request):
    # Obtener la fecha y hora del último cierre de caja
    ultimo_cierre = Cierrecaja.objects.latest('pk')
    con_fecha_hora = ultimo_cierre.fecha +" "+ ultimo_cierre.hora


    # Filtrar las Infmesa que cumplan con la condición
    infmesas = Infmesa.objects.annotate(
        datetime_fecha_hora=(Concat(
            F('fecha'), Value(' '), F('hora')))
    ).filter(
        datetime_fecha_hora__gt=con_fecha_hora
    )

    # Filtrar las Lineaspedido que cumplan con la condición
    lineas_pedido = Lineaspedido.objects.filter(
        infmesa_id__in=infmesas
    )

    # Calcular la suma total para cada estado
    suma_total_c = lineas_pedido.filter(estado='C').annotate(
        can=Count('idart'), sub_total=F('can') * F('precio')
    ).aggregate(total=Sum('sub_total', output_field=FloatField()))['total'] or 0

    suma_total_p = lineas_pedido.filter(estado='P').annotate(
        can=Count('idart'), sub_total=F('can') * F('precio')
    ).aggregate(total=Sum('sub_total', output_field=FloatField()))['total'] or 0

    suma_total_n = lineas_pedido.filter(estado='N').annotate(
        can=Count('idart'), sub_total=F('can') * F('precio')
    ).aggregate(total=Sum('sub_total', output_field=FloatField()))['total'] or 0


    # Crear un JSON con los resultados
    resultado = {
        "cobrado": suma_total_c,
        "pedido": suma_total_p,
        "borrado": suma_total_n
    }

    return JsonResponse(resultado)
