from typing import List
from django.db import connection
from decimal import ROUND_HALF_UP, Decimal
from django.db.models import Sum, Count, F
from gestion.models.camareros import Camareros
from gestion.models.arqueos import Arqueocaja
from gestion.models.ticket import Ticket
from gestion.models.familias import Receptores
from gestion.tools.config_logs import log_debug_chatbot as logger
from langchain_core.tools import tool
from datetime import datetime
from comunicacion.tools import send_mensaje_impresora
from chatbot.utilidades.ws_sender import send_tool_message  # Añadir este import
from chatbot.decorators.db_connection_manager import db_connection_handler
import json

@tool
@db_connection_handler
def get_sales_by_waiters(date_start: str, date_end: str, start_time: str, end_time: str):
    """
    Obtiene las ventas por camarero en un rango de fechas y horas. Esta herramienta permite
    analizar el desempeño de todos los camareros que han registrado ventas (cobradas, anuladas o pendientes)
    en el período especificado, útil para reportes de productividad o auditorías.

    Args:
        date_start (str): Fecha de inicio en formato 'YYYY/MM/DD'.
        date_end (str): Fecha de fin en formato 'YYYY/MM/DD'.
        start_time (str): Hora de inicio en formato 'HH:MM'.
        end_time (str): Hora de fin en formato 'HH:MM'.

    Returns:
        list: Lista de diccionarios por camarero con:
              - nombre: Nombre del camarero.
              - apellido: Apellido del camarero.
              - cantidad_c: Total cobrado (redondeado a 2 decimales).
              - cantidad_a: Total anulado (redondeado a 2 decimales).
              - cantidad_p: Total pendiente (redondeado a 2 decimales).
              Retorna lista vacía si no hay datos, o un dict con error.
    """
    send_tool_message(f"Obteniendo ventas por camareros desde {date_start} {start_time} hasta {date_end} {end_time}...")
    try:
        # 1. Validar formato de entrada
        try:
            datetime.strptime(date_start, "%Y/%m/%d")
            datetime.strptime(date_end, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            if date_start > date_end:
                logger.warning(f"date_start ({date_start}) posterior a date_end ({date_end})")
                return {"error": "La fecha de inicio debe ser anterior o igual a la fecha de fin."}
            if date_start == date_end and start_time > end_time:
                logger.warning(f"start_time ({start_time}) posterior a end_time ({end_time})")
                return {"error": "La hora de inicio debe ser anterior o igual a la hora de fin en la misma fecha."}
        except ValueError:
            logger.warning(f"Formato de fecha/hora inválido: {date_start}, {date_end}, {start_time}, {end_time}")
            return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # 2. Consulta SQL
        sql = """
            SELECT
                p.IDCam AS waiter_id,
                lp.estado AS status,
                SUM(lp.precio) AS total_sales
            FROM
                lineaspedido lp
            INNER JOIN
                infmesa im ON lp.UID = im.UID
            INNER JOIN
                pedidos p ON lp.IDPedido = p.id
            WHERE
                im.fecha BETWEEN %s AND %s
                AND p.hora BETWEEN %s AND %s
                AND p.IDCam IS NOT NULL
            GROUP BY
                p.IDCam, lp.estado
            ORDER BY
                p.IDCam
        """

        # 3. Ejecutar consulta
        with connection.cursor() as cursor:
            params = [date_start, date_end, start_time, end_time]
            logger.debug(f"Ejecutando SQL con params: {params}")
            cursor.execute(sql, params)
            rows = cursor.fetchall()
            logger.debug(f"SQL devolvió {len(rows)} filas.")

        # 4. Procesar resultados
        camareros_data = {}
        for row in rows:
            camarero_id = row[0]
            estado = row[1]
            total_sales = float(row[2]) if row[2] is not None else 0.0

            if camarero_id not in camareros_data:
                camareros_data[camarero_id] = {
                    "cantidad_c": 0.0,
                    "cantidad_a": 0.0,
                    "cantidad_p": 0.0
                }

            if estado == 'C':
                camareros_data[camarero_id]["cantidad_c"] += total_sales
            elif estado == 'A':
                camareros_data[camarero_id]["cantidad_a"] += total_sales
            elif estado == 'P':
                camareros_data[camarero_id]["cantidad_p"] += total_sales
            else:
                logger.warning(f"Estado desconocido '{estado}' para camarero {camarero_id}")

        if not camareros_data:
            logger.info(f"No se encontraron ventas para {date_start} a {date_end}, {start_time}-{end_time}.")
            return []

        # 5. Obtener nombres de camareros
        camarero_ids = list(camareros_data.keys())
        camareros_info = Camareros.objects.filter(id__in=camarero_ids).values('id', 'nombre', 'apellidos')
        camareros_dict = {c['id']: c for c in camareros_info}

        # 6. Construir resultado final
        formatted_results = []
        for camarero_id, counts in camareros_data.items():
            camarero_info = camareros_dict.get(camarero_id)
            if camarero_info:
                nombre = camarero_info.get('nombre', '')
                apellido = camarero_info.get('apellidos', '')
            else:
                nombre = f"Camarero ID {camarero_id}"
                apellido = "(Desconocido)"

            formatted_results.append({
                "nombre": nombre,
                "apellido": apellido,
                "cantidad_c": round(counts["cantidad_c"], 2),
                "cantidad_a": round(counts["cantidad_a"], 2),
                "cantidad_p": round(counts["cantidad_p"], 2)
            })

        # Ordenar por apellido y nombre
        formatted_results.sort(key=lambda x: (x['apellido'], x['nombre']))
        return formatted_results

    except Exception as e:
        logger.error(f"Error en get_sales_by_waiters: {e}", exc_info=True)
        return {"error": "Error interno del servidor al obtener ventas por camarero."}

@tool
@db_connection_handler
def get_sales_by_waiter_id(waiter_id: int, date_start: str, date_end: str, start_time: str, end_time: str):
    """
    Obtiene las ventas detalladas (cobradas, anuladas, pendientes) realizadas por un camarero
    específico en un rango de fechas y horas. Esta herramienta es útil para analizar el
    desempeño individual de un camarero en un período determinado, mostrando el total de
    ventas procesadas.

    Args:
        waiter_id (int): ID del camarero.
        date_start (str): Fecha de inicio en formato 'YYYY/MM/DD'.
        date_end (str): Fecha de fin en formato 'YYYY/MM/DD'.
        start_time (str): Hora de inicio en formato 'HH:MM'.
        end_time (str): Hora de fin en formato 'HH:MM'.

    Returns:
        dict: Diccionario con:
              - nombre: Nombre del camarero.
              - apellido: Apellido del camarero.
              - cantidad_c: Total cobrado (redondeado a 2 decimales).
              - cantidad_a: Total anulado (redondeado a 2 decimales).
              - cantidad_p: Total pendiente (redondeado a 2 decimales).
              Retorna un dict de error si falla.
    """
    send_tool_message(f"Obteniendo ventas del camarero ID {waiter_id} desde {date_start} {start_time} hasta {date_end} {end_time}...")
    try:
        # 1. Validar waiter_id
        if not isinstance(waiter_id, int) or waiter_id <= 0:
            logger.warning(f"ID de camarero inválido: {waiter_id}")
            return {"error": "El ID del camarero debe ser un entero positivo."}

        # 2. Validar formato de entrada
        try:
            datetime.strptime(date_start, "%Y/%m/%d")
            datetime.strptime(date_end, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            if date_start > date_end:
                logger.warning(f"date_start ({date_start}) posterior a date_end ({date_end})")
                return {"error": "La fecha de inicio debe ser anterior o igual a la fecha de fin."}
            if date_start == date_end and start_time > end_time:
                logger.warning(f"start_time ({start_time}) posterior a end_time ({end_time})")
                return {"error": "La hora de inicio debe ser anterior o igual a la hora de fin en la misma fecha."}
        except ValueError:
            logger.warning(f"Formato de fecha/hora inválido: {date_start}, {date_end}, {start_time}, {end_time}")
            return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # 3. Consulta SQL
        sql = """
            SELECT
                lp.estado AS status,
                SUM(lp.precio) AS total_sales
            FROM
                lineaspedido lp
            INNER JOIN
                infmesa im ON lp.UID = im.UID
            INNER JOIN
                pedidos p ON lp.IDPedido = p.id
            WHERE
                p.IDCam = %s
                AND im.fecha BETWEEN %s AND %s
                AND p.hora BETWEEN %s AND %s
                AND p.IDCam IS NOT NULL
            GROUP BY
                lp.estado
        """
        params = [waiter_id, date_start, date_end, start_time, end_time]

        # 4. Ejecutar consulta
        with connection.cursor() as cursor:
            logger.debug(f"Ejecutando SQL con params: {params}")
            cursor.execute(sql, params)
            rows = cursor.fetchall()
            logger.debug(f"SQL devolvió {len(rows)} filas.")

        # 5. Procesar resultados
        sales_data = {
            "cantidad_c": 0.0,
            "cantidad_a": 0.0,
            "cantidad_p": 0.0
        }
        for row in rows:
            status = row[0]
            total_sales = float(row[1]) if row[1] is not None else 0.0
            if status == 'C':
                sales_data["cantidad_c"] += total_sales
            elif status == 'A':
                sales_data["cantidad_a"] += total_sales
            elif status == 'P':
                sales_data["cantidad_p"] += total_sales
            else:
                logger.warning(f"Estado desconocido '{status}' para camarero {waiter_id}")

        # 6. Obtener nombre y apellido
        nombre_camarero = f"Camarero ID {waiter_id}"
        apellido_camarero = "(Desconocido)"
        try:
            camarero = Camareros.objects.filter(id=waiter_id).values('nombre', 'apellidos').first()
            if camarero:
                nombre_camarero = camarero.get('nombre', nombre_camarero)
                apellido_camarero = camarero.get('apellidos', '')
        except Exception as e:
            logger.error(f"Error al consultar Camareros para ID {waiter_id}: {e}", exc_info=True)

        # 7. Construir resultado
        result = {
            "nombre": nombre_camarero,
            "apellido": apellido_camarero,
            "cantidad_c": round(sales_data["cantidad_c"], 2),
            "cantidad_a": round(sales_data["cantidad_a"], 2),
            "cantidad_p": round(sales_data["cantidad_p"], 2)
        }

        return result

    except Exception as e:
        logger.error(f"Error en get_sales_by_waiter: {e}", exc_info=True)
        return {"error": "Error interno del servidor al obtener ventas del camarero."}
    
@tool
@db_connection_handler
def get_top_selling_items(date_start: str, date_end: str, start_time: str, end_time: str, limit: int = 10):
    """
    Obtiene los artículos más vendidos en un rango de fechas y horas (usando SQL raw internamente),
    ordenados por número de veces que aparecen (cantidad de líneas) de mayor a menor.
    Cada línea representa un artículo vendido. Los precios se redondean a 2 decimales.

    Args:
        date_start (str): Fecha de inicio en formato 'YYYY/MM/DD'.
        date_end (str): Fecha de fin en formato 'YYYY/MM/DD'.
        start_time (str): Hora de inicio en formato 'HH:MM'.
        end_time (str): Hora de fin en formato 'HH:MM'.
        limit (int): Número máximo de artículos a devolver (por defecto 10).

    Returns:
        list: Lista de diccionarios con los artículos más vendidos
              (descripcion, cantidad, precio_total, precio_unidad).
              O un dict de error.
    """
    send_tool_message(f"Obteniendo los {limit} artículos más vendidos desde {date_start} {start_time} hasta {date_end} {end_time}...")
    try:
        # 1. Validar formato de fechas y horas
        try:
            datetime.strptime(date_start, "%Y/%m/%d")
            datetime.strptime(date_end, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            
            # Validar que date_start <= date_end
            if date_start > date_end:
                logger.warning(f"date_start ({date_start}) es posterior a date_end ({date_end})")
                return {"error": "La fecha de inicio debe ser anterior o igual a la fecha de fin."}
                
            # Validar que si las fechas son iguales, start_time <= end_time
            if date_start == date_end and start_time > end_time:
                logger.warning(f"start_time ({start_time}) es posterior a end_time ({end_time}) en la misma fecha")
                return {"error": "La hora de inicio debe ser anterior o igual a la hora de fin en la misma fecha."}
                
        except ValueError as ve:
            logger.warning(f"Error de formato detectado: {ve}")
            return {"error": "El formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # 2. Validar y asegurar límite
        if not isinstance(limit, int) or limit <= 0:
            logger.debug(f"Límite inválido ({limit}), usando valor por defecto 10.")
            limit = 10

        # 3. LÓGICA INTERNA CON SQL RAW
        raw_query = """
            SELECT
                lp.Descripcion AS descripcion,
                COUNT(tl.id) AS cantidad,
                SUM(lp.Precio) AS precio_total,
                CASE
                    WHEN COUNT(tl.id) > 0 THEN SUM(lp.Precio) / COUNT(tl.id)
                    ELSE 0.0
                END AS precio_unidad
            FROM
                ticketlineas tl
            INNER JOIN
                ticket t ON tl.IDTicket = t.id
            INNER JOIN
                lineaspedido lp ON tl.IDLinea = lp.id
            WHERE
                t.Fecha BETWEEN %s AND %s
                AND t.Hora BETWEEN %s AND %s
                AND lp.Descripcion IS NOT NULL
            GROUP BY
                lp.Descripcion
            HAVING
                COUNT(tl.id) > 0
            ORDER BY
                cantidad DESC
            LIMIT %s;
        """

        with connection.cursor() as cursor:
            params = [date_start, date_end, start_time, end_time, limit]
            logger.debug(f"Ejecutando SQL raw con params: {params}")
            cursor.execute(raw_query, params)
            rows = cursor.fetchall()
            logger.debug(f"SQL raw devolvió {len(rows)} filas.")

        # 4. Formatear resultados CON REDONDEO A 2 DECIMALES
        formatted_results = []
        for row in rows:
            descripcion = row[0]
            cantidad = row[1]
            precio_total_raw = row[2]
            precio_unidad_raw = row[3]

            # Convertir cantidad a entero
            cantidad_int = int(cantidad) if cantidad is not None else 0

            # Convertir precios a float y redondear
            precio_total_float = float(precio_total_raw) if precio_total_raw is not None else 0.0
            precio_unidad_float = float(precio_unidad_raw) if precio_unidad_raw is not None else 0.0

            precio_total_rounded = round(precio_total_float, 2)
            precio_unidad_rounded = round(precio_unidad_float, 2)

            formatted_results.append({
                "descripcion": descripcion,
                "cantidad": cantidad_int,
                "precio_total": precio_total_rounded,
                "precio_unidad": precio_unidad_rounded,
            })

        return formatted_results

    except Exception as e:
        logger.error(f"Error en get_top_selling_items: {e}", exc_info=True)
        return {"error": "Error interno del servidor al obtener los artículos más vendidos."}
    
@tool 
@db_connection_handler  
def get_sales_item_by_description(descripcion: str, date_start: str, date_end: str, start_time: str, end_time: str):
    """
    Obtiene las ventas de artículos cuya descripción contiene el texto dado,
    en un rango de fechas y horas, agrupado por descripción, precio y estado. Precios redondeados a 2 decimales.

    Args:
        descripcion (str): Texto a buscar en la descripción.
        date_start (str): Fecha de inicio en formato 'YYYY/MM/DD'.
        date_end (str): Fecha de fin en formato 'YYYY/MM/DD'.
        start_time (str): Hora de inicio en formato 'HH:MM'.
        end_time (str): Hora de fin en formato 'HH:MM'.

    Returns:
        list: Lista de diccionarios (descripcion, precio, estado, total_quantity).
              Retorna lista vacía si no hay coincidencias, o un dict de error.
    """
    send_tool_message(f"Buscando ventas de artículos con descripción '{descripcion}' desde {date_start} {start_time} hasta {date_end} {end_time}...")
    try:
        # 1. Validar entradas
        if not descripcion or not isinstance(descripcion, str) or len(descripcion) < 3:
            return {"error": "La descripción debe tener al menos 3 caracteres."}
        
        try:
            datetime.strptime(date_start, "%Y/%m/%d")
            datetime.strptime(date_end, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            if date_start > date_end or (date_start == date_end and start_time > end_time):
                return {"error": "Rango de fechas u horas inválido."}
        except ValueError:
            return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # 2. Iterar sobre la descripción reduciendo caracteres
        formatted_results = []
        for i in range(len(descripcion), 2, -1):  # Reducir hasta un mínimo de 3 caracteres
            partial_description = descripcion[:i]
            raw_query = """
                SELECT
                    lp.descripcion,
                    lp.precio,
                    lp.estado,
                    COUNT(lp.id) AS total_quantity
                FROM
                    lineaspedido lp
                INNER JOIN
                    infmesa im ON lp.UID = im.UID
                INNER JOIN
                    pedidos p ON lp.IDPedido = p.id
                WHERE
                    LOWER(lp.descripcion) LIKE LOWER(%s)
                    AND im.fecha BETWEEN %s AND %s
                    AND p.hora BETWEEN %s AND %s
                GROUP BY
                    lp.descripcion, lp.precio, lp.estado
                ORDER BY
                    total_quantity DESC;
            """
            with connection.cursor() as cursor:
                params = [f"%{partial_description}%", date_start, date_end, start_time, end_time]
                cursor.execute(raw_query, params)
                for row in cursor.fetchall():
                    formatted_results.append({
                        "descripcion": row[0],
                        "precio": round(float(row[1]) if row[1] is not None else 0.0, 2),
                        "estado": row[2] if row[2] is not None else "",
                        "total_quantity": round(float(row[3]) if row[3] is not None else 0.0, 2)
                    })

            # Si se encontraron resultados, detener la iteración
            if formatted_results:
                break

        return formatted_results

    except Exception as e:
        logger.error(f"Error en get_sales_item_by_description: {e}", exc_info=True)
        return {"error": "Error interno al obtener las ventas."}
    

@tool
@db_connection_handler
def get_average_sales(start_date: str, end_date: str, start_time: str, end_time: str):
    """
    Calcula la media de ventas por ticket y por día en un rango de fechas y horas.
    Total de ventas es la suma de precios en lineaspedido, y tickets es el conteo de ticket.id.

    Args:
        start_date (str): Fecha de inicio ('YYYY/MM/DD').
        end_date (str): Fecha de fin ('YYYY/MM/DD').
        start_time (str): Hora de inicio ('HH:MM').
        end_time (str): Hora de fin ('HH:MM').

    Returns:
        dict: 
            - average_per_ticket (float): Media de ventas por ticket, redondeada a 2 decimales.
            - average_per_day (float): Media de ventas por día, redondeada a 2 decimales.
            - total_sales (float): Total de ventas en el rango especificado.
            - total_tickets (int): Número total de tickets en el rango especificado.
            - days (int): Número de días en el rango especificado.
    """
    send_tool_message(f"Calculando promedio de ventas desde {start_date} {start_time} hasta {end_date} {end_time}...")
    try:
        # 1. Validar entradas
        try:
            start_dt = datetime.strptime(start_date, "%Y/%m/%d")
            end_dt = datetime.strptime(end_date, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            if start_date > end_date or (start_date == end_date and start_time > end_time):
                return {"error": "Rango de fechas u horas inválido."}
        except ValueError:
            return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # 2. Consulta SQL
        raw_query = """
            SELECT
                SUM(lp.Precio) AS total_sales,
                COUNT(DISTINCT t.id) AS total_tickets
            FROM
                ticket t
            INNER JOIN
                ticketlineas tl ON t.id = tl.IDTicket
            INNER JOIN
                lineaspedido lp ON tl.IDLinea = lp.id
            WHERE
                t.Fecha BETWEEN %s AND %s
                AND t.Hora BETWEEN %s AND %s
        """
        params = [start_date, end_date, start_time, end_time]

        # 3. Ejecutar consulta
        with connection.cursor() as cursor:
            cursor.execute(raw_query, params)
            row = cursor.fetchone()
            total_sales = Decimal(str(row[0] or '0.0'))
            total_tickets = int(row[1] or 0)

        # 4. Calcular días
        days = (end_dt - start_dt).days + 1

        # 5. Calcular medias
        avg_per_ticket = total_sales / total_tickets if total_tickets > 0 else Decimal('0.0')
        avg_per_day = total_sales / days if days > 0 else Decimal('0.0')

        # 6. Devolver resultado
        return {
            "average_per_ticket": round(float(avg_per_ticket), 2),
            "average_per_day": round(float(avg_per_day), 2),
            "total_sales": round(float(total_sales), 2),
            "total_tickets": total_tickets,
            "days": days
        }

    except Exception as e:
        logger.error(f"Error en get_average_sales: {e}", exc_info=True)
        return {"error": "Error interno al calcular la media de ventas."}

@tool
@db_connection_handler
def get_total_sales_by_cash_counts(id_cash_count: int):
    """
    Obtiene el total de ventas asociadas a un arqueo de caja (por rango de IDs de ticket),
    desglosando entre efectivo y tarjeta y buscando tickets incompletos.

    Args:
        id_cash_count (int): ID del arqueo de caja.

    Returns:
        dict: 
            - id_cash_count (int): ID del arqueo de caja.
            - ticket_range (str): Rango de tickets asociados al arqueo (ej. "100 - 200").
            - total_ventas (float): Total de ventas realizadas en el rango de tickets.
            - total_efectivo (float): Total de ventas en efectivo.
            - total_tarjeta (float): Total de ventas con tarjeta.
            - incomplete_tickets (list): Lista de tickets incompletos con:
                - id (int): ID del ticket.
                - fecha (str): Fecha del ticket.
                - hora (str): Hora del ticket.
                - entrega (float): Monto entregado en el ticket.
    """
    send_tool_message(f"Obteniendo total de ventas para el arqueo de caja ID {id_cash_count}...")
    try:
        # Obtener rango de tickets desde Arqueocaja usando SQL
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT ac.IDCierre, c.ticketcom, c.ticketfinal
                FROM arqueocaja ac
                LEFT JOIN cierrecaja c ON ac.IDCierre = c.id
                WHERE ac.id = %s
            """, [id_cash_count])
            row = cursor.fetchone()
            if not row or row[1] is None or row[2] is None:
                return {"error": f"El arqueo de caja {id_cash_count} no tiene un cierre o rango de tickets válido."}
            ticketcom, ticketfinal = row[1], row[2]

        # Consulta para efectivo (entrega > 0)
        efectivo_query = """
            SELECT SUM(lp.Precio) AS total
            FROM ticketlineas tl
            INNER JOIN ticket t ON tl.IDTicket = t.id
            INNER JOIN lineaspedido lp ON tl.IDLinea = lp.id
            WHERE t.id BETWEEN %s AND %s AND t.entrega > 0
        """
        with connection.cursor() as cursor:
            cursor.execute(efectivo_query, [ticketcom, ticketfinal])
            total_efectivo = Decimal(str(cursor.fetchone()[0] or '0.0'))

        # Consulta para tarjeta (entrega = 0)
        tarjeta_query = """
            SELECT SUM(lp.Precio) AS total
            FROM ticketlineas tl
            INNER JOIN ticket t ON tl.IDTicket = t.id
            INNER JOIN lineaspedido lp ON tl.IDLinea = lp.id
            WHERE t.id BETWEEN %s AND %s AND t.entrega = 0
        """
        with connection.cursor() as cursor:
            cursor.execute(tarjeta_query, [ticketcom, ticketfinal])
            total_tarjeta = Decimal(str(cursor.fetchone()[0] or '0.0'))

        # Total general
        total_ventas = total_efectivo + total_tarjeta

        # Consulta para tickets incompletos
        incomplete_query = """
            SELECT t.id, t.Fecha, t.Hora, t.entrega
            FROM ticket t
            WHERE t.id BETWEEN %s AND %s AND t.entrega = 0 AND t.recibo_tarjeta = ''
        """
        with connection.cursor() as cursor:
            cursor.execute(incomplete_query, [ticketcom, ticketfinal])
            incomplete_tickets_result = [
                {
                    "id": row[0],
                    "fecha": row[1],
                    "hora": row[2],
                    "entrega": float(row[3] or 0.0),
                }
                for row in cursor.fetchall()
            ]

        return {
            "id_cash_count": id_cash_count,
            "count_ticket":  ticketfinal - ticketcom,
            "total_ventas": float(total_ventas),
            "total_efectivo": float(total_efectivo),
            "total_tarjeta": float(total_tarjeta),
            "incomplete_tickets": incomplete_tickets_result
        }

    except Exception as e:
        logger.error(f"Error en get_total_sales_by_arqueo(id={id_cash_count}): {e}", exc_info=True)
        return {"error": f"Error al obtener el total de ventas por arqueo: {str(e)}"}


@tool
@db_connection_handler
def get_total_sales_by_date_range(start_date: str, end_date: str, start_time: str, end_time: str):
    """
    Obtiene el total de ventas en un rango de fechas y horas, desglosando entre
    efectivo y tarjeta y buscando tickets incompletos.

    Args:
        start_date (str): Fecha de inicio ('YYYY/MM/DD').
        end_date (str): Fecha de fin ('YYYY/MM/DD').
        start_time (str): Hora de inicio ('HH:MM').
        end_time (str): Hora de fin ('HH:MM').

    Returns:
        dict: Total de ventas, desglose y tickets incompletos, o un dict de error.
    """
    send_tool_message(f"Obteniendo total de ventas desde {start_date} {start_time} hasta {end_date} {end_time}...")
    try:
        # Validar formato y lógica
        datetime.strptime(start_date, "%Y/%m/%d")
        datetime.strptime(end_date, "%Y/%m/%d")
        datetime.strptime(start_time, "%H:%M")
        datetime.strptime(end_time, "%H:%M")
        if start_date > end_date:
            return {"error": "La fecha de inicio debe ser anterior o igual a la fecha de fin."}
        if start_date == end_date and start_time > end_time:
            return {"error": "La hora de inicio debe ser anterior o igual a la hora de fin en la misma fecha."}

        # Consulta para efectivo (entrega > 0)
        efectivo_query = """
            SELECT SUM(lp.Precio) AS total, COUNT(t.id) AS ticket_count
            FROM ticketlineas tl
            INNER JOIN ticket t ON tl.IDTicket = t.id
            INNER JOIN lineaspedido lp ON tl.IDLinea = lp.id
            WHERE t.Fecha BETWEEN %s AND %s AND t.Hora BETWEEN %s AND %s AND t.entrega > 0
        """
        with connection.cursor() as cursor:
            cursor.execute(efectivo_query, [start_date, end_date, start_time, end_time])
            efectivo_result = cursor.fetchone()
            total_efectivo = Decimal(str(efectivo_result[0] or '0.0'))
            ticket_count_efectivo = int(efectivo_result[1] or 0)

        # Consulta para tarjeta (entrega = 0)
        tarjeta_query = """
            SELECT SUM(lp.Precio) AS total, COUNT(t.id) AS ticket_count
            FROM ticketlineas tl
            INNER JOIN ticket t ON tl.IDTicket = t.id
            INNER JOIN lineaspedido lp ON tl.IDLinea = lp.id
            WHERE t.Fecha BETWEEN %s AND %s AND t.Hora BETWEEN %s AND %s AND t.entrega = 0
        """
        with connection.cursor() as cursor:
            cursor.execute(tarjeta_query, [start_date, end_date, start_time, end_time])
            tarjeta_result = cursor.fetchone()
            total_tarjeta = Decimal(str(tarjeta_result[0] or '0.0'))
            ticket_count_tarjeta = int(tarjeta_result[1] or 0)

        # Total general
        total_ventas = total_efectivo + total_tarjeta
        total_tickets = ticket_count_efectivo + ticket_count_tarjeta

        # Consulta para tickets incompletos
        incomplete_query = """
            SELECT t.id, t.Fecha, t.Hora, t.entrega
            FROM ticket t
            WHERE t.Fecha BETWEEN %s AND %s AND t.Hora BETWEEN %s AND %s AND t.entrega = 0 AND t.recibo_tarjeta = ''
        """
        with connection.cursor() as cursor:
            cursor.execute(incomplete_query, [start_date, end_date, start_time, end_time])
            incomplete_tickets_result = [
                {
                    "id": row[0],
                    "fecha": row[1],
                    "hora": row[2],
                    "entrega": float(row[3] or 0.0),
                }
                for row in cursor.fetchall()
            ]

        return {
            "start_date": start_date,
            "end_date": end_date,
            "start_time": start_time,
            "end_time": end_time,
            "total_ventas": float(total_ventas),
            "total_efectivo": float(total_efectivo),
            "total_tarjeta": float(total_tarjeta),
            "total_tickets": total_tickets,
            "incomplete_tickets": incomplete_tickets_result
        }

    except ValueError:
        logger.error(f"Formato inválido en get_total_sales_by_date_range(dates={start_date}-{end_date}, time={start_time}-{end_time})")
        return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}
    except Exception as e:
        logger.error(f"Error en get_total_sales_by_date_range(dates={start_date}-{end_date}, time={start_time}-{end_time}): {e}", exc_info=True)
        return {"error": f"Error al obtener el total de ventas por fecha/hora: {str(e)}"}
    


@tool
@db_connection_handler
def get_latest_cash_count():
    """
    Obtiene el último arqueo de caja sinonimos (cierre, cierre de caja, caja, arqueo. etc) realizado y calcula el total del desglose de efectivo.
    Returns:
        dict: Detalles del último arqueo de caja con el total del efectivo o un dict de error.
    """
    send_tool_message("Obteniendo el último arqueo de caja...")
    try:
        latest_cash_count = Arqueocaja.objects.latest('cierre__fecha', 'cierre__hora')

        return {
            "id": latest_cash_count.id,
            "hora": latest_cash_count.cierre.hora,
            "fecha": latest_cash_count.cierre.fecha,
            "descuadre": latest_cash_count.descuadre,
            "desglose_gastos": latest_cash_count.get_desglose_gastos(),
            "average_ventas": get_average_sales_per_cierre.invoke({"id_cash_count":latest_cash_count.id}),
            "total_ventas": get_total_sales_by_cash_counts.invoke({"id_cash_count":latest_cash_count.id}) 
        }
    
    except Arqueocaja.DoesNotExist:
        return {"error": "No se encontraron arqueos de caja."}
    except Exception as e:
        return {"error": f"Error al obtener el último arqueo de caja: {str(e)}"}

@tool
@db_connection_handler
def get_cash_counts(fecha: str):
    """
    Obtiene los arqueos (cajas o cierre de cajas) realizados en una fecha específica, incluyendo el total del desglose de efectivo.
    Args:
        fecha (str): Fecha en formato 'YYYY/MM/DD'.
    Returns:
        list: Lista de arqueos realizados en la fecha con el total del efectivo y desglose de gastos.
    """
    send_tool_message(f"Obteniendo arqueos de caja para la fecha {fecha}...")
    try:
        # Validar formato de fecha
        datetime.strptime(fecha, "%Y/%m/%d")

        # Ordenar por fecha y hora
        cash_counts = Arqueocaja.objects.filter(cierre__fecha=fecha).order_by('cierre__fecha', 'cierre__hora')
        result = []
        for cash_count in cash_counts:
            
            result.append({
                "id": cash_count.id,
                "hora": cash_count.cierre.hora,
                "fecha": cash_count.cierre.fecha,
                "descuadre": cash_count.descuadre,
                "desglose_gastos": cash_count.get_desglose_gastos(),
                "average_ventas": get_average_sales_per_cierre.invoke({"id_cash_count":cash_count.id}),
                "total_ventas": get_total_sales_by_cash_counts.invoke({"id_cash_count":cash_count.id}) 
            })

        return result
    except ValueError as ve:
        logger.error(f"Error de formato detectado: {ve}")
        return {"error": "El formato de la fecha debe ser 'YYYY/MM/DD'."}
    except Exception as e:
        logger.error(f"Error al obtener arqueos de caja: {e}", exc_info=True)
        return {"error": f"Error al obtener arqueos: {str(e)}"}
    

@tool
@db_connection_handler
def get_average_sales_per_cierre(id_cash_count: int):
    """
    Calcula la media de ventas por ticket para el cierre de caja asociado a un arqueo,
    dado su ID. Usa el rango de tickets (ticketcom a ticketfinal) del cierre.

    Args:
        id_cash_count (int): ID del arqueo de caja.

    Returns:
        dict: Con 'average_per_ticket' (media por ticket, redondeada a 2 decimales),
              'total_sales' (ventas totales), 'total_tickets' (número de tickets),
              'ticket_range' (rango de tickets), o un dict de error.
    """
    send_tool_message(f"Calculando promedio de ventas por ticket para el arqueo de caja ID {id_cash_count}...")
    try:
        # 1. Validar id_cash_count
        if not isinstance(id_cash_count, int) or id_cash_count <= 0:
            return {"error": f"ID de arqueo inválido: {id_cash_count}"}

        # 2. Obtener ticketcom y ticketfinal desde arqueocaja y cierre
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT c.ticketcom, c.ticketfinal
                FROM arqueocaja ac
                INNER JOIN cierrecaja c ON ac.IDCierre = c.id
                WHERE ac.id = %s
            """, [id_cash_count])
            row = cursor.fetchone()
            if not row or row[0] is None or row[1] is None:
                return {"error": f"El arqueo de caja {id_cash_count} no tiene un cierre o rango de tickets válido."}
            ticketcom, ticketfinal = row[0], row[1]

        # 3. Consulta SQL para ventas y tickets
        query = """
            SELECT
                SUM(lp.Precio) AS total_sales,
                COUNT(DISTINCT t.id) AS total_tickets
            FROM
                ticket t
            INNER JOIN
                ticketlineas tl ON t.id = tl.IDTicket
            INNER JOIN
                lineaspedido lp ON tl.IDLinea = lp.id
            WHERE
                t.id BETWEEN %s AND %s
        """
        with connection.cursor() as cursor:
            cursor.execute(query, [ticketcom, ticketfinal])
            row = cursor.fetchone()
            total_sales = Decimal(str(row[0] or '0.0'))
            total_tickets = int(row[1] or 0)

        # 4. Calcular media por ticket
        average_per_ticket = total_sales / total_tickets if total_tickets > 0 else Decimal('0.0')

        # 5. Devolver resultado
        return {
            "id_cash_count": id_cash_count,
            "average_per_ticket": round(float(average_per_ticket), 2),
            "total_sales": round(float(total_sales), 2),
            "total_tickets": total_tickets
        }

    except Exception as e:
        logger.error(f"Error en get_average_sales_per_cierre(id={id_cash_count}): {e}", exc_info=True)
        return {"error": f"Error al calcular la media de ventas por cierre: {str(e)}"}


@tool
@db_connection_handler
def get_last_tickets(date_start: str, date_end: str, start_time: str, end_time: str):
    """
    Obtiene los tickets registrados en un rango de fechas y horas.
   
    Args:
        date_start (str): Fecha de inicio ('YYYY/MM/DD').
        date_end (str): Fecha de fin ('YYYY/MM/DD').
        start_time (str): Hora de inicio ('HH:MM').
        end_time (str): Hora de fin ('HH:MM').

    Returns:
        list: Lista de diccionarios con información básica de cada ticket:
            - id (int): Identificador único del ticket.
            - fecha (str): Fecha del ticket en formato 'YYYY/MM/DD'.
            - hora (str): Hora del ticket en formato 'HH:MM'.
            - entrega (float): Monto entregado por el cliente.
            - total (float): Total del ticket (suma de precios de sus líneas).
              Los valores monetarios están redondeados a 2 decimales.
              
        En caso de error, retorna un diccionario con la clave 'error' y el mensaje correspondiente.
    """
    send_tool_message(f"Obteniendo tickets desde {date_start} {start_time} hasta {date_end} {end_time}...")
    try:
        # Validar formato de fechas y horas
        try:
            datetime.strptime(date_start, "%Y/%m/%d")
            datetime.strptime(date_end, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            if date_start > date_end or (date_start == date_end and start_time > end_time):
                return {"error": "Rango de fechas u horas inválido."}
        except ValueError:
            return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # Consulta SQL para obtener tickets en el rango especificado
        query = """
            SELECT t.id, t.Fecha, t.Hora, t.entrega,
                COALESCE(SUM(lp.Precio), 0.0) AS total
            FROM ticket t
            LEFT JOIN ticketlineas tl ON t.id = tl.IDTicket
            LEFT JOIN lineaspedido lp ON tl.IDLinea = lp.id
            WHERE t.Fecha BETWEEN %s AND %s
              AND t.Hora BETWEEN %s AND %s
            GROUP BY t.id, t.Fecha, t.Hora, t.entrega
            ORDER BY t.Fecha DESC, t.Hora DESC, t.id DESC
        """
        with connection.cursor() as cursor:
            cursor.execute(query, [date_start, date_end, start_time, end_time])
            rows = cursor.fetchall()
            result = [
                {
                    "id": row[0],
                    "fecha": row[1],
                    "hora": row[2],
                    "entrega": float(row[3] or 0.0),
                    "total": round(float(row[4] or 0.0), 2)
                }
                for row in rows
            ]
        return result
    except Exception as e:
        logger.error(f"Error en get_last_tickets: {e}", exc_info=True)
        return {"error": f"Error al obtener los tickets: {str(e)}"}

@tool
@db_connection_handler
def get_ticket_details(ticket_id: int):
    """
    Devuelve el desglose de un ticket por su ID, incluyendo líneas, totales y datos básicos.

    Args:
        ticket_id (int): ID del ticket.

    Returns:
        dict: 
            - id: ID del ticket.
            - fecha: Fecha del ticket.
            - hora: Hora del ticket.
            - entrega: Monto entregado.
            - lineas: Lista de líneas con descripcion, cantidad, precio_unitario, total_linea, estado.
            - total: Total del ticket (suma de precios de sus líneas).
    """
    send_tool_message(f"Obteniendo desglose del ticket {ticket_id}...")
    try:
        if not isinstance(ticket_id, int) or ticket_id <= 0:
            return {"error": "ID de ticket inválido."}

        # Obtener datos básicos del ticket
        ticket_query = """
            SELECT id, Fecha, Hora, entrega
            FROM ticket
            WHERE id = %s
        """
        with connection.cursor() as cursor:
            cursor.execute(ticket_query, [ticket_id])
            ticket_row = cursor.fetchone()
            if not ticket_row:
                return {"error": f"No existe el ticket con ID {ticket_id}."}
            ticket_data = {
                "id": ticket_row[0],
                "fecha": ticket_row[1],
                "hora": ticket_row[2],
                "entrega": float(ticket_row[3] or 0.0),
            }

        # Obtener líneas del ticket
        lineas_query = """
            SELECT lp.descripcion, COUNT(tl.id) as can, lp.precio, COUNT(tl.id) * lp.precio as totalLinea
            FROM ticketlineas tl
            INNER JOIN lineaspedido lp ON tl.IDLinea = lp.id
            WHERE tl.IDTicket = %s
            GROUP BY lp.descripcion, lp.precio
        """
        with connection.cursor() as cursor:
            cursor.execute(lineas_query, [ticket_id])
            lineas = []
            total = 0.0
            for row in cursor.fetchall():
                descripcion = row[0]
                cantidad = float(row[1] or 1)
                precio_unitario = float(row[2] or 0.0)
                total_linea = float(row[3] or 0.0)  # Convertir a float
                total += total_linea
                lineas.append({
                    "descripcion": descripcion,
                    "cantidad": cantidad,
                    "precio_unitario": round(precio_unitario, 2),
                    "total_linea": round(total_linea, 2),  # Redondear también
                })
            ticket_data["lineas"] = lineas
            ticket_data["total"] = round(total, 2)

        return ticket_data
    except Exception as e:
        logger.error(f"Error en get_ticket_details({ticket_id}): {e}", exc_info=True)
        return {"error": f"Error al obtener el desglose del ticket: {str(e)}"}

@tool
@db_connection_handler
def get_sales_item_by_ids(ids: List[int], date_start: str, date_end: str, start_time: str, end_time: str):
    """
    Obtiene las ventas de artículos cuyos IDs están en la lista proporcionada,
    en un rango de fechas y horas, agrupado por descripción, precio y estado. Precios redondeados a 2 decimales.

    Args:
        ids (list): Lista de IDs de artículos.
        date_start (str): Fecha de inicio ('YYYY/MM/DD').
        date_end (str): Fecha de fin ('YYYY/MM/DD').
        start_time (str): Hora de inicio ('HH:MM').
        end_time (str): Hora de fin ('HH:MM').

    Returns:
        list: Lista de diccionarios (descripcion, precio, estado, total_quantity).
              Retorna lista vacía si no hay coincidencias, o un dict de error.
    """
    send_tool_message(f"Buscando ventas de artículos con IDs {ids} desde {date_start} {start_time} hasta {date_end} {end_time}...")
    try:
        # 1. Validar entradas
        if not ids or not isinstance(ids, list) or not all(isinstance(i, int) for i in ids):
            return {"error": "La lista de IDs debe contener enteros válidos."}
        
        try:
            datetime.strptime(date_start, "%Y/%m/%d")
            datetime.strptime(date_end, "%Y/%m/%d")
            datetime.strptime(start_time, "%H:%M")
            datetime.strptime(end_time, "%H:%M")
            if date_start > date_end or (date_start == date_end and start_time > end_time):
                return {"error": "Rango de fechas u horas inválido."}
        except ValueError:
            return {"error": "Formato debe ser 'YYYY/MM/DD' para fechas y 'HH:MM' para horas."}

        # 2. Consulta SQL
        raw_query = """
            SELECT
                lp.descripcion,
                lp.precio,
                lp.estado,
                COUNT(lp.id) AS total_quantity
            FROM
                lineaspedido lp
            INNER JOIN
                infmesa im ON lp.UID = im.UID
            INNER JOIN
                pedidos p ON lp.IDPedido = p.id
            WHERE
                lp.id IN %s
                AND im.fecha BETWEEN %s AND %s
                AND p.hora BETWEEN %s AND %s
            GROUP BY
                lp.descripcion, lp.precio, lp.estado
            ORDER BY
                total_quantity DESC;
        """

        # 3. Ejecutar consulta
        formatted_results = []
        with connection.cursor() as cursor:
            params = [tuple(ids), date_start, date_end, start_time, end_time]
            cursor.execute(raw_query, params)
            for row in cursor.fetchall():
                formatted_results.append({
                    "descripcion": row[0],
                    "precio": round(float(row[1]) if row[1] is not None else 0.0, 2),
                    "estado": row[2] if row[2] is not None else "",
                    "total_quantity": int(row[3]) if row[3] is not None else 0,
                })

        return formatted_results

    except Exception as e:
        logger.error(f"Error en get_sales_item_by_ids: {e}", exc_info=True)
        return {"error": "Error interno al obtener las ventas."}

@tool
@db_connection_handler
def get_cash_count_breakdown_by_id(arqueo_id: int):
    """
    Obtiene el desglose de cambio específicos de un arqueo de caja por su ID:
    - Stacke actual y anterior
    - Cambio real actual y anterior
    - Desglose separado en dos tablas: cambio remanente y cambio retirado

    Args:
        arqueo_id (int): ID del arqueo de caja.

    Returns:
        dict: Diccionario con datos específicos del arqueo y anterior
    """
    send_tool_message(f"Obteniendo datos específicos del arqueo de caja ID {arqueo_id}...")
    try:
        # Obtener el arqueo actual
        try:
            arqueo_actual = Arqueocaja.objects.select_related('cierre').get(id=arqueo_id)
        except Arqueocaja.DoesNotExist:
            return {"error": f"No se encontró el arqueo con ID {arqueo_id}"}

        # Obtener el arqueo anterior (por ID anterior)
        arqueo_anterior = Arqueocaja.objects.filter(id__lt=arqueo_id).order_by('-id').first()
        
        # Valores del arqueo anterior
        stacke_anterior = arqueo_anterior.stacke if arqueo_anterior else 0.0
        cambio_real_anterior = arqueo_anterior.cambio_real if arqueo_anterior else 0.0

        # Valores del arqueo actual
        stacke_actual = arqueo_actual.stacke
        cambio_real_actual = arqueo_actual.cambio_real

        # Obtener desglose completo del efectivo
        desglose_efectivo_completo = arqueo_actual.get_desglose_efectivo()
        
        # TABLA 1: CAMBIO REMANENTE (líneas que quedan en caja)
        denominaciones_remanente = {}
        if 'lineas_cambio' in desglose_efectivo_completo:
            for linea in desglose_efectivo_completo['lineas_cambio']:
                tipo = linea['tipo']
                if tipo in denominaciones_remanente:
                    denominaciones_remanente[tipo]['can'] += linea['can']
                else:
                    denominaciones_remanente[tipo] = {
                        'tipo': tipo,
                        'can': linea['can'],
                        'texto_tipo': linea['texto_tipo']
                    }
        
        # Convertir a lista ordenada
        cambio_remanente = list(denominaciones_remanente.values())
        cambio_remanente.sort(key=lambda x: x['tipo'], reverse=True)
        
        # TABLA 2: CAMBIO RETIRADO (todas las líneas retiradas)
        denominaciones_retirado = {}
        if 'lineas_retirar' in desglose_efectivo_completo:
            for linea in desglose_efectivo_completo['lineas_retirar']:
                tipo = linea['tipo']
                if tipo in denominaciones_retirado:
                    denominaciones_retirado[tipo]['can'] += linea['can']
                else:
                    denominaciones_retirado[tipo] = {
                        'tipo': tipo,
                        'can': linea['can'],
                        'texto_tipo': linea['texto_tipo']
                    }
        
        # Convertir a lista ordenada
        cambio_retirado = list(denominaciones_retirado.values())
        cambio_retirado.sort(key=lambda x: x['tipo'], reverse=True)
        
        # Calcular totales para cambio remanente
        total_remanente_unidades = sum(denominacion['can'] for denominacion in cambio_remanente)
        total_remanente_valor = sum(denominacion['can'] * denominacion['tipo'] for denominacion in cambio_remanente)
        
        # Calcular totales para cambio retirado
        total_retirado_unidades = sum(denominacion['can'] for denominacion in cambio_retirado)
        total_retirado_valor = sum(denominacion['can'] * denominacion['tipo'] for denominacion in cambio_retirado)

        return {
            "stacke_actual": round(stacke_actual, 2),
            "cambio_real_actual": round(cambio_real_actual, 2),
            "stacke_anterior": round(stacke_anterior, 2),
            "cambio_real_anterior": round(cambio_real_anterior, 2),
            "cambio_remanente": cambio_remanente,
            "total_remanente_unidades": total_remanente_unidades,
            "total_remanente_valor": round(total_remanente_valor, 2),
            "cambio_retirado": cambio_retirado,
            "total_retirado_unidades": total_retirado_unidades,
            "total_retirado_valor": round(total_retirado_valor, 2)
        }

    except Exception as e:
        logger.error(f"Error en get_cash_count_breakdown_by_id({arqueo_id}): {e}", exc_info=True)
        return {"error": f"Error interno al obtener el desglose del arqueo: {str(e)}"}

@tool
@db_connection_handler
def create_ticket_print_object(ticket_id: int):
    """
    Crea un objeto para imprimir un ticket específico y lo envia a la impresora Ticket, con todos los datos necesarios
    para el sistema de impresión (receptor, líneas, totales, etc.).

    Args:
        ticket_id (int): ID del ticket a preparar para impresión.

    Returns:
        dict: Objeto completo para impresión del ticket con:
              - op: Operación ('ticket')
              - fecha: Fecha y hora del ticket
              - receptor: Configuración de impresora
              - camarero: Nombre completo del camarero
              - mesa: Mesa del ticket
              - lineas: Líneas del ticket agrupadas
              - num: Número del ticket
              - efectivo: Monto entregado
              - total: Total del ticket
              - recibo: Líneas del recibo de tarjeta (si existe)
    """
    send_tool_message(f"Creando objeto de impresión para ticket ID {ticket_id}...")
    try:
        # Validar ticket_id
        if not isinstance(ticket_id, int) or ticket_id <= 0:
            return {"error": "ID de ticket inválido."}

        # Obtener el ticket
        try:
            ticket = Ticket.objects.get(pk=ticket_id)
        except Ticket.DoesNotExist:
            return {"error": f"No se encontró el ticket con ID {ticket_id}"}

        # Obtener el camarero
        try:
            camarero = Camareros.objects.get(pk=ticket.camarero_id)
        except Camareros.DoesNotExist:
            return {"error": f"No se encontró el camarero del ticket {ticket_id}"}

        # Obtener líneas del ticket agrupadas (como en impresion.py)
        lineas = ticket.ticketlineas_set.all().annotate(
            idart=F("linea__idart"),
            precio=F("linea__precio"),
            descripcion_t=F("linea__descripcion_t")
        )

        lineas = lineas.values("idart", "descripcion_t", "precio").annotate(
            can=Count('idart'),
            totallinea=Sum("precio")
        )

        # Obtener receptor de ticket
        try:
            receptor = Receptores.objects.get(nombre='Ticket')
        except Receptores.DoesNotExist:
            return {"error": "No se encontró el receptor de impresión 'Ticket'"}

        # Procesar líneas para la impresión
        lineas_ticket = []
        for l in lineas:
            lineas_ticket.append(l)

        # Procesar recibo de tarjeta si existe
        lineas_recibo = []
        if ticket.recibo_tarjeta != "":
            try:
                recibo = json.loads(ticket.recibo_tarjeta)
                
                # Convertir el recibo en una lista de líneas formato "clave: valor"
                for key, value in recibo['recibo'].items():
                    lineas_recibo.append(f"{key} {value}")
                
                # Añadir la línea con el código de autorización
                lineas_recibo.append(f"Código autorización: {recibo['codigo_autorizacion']}")
            except (json.JSONDecodeError, KeyError) as e:
                logger.warning(f"Error al procesar recibo_tarjeta para ticket {ticket_id}: {e}")

        # Calcular total del ticket
        total_ticket = ticket.ticketlineas_set.all().aggregate(Total=Sum("linea__precio"))['Total']
        total_ticket = float(total_ticket) if total_ticket is not None else 0.0

        # Crear objeto de impresión
        obj = {
            "op": "ticket",
            "fecha": ticket.fecha + " " + ticket.hora,
            "receptor": receptor.nomimp,
            "nom_receptor": receptor.nombre,
            "receptor_activo": True,
            "abrircajon": False,  # No abrir cajón según especificaciones
            "camarero": camarero.nombre + " " + camarero.apellidos,
            "mesa": ticket.mesa,
            "lineas": lineas_ticket,
            "num": ticket.id,
            "efectivo": float(ticket.entrega),
            "total": round(total_ticket, 2),
            "url_factura": "",  # No es factura según especificaciones
            "recibo": lineas_recibo
        }

        send_mensaje_impresora(obj)
        return obj

    except Exception as e:
        logger.error(f"Error en create_ticket_print_object({ticket_id}): {e}", exc_info=True)
        return {"error": f"Error interno al crear objeto de impresión: {str(e)}"}


@tool
@db_connection_handler
def search_order_lines_by_description(descripcion: str, fecha: str = None, start_time: str = None, end_time: str = None, limit: int = None):
    """
    Busca líneas de pedido individuales por descripción con filtros opcionales de fecha y hora.
    Devuelve información básica de cada línea: infmesa.pk, estado y descripción.
    La búsqueda es insensible a mayúsculas y busca si la descripción contiene, empieza o acaba con el texto.

    Args:
        descripcion (str): Descripción del artículo a buscar (mínimo 3 caracteres).
        fecha (str, optional): Fecha específica en formato 'YYYY/MM/DD'. Si no se proporciona, busca en todas las fechas.
        start_time (str, optional): Hora de inicio en formato 'HH:MM'. Requiere end_time si se usa.
        end_time (str, optional): Hora de fin en formato 'HH:MM'. Requiere start_time si se usa.
        limit (int, optional): Número máximo de resultados a devolver. Si no se especifica, devuelve todos.

    Returns:
        list: Lista de diccionarios con líneas individuales:
              - id: ID de la línea de pedido
              - infmesa_pk: PK de la infmesa (UID)
              - descripcion: Descripción del artículo
              - precio: Precio del artículo
              - estado: Estado de la línea (P=Pendiente, C=Cobrado, A=Anulado)
              - fecha: Fecha del pedido (desde infmesa)
              - hora: Hora del pedido (desde pedidos)
              - id_pedido: ID del pedido al que pertenece
              
    Ejemplos de uso:
        - "dame los 2 últimos café bombón pedidos" -> descripcion="café bombón", limit=2
        - "café bombón del día 2025/01/15" -> descripcion="café bombón", fecha="2025/01/15"
        - "café bombón desde las 10:00 a las 14:00" -> descripcion="café bombón", start_time="10:00", end_time="14:00"
    """
    send_tool_message(f"Buscando líneas individuales de pedido con descripción '{descripcion}'" + 
                     (f" en fecha {fecha}" if fecha else "") +
                     (f" desde {start_time} hasta {end_time}" if start_time and end_time else "") +
                     (f" (límite: {limit})" if limit else ""))
    
    try:
        # 1. Validar descripción
        if not descripcion or not isinstance(descripcion, str) or len(descripcion.strip()) < 3:
            return {"error": "La descripción debe tener al menos 3 caracteres."}
        
        descripcion = descripcion.strip()
        
        # 2. Validar fecha si se proporciona
        if fecha:
            try:
                datetime.strptime(fecha, "%Y/%m/%d")
            except ValueError:
                return {"error": "El formato de fecha debe ser 'YYYY/MM/DD'."}
        
        # 3. Validar horas si se proporcionan
        if start_time or end_time:
            if not (start_time and end_time):
                return {"error": "Si especificas hora de inicio, también debes especificar hora de fin y viceversa."}
            try:
                datetime.strptime(start_time, "%H:%M")
                datetime.strptime(end_time, "%H:%M")
                if start_time > end_time and not fecha:
                    return {"error": "La hora de inicio debe ser anterior a la hora de fin."}
            except ValueError:
                return {"error": "El formato de hora debe ser 'HH:MM'."}
        
        # 4. Validar límite
        if limit is not None and (not isinstance(limit, int) or limit <= 0):
            return {"error": "El límite debe ser un número entero positivo."}
        
        # 5. Construir consulta SQL simplificada (solo información básica disponible)
        base_query = """
            SELECT 
                lp.id,
                lp.UID as infmesa_pk,
                lp.descripcion,
                lp.precio,
                lp.estado,
                im.fecha,
                p.hora,
                p.id as id_pedido
            FROM lineaspedido lp
            INNER JOIN infmesa im ON lp.UID = im.UID
            INNER JOIN pedidos p ON lp.IDPedido = p.id
            WHERE (
                LOWER(lp.descripcion) LIKE LOWER(%s) OR 
                LOWER(lp.descripcion) LIKE LOWER(%s) OR 
                LOWER(lp.descripcion) LIKE LOWER(%s)
            )
        """
        
        # 6. Preparar parámetros para búsqueda flexible (contiene, empieza, acaba)
        desc_contains = f"%{descripcion}%"  # Contiene
        desc_starts = f"{descripcion}%"     # Empieza con
        desc_ends = f"%{descripcion}"       # Acaba con
        
        params = [desc_contains, desc_starts, desc_ends]
        
        # Agregar filtro de fecha si se especifica
        if fecha:
            base_query += " AND im.fecha = %s"
            params.append(fecha)
        
        # Agregar filtro de horas si se especifican
        if start_time and end_time:
            base_query += " AND p.hora BETWEEN %s AND %s"
            params.append(start_time)
            params.append(end_time)
        
        # Ordenar por fecha y hora descendente (más recientes primero)
        base_query += " ORDER BY im.fecha DESC, p.hora DESC, lp.id DESC"
        
        # Agregar límite si se especifica
        if limit:
            base_query += " LIMIT %s"
            params.append(limit)
        
        # 7. Ejecutar consulta
        with connection.cursor() as cursor:
            logger.debug(f"Ejecutando consulta con params: {params}")
            cursor.execute(base_query, params)
            rows = cursor.fetchall()
            logger.debug(f"Consulta devolvió {len(rows)} resultados")
        
        # 8. Formatear resultados (información básica)
        results = []
        for row in rows:
            results.append({
                "id": row[0],
                "infmesa_pk": row[1],
                "descripcion": row[2],
                "precio": round(float(row[3]) if row[3] is not None else 0.0, 2),
                "estado": row[4] if row[4] else "P",
                "fecha": row[5],
                "hora": row[6],
                "id_pedido": row[7]
            })
        
        # 9. Si no se encontraron resultados exactos, intentar búsqueda parcial más flexible
        if not results and len(descripcion) > 3:
            # Buscar con descripción reducida, manteniendo la búsqueda flexible
            for i in range(len(descripcion) - 1, 2, -1):
                partial_desc = descripcion[:i]
                partial_contains = f"%{partial_desc}%"
                partial_starts = f"{partial_desc}%"
                partial_ends = f"%{partial_desc}"
                
                # Reemplazar los primeros 3 parámetros con los nuevos
                new_params = [partial_contains, partial_starts, partial_ends] + params[3:]
                
                cursor.execute(base_query, new_params)
                rows = cursor.fetchall()
                if rows:
                    for row in rows:
                        results.append({
                            "id": row[0],
                            "infmesa_pk": row[1],
                            "descripcion": row[2],
                            "precio": round(float(row[3]) if row[3] is not None else 0.0, 2),
                            "estado": row[4] if row[4] else "P",
                            "fecha": row[5],
                            "hora": row[6],
                            "id_pedido": row[7]
                        })
                    break
        
        return results
        
    except Exception as e:
        logger.error(f"Error en search_order_lines_by_description: {e}", exc_info=True)
        return {"error": f"Error interno al buscar líneas de pedido: {str(e)}"}


# --- Lista Final de Herramientas ---
tools = [
    get_sales_by_waiters,
    get_sales_by_waiter_id,
    get_sales_item_by_description,
    get_top_selling_items,
    get_average_sales,
    get_cash_counts,
    get_latest_cash_count,
    get_total_sales_by_cash_counts,
    get_total_sales_by_date_range,
    get_average_sales_per_cierre,
    get_last_tickets,
    get_ticket_details,  
    get_sales_item_by_ids,  
    get_cash_count_breakdown_by_id,
    create_ticket_print_object,
    search_order_lines_by_description,
]
# --- Fin de la lista de herramientas ---