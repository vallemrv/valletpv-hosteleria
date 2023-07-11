from django.db.models import F, Q, Sum, Count, Value, FloatField, CharField
from django.db.models.functions import Concat, Cast
from tokenapi.decorators import token_required
from tokenapi.http import JsonResponse, JsonError
from valle_tpv.models import (Cierrecaja, Infmesa, Pedidos,
                            Lineaspedido, Camareros, Teclas)
from datetime import datetime, time, timedelta, timezone


@token_required
def articulos_vendidos(request):
    # Obtener el mes y año actual
    today = datetime.today()
    current_month = today.month
    current_year = today.year

    # Filtrar por mes y año actual y estado "C"
    ventas = Lineaspedido.objects.filter(
        infmesa_id__fecha__month=current_month,
        infmesa_id__fecha__year=current_year,
        estado="C",
    ).values("tecla_id")

    # Si no hay ventas en este mes, devolver un error
    if not ventas:
        return JsonError("No hay ventas en este mes")

    # Contar y agrupar por idart
    ventas = ventas.annotate(can=Count("tecla_id")).order_by("-can")

    # Limitar los resultados a 25 y obtener nombres de los artículos
    resultado = []
    for venta in ventas[:25]:
        tecla = Teclas.objects.filter(pk=venta["tecla_id"]).first()
        if tecla:
            resultado.append({"can": venta["can"], "nombre": tecla.nombre})

    return JsonResponse(resultado)





@token_required
def ventas_por_intervalos(request):
    resultado = []
    try:
        date_param = request.POST.get("date")
        estado = request.POST.get("estado", "C")
        if date_param:
            selected_date = datetime.strptime(date_param, "%Y/%m/%d").date()
        else:
            selected_date = timezone.now().date() - timedelta(days=1)

        time_ranges = [
            (time(8, 0), time(13, 0)),
            (time(13, 1), time(17, 0)),
            (time(17, 1), time(20, 0)),
            (time(20, 1), time(23, 59)),
        ]

        

        for start, end in time_ranges:
            fecha_str = selected_date
            hora_start_str = start.strftime("%H:%M")
            hora_end_str = end.strftime("%H:%M")

            ventas = Lineaspedido.objects.filter(
                pedido_id__infmesa__fecha=fecha_str,
                pedido_id__infmesa__hora__range=(hora_start_str, hora_end_str),
                estado=estado
            ).annotate(
                can=Count("tecla_id"), sub=(F("can")*F("precio"))
            ).aggregate(
                total=Sum("sub")
            )["total"]

            if ventas is None:
                ventas = 0

            resultado.append({"inicio": hora_start_str, "fin": hora_end_str, "ventas": ventas})
    except Exception as e:
        return JsonError(str(e))

    return JsonResponse(resultado)

@token_required
def get_estado_ventas_by_cam(request):
    # Obtener la fecha y hora del último cierre de caja
    resultado = []
    try:
        try:
            ultimo_cierre = Cierrecaja.objects.latest('pk')
            dt_ultimo_cierre = ultimo_cierre.fecha.strftime("%Y-%m-%d") +" "+ ultimo_cierre.hora
        except:
            return JsonError("No hay cierres de caja")
        
        
        
        # Filtrar las Infmesa que cumplan con la condición
        infmesas = Infmesa.objects.annotate(
            datetime_fecha_hora=Concat(
                Cast(F('fecha'), CharField()), Value(' '), F('hora'))
        ).filter(
            datetime_fecha_hora__gt=dt_ultimo_cierre
        )

        # Obtener la lista de camareros
        camareros = Camareros.objects.filter(activo=1)

    
        # Realizar la consulta para cada camarero
        for camarero in camareros:
            pedidos_camarero = Pedidos.objects.filter(
                camarero_id=camarero.pk,
                infmesa_id__in=infmesas
            )
            
            total_vendido = Lineaspedido.objects.filter(
            Q(pedido_id__in=pedidos_camarero) & (Q(estado='P') | Q(estado='C'))
            ).annotate(
                can=Count('tecla_id'),
                subtotal=F('can') * F('precio')
            ).aggregate(total=Sum('subtotal'))['total'] or 0
            
            if total_vendido > 0:
                resultado.append({
                    "nombre": camarero.nombre,
                    "total_vendido": total_vendido
                })
    except Exception as e:
        return JsonError(str(e))
    
    return JsonResponse(resultado)

@token_required
def get_estado_ventas(request):
    resultado = resultado = {
        "cobrado": 0,
        "pedido": 0,
        "borrado": 0
    }

    try:
        try:
            ultimo_cierre = Cierrecaja.objects.latest('pk')
            con_fecha_hora = ultimo_cierre.fecha.strftime("%Y-%m-%d") +" "+ ultimo_cierre.hora
        except:
            return JsonError("No hay cierres de caja")
        

        # Filtrar las Infmesa que cumplan con la condición
        infmesas = Infmesa.objects.annotate(
            datetime_fecha_hora=(Concat(
                Cast(F('fecha'), CharField()), Value(' '), F('hora')))
        ).filter(
            datetime_fecha_hora__gt=con_fecha_hora
        )

        # Filtrar las Lineaspedido que cumplan con la condición
        lineas_pedido = Lineaspedido.objects.filter(
            infmesa_id__in=infmesas
        )

        # Calcular la suma total para cada estado
        suma_total_c = lineas_pedido.filter(estado='C').annotate(
            can=Count('tecla_id'), sub_total=F('can') * F('precio')
        ).aggregate(total=Sum('sub_total', output_field=FloatField()))['total'] or 0

        suma_total_p = lineas_pedido.filter(estado='P').annotate(
            can=Count('tecla_id'), sub_total=F('can') * F('precio')
        ).aggregate(total=Sum('sub_total', output_field=FloatField()))['total'] or 0

        suma_total_n = lineas_pedido.filter(estado='A').annotate(
            can=Count('tecla_id'), sub_total=F('can') * F('precio')
        ).aggregate(total=Sum('sub_total', output_field=FloatField()))['total'] or 0


        # Crear un JSON con los resultados
        resultado = {
            "cobrado": suma_total_c,
            "pedido": suma_total_p,
            "borrado": suma_total_n
        }
        
    except Exception as e:
        return JsonError(str(e))

    return JsonResponse(resultado)
