from typing import List, Dict, Any
from gestion.models.pedidos import Lineaspedido
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.infmesa import Infmesa
from gestion.models.historiales import Historialnulos 

from django.db.models import Sum,  Q, Value, FloatField
from django.db.models.functions import Coalesce
       
from langchain_core.tools import tool
from gestion.tools.config_logs import log_debug_chatbot as logger
from chatbot.utilidades.ws_sender import send_tool_message  # Añadir este import
from ..decorators.db_connection_manager import db_connection_handler  # Añadir este import

from gestion.models.camareros import Camareros 
from gestion.models.mesas import Mesas     


@tool
@db_connection_handler
def get_open_tables() -> List[Dict[str, Any]]:
    """
    Obtiene un listado de todas las mesas abiertas y calcula sus totales.

    Realiza una única consulta a la base de datos para agregar los totales de
    líneas pedidas, anuladas y cobradas para cada mesa, resultando en un
    rendimiento muy eficiente.

    Returns:
        Una lista de diccionarios, donde cada uno representa una mesa abierta.
        Ejemplo de un elemento de la lista:
        {
            "id": 15,
            "uid": "15-20250709234501",
            "nombre": "Mesa 15",
            "total_mesa": {
                "total_pedido": 45.50,
                "total_anulado": 5.00,
                "total_cobrado": 0.00
            }
        }
    """
    send_tool_message("Obteniendo datos de mesas abiertas...")
    try:
        # Usamos annotate() y values() para la máxima eficiencia.
        mesas_data = Mesasabiertas.objects.annotate(
            total_pedido=Coalesce(Sum('infmesa__lineaspedido__precio', filter=Q(infmesa__lineaspedido__estado__in=['P','M'])), Value(0), output_field=FloatField()),
            total_anulado=Coalesce(Sum('infmesa__lineaspedido__precio', filter=Q(infmesa__lineaspedido__estado='A')), Value(0), output_field=FloatField()),
            total_cobrado=Coalesce(Sum('infmesa__lineaspedido__precio', filter=Q(infmesa__lineaspedido__estado='C')), Value(0), output_field=FloatField()),
        ).values(
            'mesa__id',
            'infmesa__pk',
            'mesa__nombre',
            'total_pedido',
            'total_anulado',
            'total_cobrado',
        )

        # Construye la estructura final anidada.
        result = [
            {
                "id": mesa['mesa__id'],
                "uid": mesa['infmesa__pk'],
                "nombre": mesa['mesa__nombre'],
                "total_mesa": {
                    "total_pedido": round(mesa['total_pedido'], 2),
                    "total_anulado": round(mesa['total_anulado'], 2),
                    "total_cobrado": round(mesa['total_cobrado'], 2),
                }
            }
            for mesa in mesas_data
        ]
        return result
        
    except Exception as e:
        logger.error(f"Error en get_open_tables: {e}", exc_info=True)
        return {"error": "Error interno al obtener las mesas abiertas."}
    


@tool
@db_connection_handler
def get_table_items_by_uid(uids):
    """
    Obtiene el desglose de pedidos de una o varias mesas abiertas dado sus UIDs.

    Args:
        uids (str o list): UID de la mesa abierta o lista de UIDs.

    Returns:
        dict: Diccionario con:
              - mesas: Lista de diccionarios, cada uno representando una mesa con:
                  - uid: UID de la mesa
                  - pedidos: Lista de pedidos de la mesa
                  - totales: Totales calculados para la mesa
              - total_mesas: Número total de mesas procesadas
              - errores: Lista de errores encontrados
    """
    # Convertir a lista si se recibe un solo UID
    if isinstance(uids, str):
        uids = [uids]
    
    send_tool_message(f"Obteniendo pedidos de {len(uids)} mesa(s) por UID...")
    
    resultado = {
        "mesas": [],
        "total_mesas": 0,
        "errores": []
    }
    
    try:
        for uid in uids:
            try:
                infmesa = Infmesa.objects.filter(pk=uid).first()
                if not infmesa:
                    resultado["errores"].append(f"No se encontró una mesa con UID {uid}")
                    continue
                
                # Obtener los pedidos
                pedidos = infmesa.get_pedidos()
                
                # Calcular totales
                totales = infmesa.lineaspedido_set.aggregate(
                    total_pedido=Coalesce(Sum('precio', filter=Q(estado__in=['P','M'])), Value(0), output_field=FloatField()),
                    total_anulado=Coalesce(Sum('precio', filter=Q(estado='A')), Value(0), output_field=FloatField()),
                    total_cobrado=Coalesce(Sum('precio', filter=Q(estado='C')), Value(0), output_field=FloatField()),
                )
                
                mesa_data = {
                    "uid": uid,
                    "pedidos": pedidos,
                    "totales": {
                        "total_pedido": round(float(totales['total_pedido']), 2),
                        "total_anulado": round(float(totales['total_anulado']), 2),
                        "total_cobrado": round(float(totales['total_cobrado']), 2),
                    }
                }
                
                resultado["mesas"].append(mesa_data)
                
            except Exception as e:
                logger.error(f"Error procesando UID {uid}: {e}", exc_info=True)
                resultado["errores"].append(f"Error interno al procesar UID {uid}: {str(e)}")
        
        resultado["total_mesas"] = len(resultado["mesas"])
        return resultado
        
    except Exception as e:
        logger.error(f"Error en get_table_items_by_uid: {e}", exc_info=True)
        return {"error": "Error interno al obtener los artículos de las mesas."}


@tool
@db_connection_handler
def get_table_items_by_name(names):
    """
    Obtiene el desglose de pedidos de una o varias mesas abiertas dado sus nombres.

    Args:
        names (str o list): Nombre de la mesa o lista de nombres.

    Returns:
        dict: Diccionario con:
              - mesas: Lista de diccionarios, cada uno representando una mesa con:
                  - nombre: Nombre de la mesa
                  - uid: UID de la mesa
                  - pedidos: Lista de pedidos de la mesa
                  - totales: Totales calculados para la mesa
              - total_mesas: Número total de mesas procesadas
              - errores: Lista de errores encontrados
    """
    # Convertir a lista si se recibe un solo nombre
    if isinstance(names, str):
        names = [names]
    
    send_tool_message(f"Obteniendo pedidos de {len(names)} mesa(s) por nombre...")
    
    resultado = {
        "mesas": [],
        "total_mesas": 0,
        "errores": []
    }
    
    try:
        for name in names:
            try:
                mesa_abierta = Mesasabiertas.objects.filter(mesa__nombre=name).first()
                if not mesa_abierta:
                    resultado["errores"].append(f"No se encontró una mesa abierta con el nombre '{name}'")
                    continue
                
                # Obtener los pedidos
                pedidos = mesa_abierta.infmesa.get_pedidos()
                
                # Calcular totales
                totales = mesa_abierta.infmesa.lineaspedido_set.aggregate(
                    total_pedido=Coalesce(Sum('precio', filter=Q(estado__in=['P','M'])), Value(0), output_field=FloatField()),
                    total_anulado=Coalesce(Sum('precio', filter=Q(estado='A')), Value(0), output_field=FloatField()),
                    total_cobrado=Coalesce(Sum('precio', filter=Q(estado='C')), Value(0), output_field=FloatField()),
                )
                
                mesa_data = {
                    "nombre": name,
                    "uid": mesa_abierta.infmesa.pk,
                    "pedidos": pedidos,
                    "totales": {
                        "total_pedido": round(float(totales['total_pedido']), 2),
                        "total_anulado": round(float(totales['total_anulado']), 2),
                        "total_cobrado": round(float(totales['total_cobrado']), 2),
                    }
                }
                
                resultado["mesas"].append(mesa_data)
                
            except Exception as e:
                logger.error(f"Error procesando nombre '{name}': {e}", exc_info=True)
                resultado["errores"].append(f"Error interno al procesar nombre '{name}': {str(e)}")
        
        resultado["total_mesas"] = len(resultado["mesas"])
        return resultado
        
    except Exception as e:
        logger.error(f"Error en get_table_items_by_name: {e}", exc_info=True)
        return {"error": "Error interno al obtener los artículos de las mesas."}

@tool
@db_connection_handler
def get_table_items_by_id(table_ids):
    """
    Obtiene el desglose de pedidos de una o varias mesas abiertas dado sus IDs.

    Args:
        table_ids (int o list): ID de la mesa o lista de IDs.

    Returns:
        dict: Diccionario con:
              - mesas: Lista de diccionarios, cada uno representando una mesa con:
                  - id: ID de la mesa
                  - nombre: Nombre de la mesa
                  - uid: UID de la mesa
                  - pedidos: Lista de pedidos de la mesa
                  - totales: Totales calculados para la mesa
              - total_mesas: Número total de mesas procesadas
              - errores: Lista de errores encontrados
    """
    # Convertir a lista si se recibe un solo ID
    if isinstance(table_ids, int):
        table_ids = [table_ids]
    
    send_tool_message(f"Obteniendo pedidos de {len(table_ids)} mesa(s) por ID...")
    
    resultado = {
        "mesas": [],
        "total_mesas": 0,
        "errores": []
    }
    
    try:
        for table_id in table_ids:
            try:
                mesa_abierta = Mesasabiertas.objects.filter(mesa__id=table_id).first()
                if not mesa_abierta:
                    resultado["errores"].append(f"No se encontró una mesa abierta con el ID {table_id}")
                    continue
                
                # Obtener los pedidos
                pedidos = mesa_abierta.infmesa.get_pedidos()
                
                # Calcular totales
                totales = mesa_abierta.infmesa.lineaspedido_set.aggregate(
                    total_pedido=Coalesce(Sum('precio', filter=Q(estado__in=['P','M'])), Value(0), output_field=FloatField()),
                    total_anulado=Coalesce(Sum('precio', filter=Q(estado='A')), Value(0), output_field=FloatField()),
                    total_cobrado=Coalesce(Sum('precio', filter=Q(estado='C')), Value(0), output_field=FloatField()),
                )
                
                mesa_data = {
                    "id": table_id,
                    "nombre": mesa_abierta.mesa.nombre,
                    "uid": mesa_abierta.infmesa.pk,
                    "pedidos": pedidos,
                    "totales": {
                        "total_pedido": round(float(totales['total_pedido']), 2),
                        "total_anulado": round(float(totales['total_anulado']), 2),
                        "total_cobrado": round(float(totales['total_cobrado']), 2),
                    }
                }
                
                resultado["mesas"].append(mesa_data)
                
            except Exception as e:
                logger.error(f"Error procesando ID {table_id}: {e}", exc_info=True)
                resultado["errores"].append(f"Error interno al procesar ID {table_id}: {str(e)}")
        
        resultado["total_mesas"] = len(resultado["mesas"])
        return resultado
        
    except Exception as e:
        logger.error(f"Error en get_table_items_by_id: {e}", exc_info=True)
        return {"error": "Error interno al obtener los artículos de las mesas."}


@tool
@db_connection_handler
def get_average_ticket_per_table() -> Dict[str, Any]:
    """
    Calcula la media de ticket por mesa, asumiendo que cada línea es una unidad.

    El total se calcula sumando el precio de cada línea no anulada (estados
    'P', 'M' y 'C'). El conteo de artículos es el número total de estas líneas.

    Returns:
        Un diccionario con estadísticas agregadas de las mesas abiertas.
    """
    send_tool_message("Calculando media de ticket por mesa (contando líneas)...")
    try:
        # Filtro para líneas que no están anuladas.
        active_lines_filter = Q(infmesa__lineaspedido__estado__in=['P', 'M', 'C'])

        mesas_queryset = Mesasabiertas.objects.annotate(
            # CORRECCIÓN: Sumar directamente el precio de cada línea.
            total_ticket=Coalesce(
                Sum(
                    'infmesa__lineaspedido__precio',
                    filter=active_lines_filter,
                    output_field=FloatField()
                ),
                Value(0.0)
            ),
            
        ).values(
            'mesa__id',
            'mesa__nombre',
            'total_ticket',
        )

        tables_list = list(mesas_queryset)

        if not tables_list:
            return {
                "total_tables": 0,
                "tables_detail": [],
                "average_ticket": 0.0,
                "total_revenue": 0.0,
            }

        total_revenue = sum(table['total_ticket'] for table in tables_list)
        num_tables = len(tables_list)
        average_ticket = total_revenue / num_tables if num_tables > 0 else 0.0

        tables_detail = [
            {
                "id": t['mesa__id'],
                "nombre": t['mesa__nombre'],
                "total_ticket": round(t['total_ticket'], 2),
            } for t in tables_list
        ]

        return {
            "total_tables": num_tables,
            "tables_detail": tables_detail,
            "average_ticket": round(average_ticket, 2),
            "total_revenue": round(total_revenue, 2)
        }

    except Exception as e:
        logger.error(f"Error en get_average_ticket_per_table: {e}", exc_info=True)
        return {"error": f"Error interno al calcular la media de ticket por mesa. {e}"}
    


@tool
@db_connection_handler
def get_open_tables_by_zones() -> Dict[str, Any]:
    """
    Obtiene las mesas abiertas agrupadas por zonas, con un desglose de totales
    idéntico al de la función get_open_tables para cada mesa.

    Esta función es muy eficiente, ya que realiza una única consulta para
    obtener todas las mesas con sus totales calculados y luego las agrupa
    por zona en memoria.

    Returns:
        Un diccionario que contiene el recuento total de mesas y dos listas:
        - zones: Una lista de zonas, cada una con sus mesas asociadas y sus totales.
        - tables_without_zone: Una lista de mesas sin zona asignada.
    """
    send_tool_message("Agrupando mesas abiertas por zonas con totales...")
    try:
        # 1. Anota los totales en cada mesa y pre-carga las zonas relacionadas
        mesas_con_totales = Mesasabiertas.objects.annotate(
            total_pedido=Coalesce(Sum('infmesa__lineaspedido__precio', filter=Q(infmesa__lineaspedido__estado__in=['P','M'])), Value(0), output_field=FloatField()),
            total_anulado=Coalesce(Sum('infmesa__lineaspedido__precio', filter=Q(infmesa__lineaspedido__estado='A')), Value(0), output_field=FloatField()),
            total_cobrado=Coalesce(Sum('infmesa__lineaspedido__precio', filter=Q(infmesa__lineaspedido__estado='C')), Value(0), output_field=FloatField()),
        ).select_related('mesa', 'infmesa').prefetch_related('mesa__mesaszona_set__zona')

        # 2. Agrupa los resultados en memoria (muy rápido)
        zones_dict = {}
        tables_without_zone = []

        for mesa in mesas_con_totales:
            mesa_data = {
                "id": mesa.mesa.id,
                "uid": mesa.infmesa.pk,
                "nombre": mesa.mesa.nombre,
                "total_mesa": {
                    "total_pedido": round(mesa.total_pedido, 2),
                    "total_anulado": round(mesa.total_anulado, 2),
                    "total_cobrado": round(mesa.total_cobrado, 2),
                }
            }
            
            # Obtiene la zona de los datos pre-cargados
            mesaszona_list = list(mesa.mesa.mesaszona_set.all())
            if mesaszona_list and mesaszona_list[0].zona:
                zona = mesaszona_list[0].zona
                if zona.id not in zones_dict:
                    zones_dict[zona.id] = {
                        "zone_id": zona.id,
                        "zone_name": zona.nombre,
                        "tables_count": 0,
                        "tables": []
                    }
                zones_dict[zona.id]["tables"].append(mesa_data)
                zones_dict[zona.id]["tables_count"] += 1
            else:
                tables_without_zone.append(mesa_data)
        
        return {
            "total_open_tables": mesas_con_totales.count(),
            "zones": sorted(zones_dict.values(), key=lambda x: x["zone_name"]),
            "tables_without_zone": tables_without_zone
        }
        
    except Exception as e:
        logger.error(f"Error en get_open_tables_by_zones: {e}", exc_info=True)
        return {"error": "Error interno al obtener las mesas abiertas por zonas."}

@tool
@db_connection_handler
def borrar_lineas_pedido(
    lineas_data: List[Dict]
):
    """
    Borra una o varias líneas de pedido de las mesas abiertas buscando por descripción.
    Evita que las descricpiones no sean en prural cuando te digo que borres varias lineas.
    En lenguaje natural, el usuario puede pedir borra todos los cafes y la busqueda se hace por cafe
    
    Args:
        lineas_data (list): Lista de diccionarios con datos de las líneas a borrar.
            Cada diccionario debe contener:
            - mesa_id (int): ID de la mesa donde está la línea
            - descripcion (str): Descripción de la línea a buscar y borrar
            - cantidad (int, opcional): Cantidad de líneas a borrar (por defecto 1, ignorado si all=True)
            - all (bool, opcional): Si es True, borra TODAS las líneas que coincidan con la descripción (por defecto False)
    
    Note:
        - El camarero se asigna automáticamente buscando primero un camarero activo con permisos de borrar,
          y si no se encuentra, se usa el primer camarero activo disponible.
        - El motivo se establece automáticamente como "Error de pedido borrado por el administrador".
        - Si no se encuentra la descripción, se devuelven todas las descripciones disponibles en la mesa.
        - Cuando all=True, se ignora el parámetro cantidad y se borran todas las líneas encontradas.
    
    Returns:
        list: Lista de resultados para cada línea procesada con:
            - status: 'success', 'error' o 'not_found'
            - mesa_id: ID de la mesa procesada
            - lineas_restantes: Número de líneas restantes en la mesa (0 si se cerró)
            - message: Mensaje descriptivo del resultado
            - descripciones_disponibles: Lista de descripciones disponibles (solo si status='not_found')
            - camarero_usado: Información del camarero que realizó la operación
            - lineas_borradas: Número de líneas borradas exitosamente
    """
    send_tool_message(f"Borrando {len(lineas_data)} líneas de pedido por descripción...")
    
    results = []
    
    # Buscar primer camarero activo con permisos de borrar
    camarero_con_permisos = Camareros.objects.filter(
        activo=True,
        permisos__icontains='borrar'
    ).first()
    
    if camarero_con_permisos:
        camarero_seleccionado = camarero_con_permisos
        tipo_camarero = "con permisos de borrar"
    else:
        # Si no hay camareros con permisos de borrar, usar el primer camarero activo
        primer_camarero = Camareros.objects.filter(activo=True).first()
        if not primer_camarero:
            return [{
                'status': 'error',
                'mesa_id': 'sistema',
                'message': 'No se encontraron camareros activos en el sistema',
                'camarero_usado': None
            }]
        camarero_seleccionado = primer_camarero
        tipo_camarero = "activo (sin permisos específicos)"
    
    camarero_id = camarero_seleccionado.id
    camarero_info = f"{camarero_seleccionado.nombre} {camarero_seleccionado.apellidos} ({tipo_camarero})"
    
    for linea_data in lineas_data:
        try:
            # Validar datos requeridos
            mesa_id = linea_data.get('mesa_id')
            descripcion = linea_data.get('descripcion')
            cantidad = linea_data.get('cantidad', 1)
            borrar_todas = linea_data.get('all', False)  # Nuevo parámetro para borrar todas
            
            # Motivo fijo establecido por el administrador
            motivo = "Error de pedido borrado por el administrador"
            
            if not mesa_id or not descripcion:
                results.append({
                    'status': 'error',
                    'mesa_id': mesa_id,
                    'lineas_borradas': 0,
                    'message': 'Datos incompletos: mesa_id y descripcion son requeridos',
                    'camarero_usado': camarero_info
                })
                continue
            
            # Buscar la mesa abierta
            mesa_abierta = Mesasabiertas.objects.filter(mesa__id=mesa_id).first()
            if not mesa_abierta:
                results.append({
                    'status': 'error',
                    'mesa_id': mesa_id,
                    'lineas_borradas': 0,
                    'message': f'Mesa {mesa_id} no encontrada o no está abierta',
                    'camarero_usado': camarero_info
                })
                continue
            
            # Buscar líneas con la descripción especificada en estados activos (P, M)
            lineas_encontradas = mesa_abierta.infmesa.lineaspedido_set.filter(
                descripcion__icontains=descripcion,
                estado__in=['P', 'M']
            ).order_by('id')  # Ordenar por ID para procesar las más antiguas primero
            
            if not lineas_encontradas.exists():
                # No se encontró la descripción, devolver todas las descripciones disponibles
                todas_las_lineas = mesa_abierta.infmesa.lineaspedido_set.filter(
                    estado__in=['P', 'M']
                ).values_list('descripcion', flat=True).distinct()
                
                results.append({
                    'status': 'not_found',
                    'mesa_id': mesa_id,
                    'lineas_borradas': 0,
                    'message': f'No se encontró ninguna línea con descripción que contenga "{descripcion}" en mesa {mesa_id}',
                    'descripciones_disponibles': list(todas_las_lineas),
                    'camarero_usado': camarero_info
                })
                continue
            
            # Procesar las líneas encontradas según la cantidad solicitada o todas si all=True
            lineas_procesadas = 0
            lineas_borradas_exitosas = 0
            
            # Determinar cuántas líneas procesar
            if borrar_todas:
                lineas_a_procesar = lineas_encontradas  # Procesar todas las encontradas
                mensaje_cantidad = f"todas las líneas ({lineas_encontradas.count()})"
            else:
                lineas_a_procesar = lineas_encontradas[:cantidad]  # Limitar a la cantidad solicitada
                mensaje_cantidad = f"{min(cantidad, lineas_encontradas.count())} líneas"
            
            for linea in lineas_a_procesar:
                try:
                    # Llamar al método estático de la clase con los datos de la línea encontrada
                    resultado = Lineaspedido.borrar_linea_pedido(
                        idm=mesa_id,
                        p=linea.precio,
                        idArt=linea.idart,  # Corregido: usar idart en lugar de id_articulo
                        can=1,  # Borrar de una en una
                        idc=camarero_id,
                        motivo=motivo,
                        s=linea.estado,  # Usar el estado real de la línea
                        n=linea.descripcion
                    )
                    
                    if resultado != -1:  # Si no hay error
                        lineas_borradas_exitosas += 1
                    
                    lineas_procesadas += 1
                    
                except Exception as e:
                    logger.error(f"Error borrando línea individual: {e}", exc_info=True)
                    continue
            
            # Calcular líneas restantes después del borrado
            lineas_restantes_query = mesa_abierta.infmesa.lineaspedido_set.filter(estado__in=['P', 'M'])
            lineas_restantes = lineas_restantes_query.count()
            
            if lineas_restantes == 0:
                results.append({
                    'status': 'success',
                    'mesa_id': mesa_id,
                    'lineas_restantes': 0,
                    'lineas_borradas': lineas_borradas_exitosas,
                    'message': f'Mesa {mesa_id} cerrada automáticamente - se borraron {mensaje_cantidad} con descripción "{descripcion}", no quedan líneas activas',
                    'camarero_usado': camarero_info
                })
            else:
                results.append({
                    'status': 'success',
                    'mesa_id': mesa_id,
                    'lineas_restantes': lineas_restantes,
                    'lineas_borradas': lineas_borradas_exitosas,
                    'message': f'Se borraron {mensaje_cantidad} con descripción "{descripcion}" de mesa {mesa_id}. Quedan {lineas_restantes} líneas activas',
                    'camarero_usado': camarero_info
                })
                
        except Exception as e:
            logger.error(f"Error borrando línea de pedido: {e}", exc_info=True)
            results.append({
                'status': 'error',
                'mesa_id': linea_data.get('mesa_id', 'desconocido'),
                'lineas_borradas': 0,
                'message': f'Error interno: {str(e)}',
                'camarero_usado': camarero_info
            })
    
    send_tool_message(f"Procesadas {len(results)} líneas de pedido usando camarero: {camarero_info}")
    return results


@tool 
@db_connection_handler
def borrar_mesas_completas(
    mesas_data: List[Dict]
):
    """
    Anula una o varias mesas abiertas completas, anulando todas sus líneas de pedido.
    
    Args:
        mesas_data (list): Lista de diccionarios con datos de las mesas a borrar.
            Cada diccionario debe contener:
            - mesa_id (int): ID de la mesa a borrar
            - motivo (str, opcional): Motivo del borrado. Por defecto: "Error de pedido borrado por el administrador"
    
    Note:
        - El camarero se asigna automáticamente buscando primero un camarero activo con permisos de borrar,
          y si no se encuentra, se usa el primer camarero activo disponible.
    
    Returns:
        list: Lista de resultados para cada mesa procesada con:
            - status: 'success' o 'error'
            - mesa_id: ID de la mesa procesada
            - message: Mensaje descriptivo del resultado
            - camarero_usado: Información del camarero que realizó la operación
    """
    send_tool_message(f"Borrando {len(mesas_data)} mesas completas...")
    
    results = []

    # Buscar primer camarero activo con permisos de borrar
    camarero_con_permisos = Camareros.objects.filter(
        activo=True,
        permisos__icontains='borrar'
    ).first()
    
    if camarero_con_permisos:
        camarero_seleccionado = camarero_con_permisos
        tipo_camarero = "con permisos de borrar"
    else:
        # Si no hay camareros con permisos de borrar, usar el primer camarero activo
        primer_camarero = Camareros.objects.filter(activo=True).first()
        if not primer_camarero:
            return [{
                'status': 'error',
                'mesa_id': 'sistema',
                'message': 'No se encontraron camareros activos en el sistema',
                'camarero_usado': None
            }]
        camarero_seleccionado = primer_camarero
        tipo_camarero = "activo (sin permisos específicos)"
    
    camarero_id = camarero_seleccionado.id
    camarero_info = f"{camarero_seleccionado.nombre} {camarero_seleccionado.apellidos} ({tipo_camarero})"
    
    for mesa_data in mesas_data:
        try:
            # Validar datos requeridos
            mesa_id = mesa_data.get('mesa_id')
            
            # Motivo personalizable o por defecto
            motivo = mesa_data.get('motivo', "Error de pedido borrado por el administrador")
            
            if not mesa_id:
                results.append({
                    'status': 'error',
                    'mesa_id': mesa_id,
                    'message': 'Datos incompletos: mesa_id es requerido',
                    'camarero_usado': camarero_info
                })
                continue
            
            # Verificar que la mesa existe y está abierta
            mesa_abierta = Mesasabiertas.objects.filter(mesa__id=mesa_id).first()
            if not mesa_abierta:
                results.append({
                    'status': 'error',
                    'mesa_id': mesa_id,
                    'message': f'Mesa {mesa_id} no encontrada o no está abierta',
                    'camarero_usado': camarero_info
                })
                continue
            
            mesa_nombre = mesa_abierta.mesa.nombre
            
            # Llamar al método estático de la clase
            Mesasabiertas.borrar_mesa_abierta(
                idm=mesa_id,
                idc=camarero_id,
                motivo=motivo
            )
            
            results.append({
                'status': 'success',
                'mesa_id': mesa_id,
                'message': f'Mesa "{mesa_nombre}" (ID: {mesa_id}) borrada completamente. Motivo: {motivo}',
                'camarero_usado': camarero_info
            })
                
        except Exception as e:
            logger.error(f"Error borrando mesa completa: {e}", exc_info=True)
            results.append({
                'status': 'error',
                'mesa_id': mesa_data.get('mesa_id', 'desconocido'),
                'message': f'Error interno: {str(e)}',
                'camarero_usado': camarero_info
            })
    
    send_tool_message(f"Procesadas {len(results)} mesas usando camarero: {camarero_info}")
    return results


@tool
@db_connection_handler
def obtener_historial_nulos(
    limite: int = 50
):
    """
    Obtiene el historial de líneas anuladas (nulos) con información detallada.
    
    Args:
        limite (int): Número máximo de registros a retornar (por defecto 50)
    
    Returns:
        dict: Diccionario con:
            - total_registros: Número total de nulos encontrados
            - total_euros: Total en euros de todas las líneas anuladas
            - nulos: Lista de diccionarios con información de cada nulo:
                - id: ID del registro de nulo
                - fecha_anulacion: Fecha y hora de anulación
                - motivo: Motivo de la anulación
                - lineapedido: Información de la línea anulada:
                    - precio: Precio de la línea
                    - descripcion: Descripción del artículo
                    - mesa_nombre: Nombre de la mesa
    """
    send_tool_message(f"Obteniendo historial de nulos (últimos {limite})...")
    
    try:
        
        # Obtener los nulos ordenados por fecha más reciente
        nulos = Historialnulos.objects.select_related(
            'camarero', 'lineapedido__infmesa__camarero'
        ).order_by('-id')[:limite]
        
        if not nulos:
            return {
                "total_registros": 0,
                "total_euros": 0.0,
                "nulos": []
            }
        
        lista_nulos = []
        total_euros = 0.0
        
        for nulo in nulos:
            try:
                # Calcular precio total de la línea anulada
                total_euros += float(nulo.lineapedido.precio)
                
                # Obtener información de la mesa física usando el UID
                mesa_id = 0
                mesa_nombre = "Mesa desconocida"
                # El UID tiene formato: "algo-id_mesa_fisica", extraemos el ID de la mesa
                if nulo.lineapedido.infmesa and nulo.lineapedido.infmesa.pk:
                    uid_parts = str(nulo.lineapedido.infmesa.pk).split('-')
                    if len(uid_parts) >= 2:
                        try:
                            mesa_id = int(uid_parts[0])  # El ID de la mesa física está en la posición 0
                            # Buscar la mesa física directamente por su ID
                            
                            mesa_fisica = Mesas.objects.filter(id=mesa_id).first()
                            if mesa_fisica:
                                mesa_nombre = mesa_fisica.nombre
                        except (ValueError, IndexError):
                            # Si hay error en la conversión o el formato del UID
                            logger.warning(f"No se pudo extraer ID de mesa del UID: {nulo.lineapedido.infmesa.pk}")
                            mesa_id = 0
                
                # Obtener fecha desde el infmesa o usar fecha del pedido
                fecha_anulacion = f"{nulo.lineapedido.infmesa.fecha if nulo.lineapedido.infmesa else 'Sin fecha'} {nulo.hora}"
                
                nulo_info = {
                    "id": nulo.id,
                    "fecha_anulacion": fecha_anulacion,
                    "motivo": nulo.motivo,
                    "lineapedido": {
                        "precio": float(nulo.lineapedido.precio),
                        "descripcion": nulo.lineapedido.descripcion,
                        "mesa_nombre": mesa_nombre,
                    }
                }
                
                lista_nulos.append(nulo_info)
                
            except Exception as e:
                logger.error(f"Error procesando nulo {nulo.id}: {e}", exc_info=True)
                continue
        
        resultado = {
            "total_registros": len(lista_nulos),
            "total_euros": round(total_euros, 2),
            "nulos": lista_nulos
        }
        
        send_tool_message(f"Encontrados {len(lista_nulos)} nulos por un total de {round(total_euros, 2)}€")
        return resultado
        
    except Exception as e:
        logger.error(f"Error en obtener_historial_nulos: {e}", exc_info=True)
        return {"error": "Error interno al obtener el historial de nulos."}

@tool
@db_connection_handler
def obtener_desglose_nulo_por_id(
    nulo_id: int
):
    """
    Obtiene el desglose completo del infmesa asociado a un nulo específico.
    
    Args:
        nulo_id (int): ID del registro de nulo
    
    Returns:
        dict: Diccionario con:
            - infmesa: Información completa del infmesa:
                - uid: UID del infmesa
                - fecha: Fecha de apertura
                - hora: Hora de apertura
                - camarero: Camarero responsable el que abrió la masa
                - mesa_nombre: Nombre de la mesa física
                - total_pedido: Total en euros del pedido
                - total_anulado: Total anulado
                - total_cobrado: Total cobrado
                - total_regalado: Total regalado
                - pedidos: Lista completa de pedidos con todas las líneas
    """
    send_tool_message(f"Obteniendo desglose completo del nulo ID {nulo_id}...")
    
    try:
        # Obtener el nulo específico
        nulo = Historialnulos.objects.filter(id=nulo_id).first()
        
        if not nulo:
            return {"error": f"No se encontró el nulo con ID {nulo_id}"}
        
        # Obtener el infmesa asociado
        infmesa = nulo.lineapedido.infmesa
        if not infmesa:
            return {"error": f"El nulo {nulo_id} no tiene un infmesa asociado"}
        
        # Extraer información de la mesa del UID
        mesa_id = 0
        mesa_nombre = "Mesa desconocida"
        if infmesa.pk:
            uid_parts = str(infmesa.pk).split('-')
            if len(uid_parts) >= 2:
                try:
                    mesa_id = int(uid_parts[0])
                    mesa_fisica = Mesas.objects.filter(id=mesa_id).first()
                    if mesa_fisica:
                        mesa_nombre = mesa_fisica.nombre
                except (ValueError, IndexError):
                    logger.warning(f"No se pudo extraer ID de mesa del UID: {infmesa.pk}")
        
        # Calcular totales del infmesa
        
        totales = infmesa.lineaspedido_set.aggregate(
            total_pedido=Coalesce(Sum('precio', filter=Q(estado__in=['P','M'])), Value(0), output_field=FloatField()),
            total_anulado=Coalesce(Sum('precio', filter=Q(estado='A')), Value(0), output_field=FloatField()),
            total_cobrado=Coalesce(Sum('precio', filter=Q(estado='C')), Value(0), output_field=FloatField()),
         )
        
        # Obtener todos los pedidos del infmesa con sus líneas
        pedidos_completos = infmesa.get_pedidos()
        
        
        # Información completa del infmesa
        infmesa_info = {
            "uid": infmesa.pk,
            "fecha": infmesa.fecha,
            "hora": infmesa.hora,
            "camarero": f"{infmesa.camarero.nombre} {infmesa.camarero.apellidos}",
            "mesa_nombre": mesa_nombre,
            "totales": {
                "total_pedido": round(float(totales['total_pedido']), 2),
                "total_anulado": round(float(totales['total_anulado']), 2),
                "total_cobrado": round(float(totales['total_cobrado']), 2),
            },
            "pedidos": pedidos_completos
        }
        
        resultado = {
            "infmesa": infmesa_info
        }
        
        send_tool_message(f"Desglose completo obtenido para nulo {nulo_id} - Mesa {mesa_nombre}")
        return resultado
        
    except Exception as e:
        logger.error(f"Error en obtener_desglose_nulo_por_id({nulo_id}): {e}", exc_info=True)
        return {"error": f"Error interno al obtener el desglose del nulo {nulo_id}."}

tools =[
    get_open_tables,
    get_table_items_by_uid,
    get_table_items_by_id,
    get_table_items_by_name,
    get_average_ticket_per_table,
    get_open_tables_by_zones,
    borrar_lineas_pedido,
    borrar_mesas_completas,
    obtener_historial_nulos,
    obtener_desglose_nulo_por_id,
]