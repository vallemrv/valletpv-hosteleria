# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:52:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-26T14:53:11+02:00
# @License: Apache License v2.0

from django.db.models import Count, Q
from comunicacion.tools import comunicar_cambios_devices
from tokenapi.http import JsonResponse
from api.decorators.uid_activo import verificar_uid_activo

from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.camareros import Camareros
from gestion.models.familias import Receptores
from gestion.models.pedidos import Pedidos, Lineaspedido, Servidos

from api.tools.smart_receptor import notificar_lineas_servidas
from api.tools import is_float
import json


@verificar_uid_activo
def servido(request):
    # --- 1. Preparación de datos ---
    try:
        articles_data = json.loads(request.POST["art"])
        if not isinstance(articles_data, list):
            articles_data = [articles_data]
    except (json.JSONDecodeError, KeyError):
        return JsonResponse({"error": "Datos de artículo inválidos o faltantes."}, status=400)

    # --- 2. Consulta Única y Eficiente a la BD ---
    query = Q()
    for art in articles_data:
        if "IDArt" in art and "IDPedido" in art:
             query |= Q(idart=art["IDArt"], pedido_id=art["IDPedido"])
    
    if not query:
        return JsonResponse({})
        
    lineas_a_servir = Lineaspedido.objects.filter(query)

    # --- 3. Procesamiento y Guardado ---
    result_rm = []

    for linea in lineas_a_servir:
        # Crear registro de servido (el modelo se encarga de comunicación "md")
        Servidos.objects.create(linea=linea)
        
        # Gestionar comunicación "rm" si es necesario
        result_rm.append({"ID": linea.pk})

    # --- 4. Comunicación Final ---
    # Notificar a smart receptors (pasamos las líneas directamente, sin consultar BD de nuevo)
    if lineas_a_servir:
        notificar_lineas_servidas(lineas_a_servir)
    
    # Comunicar cambios 'rm' a devices
    if result_rm:
        comunicar_cambios_devices("rm", "lineaspedido", result_rm)
    
    return JsonResponse({"status": "ok", "message": "Operación completada."})

@verificar_uid_activo
def get_pedidos_by_receptor(request):
    """
    Recibe lista de pedidos con sus líneas del cliente y sincroniza con el servidor.
    Formato: {"receptor": "nombre_receptor", "pedidos": [{"pedido_id": 789, "lineas": [1234, 1235]}]}
    """
    receptor_nombre = request.POST.get("receptor")
    pedidos_cliente = json.loads(request.POST.get("pedidos_locales", "[]"))
   
    
    # Obtener UIDs de mesas abiertas
    mesas_abiertas = Mesasabiertas.objects.select_related('infmesa')
    mesas_abiertas_uids = set(ma.infmesa.id for ma in mesas_abiertas)
    print(f"Mesas abiertas UIDs: {mesas_abiertas_uids}")
    
    # Obtener todas las líneas del servidor para este receptor en mesas abiertas
    lineas_servidor = Lineaspedido.objects.filter(
        Q(tecla__familia__receptor__nomimp=receptor_nombre) & 
        Q(infmesa__id__in=mesas_abiertas_uids) &
        (Q(estado='P') | Q(estado='R') | Q(estado='M'))
    ).select_related(
        'pedido__infmesa',
        'tecla__familia__receptor'
    ).prefetch_related(
        'pedido__infmesa__mesasabiertas_set__mesa',
        'servidos_set'
    )
    
    print(f"Líneas encontradas en servidor: {lineas_servidor.count()}")
    if lineas_servidor.count() > 0:
        print(f"Primera línea - Receptor nomimp: {lineas_servidor.first().tecla.familia.receptor.nomimp if lineas_servidor.first().tecla else 'Sin tecla'}")
    
    # IDs de líneas y pedidos que el cliente tiene (ignorar duplicados)
    lineas_cliente_ids = set()
    pedidos_cliente_ids = set()
    for p in pedidos_cliente:
        pedidos_cliente_ids.add(p["pedido_id"])
        lineas_cliente_ids.update(p["lineas"])
    
    print(f"Pedidos únicos del cliente: {pedidos_cliente_ids}")
    print(f"Total líneas únicas del cliente: {len(lineas_cliente_ids)}")
    
    # Lista de IDs a eliminar en el cliente
    rm_ids = []
    
    # Si el cliente tiene líneas, verificarlas
    if lineas_cliente_ids:
        lineas_a_verificar = Lineaspedido.objects.filter(
            id__in=lineas_cliente_ids
        ).select_related('pedido__infmesa')
        
        for linea in lineas_a_verificar:
            # Eliminar si está cobrada o anulada
            if linea.estado in ['C', 'A']:
                rm_ids.append(linea.id)
                continue
            
            # Eliminar si es regalo pero la mesa no está abierta
            if linea.estado == 'R':
                if linea.pedido.infmesa.id not in mesas_abiertas_uids:
                    rm_ids.append(linea.id)
    
    # Organizar líneas del servidor por pedido
    pedidos_servidor = {}
    for linea in lineas_servidor:
        pedido_id = linea.pedido_id
        
        # Verificar si está servido
        servido = linea.servidos_set.exists()
        
        # Si el cliente ya tiene este pedido, solo enviar líneas nuevas
        if pedido_id in pedidos_cliente_ids:
            # Solo agregar líneas que el cliente NO tiene
            if linea.id not in lineas_cliente_ids:
                if pedido_id not in pedidos_servidor:
                    pedido = linea.pedido
                    camarero = Camareros.objects.get(pk=pedido.camarero_id)
                    mesa_abierta = pedido.infmesa.mesasabiertas_set.first()
                    
                    if not mesa_abierta:
                        continue
                    
                    receptor = linea.tecla.familia.receptor
                    
                    pedidos_servidor[pedido_id] = {
                        "op": "pedido",
                        "hora": pedido.hora,
                        "receptor": receptor.nomimp,
                        "nom_receptor": receptor.nombre,
                        "receptor_activo": receptor.activo,
                        "camarero": camarero.nombre + " " + camarero.apellidos,
                        "mesa": mesa_abierta.mesa.nombre,
                        "pedido_id": pedido_id,
                        "lineas": []
                    }
                
                pedidos_servidor[pedido_id]["lineas"].append({
                    "id": linea.id,
                    "idart": linea.idart,
                    "descripcion": linea.descripcion,
                    "estado": linea.estado,
                    "pedido_id": linea.pedido_id,
                    "servido": servido
                })
        else:
            # Pedido completo nuevo para el cliente
            if pedido_id not in pedidos_servidor:
                pedido = linea.pedido
                camarero = Camareros.objects.get(pk=pedido.camarero_id)
                mesa_abierta = pedido.infmesa.mesasabiertas_set.first()
                
                if not mesa_abierta:
                    continue
                
                receptor = linea.tecla.familia.receptor
                
                pedidos_servidor[pedido_id] = {
                    "op": "pedido",
                    "hora": pedido.hora,
                    "receptor": receptor.nomimp,
                    "nom_receptor": receptor.nombre,
                    "receptor_activo": receptor.activo,
                    "camarero": camarero.nombre + " " + camarero.apellidos,
                    "mesa": mesa_abierta.mesa.nombre,
                    "pedido_id": pedido_id,
                    "lineas": []
                }
            
            # Agregar línea al pedido
            pedidos_servidor[pedido_id]["lineas"].append({
                "id": linea.id,
                "idart": linea.idart,
                "descripcion": linea.descripcion,
                "estado": linea.estado,
                "pedido_id": linea.pedido_id,
                "servido": servido
            })
    
    resultado = {
        "pedidos": list(pedidos_servidor.values()),
        "rm": rm_ids
    }
    
    print(f"Respuesta: {len(resultado['pedidos'])} pedidos, {len(resultado['rm'])} líneas a eliminar")
    print(f"=== Fin get_pedidos_by_receptor ===\n")
    
    return JsonResponse(resultado)


