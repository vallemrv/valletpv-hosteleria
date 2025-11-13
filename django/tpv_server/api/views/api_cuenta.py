# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T15:08:41+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-01T02:14:29+02:00
# @License: Apache License v2.0


from django.db.models import Q, Sum, Count
from tokenapi.http import JsonResponse
from django.http import HttpResponse
from api.tools import send_imprimir_ticket
from api.decorators.uid_activo import verificar_uid_activo
from gestion.models.infmesa import Infmesa
from gestion.models.pedidos import Lineaspedido, Pedidos
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.ticket import Ticket
from datetime import datetime
from gestion.tools.config_logs import logger_sync 
from uuid import uuid4
import json


@verificar_uid_activo
def get_cuenta(request):
        
    id = request.POST['idm'] if 'idm' in request.POST else request.POST["mesa_id"]  # ID de la mesa
    reg = json.loads(request.POST['reg'])  # Datos recibidos del cliente
    

    # Obtener las líneas de pedido del servidor
    m_abierta = Mesasabiertas.objects.filter(mesa__pk=id).first()
    lstArt = []

   
    if m_abierta:
        lstArt = m_abierta.get_lineaspedido()
        if len(lstArt) == 0 and len(reg) == 0:
            logger_sync.debug(f"GET_CUENTA ___ No se encontraron líneas de pedido para la mesa {id}")
            m_abierta.delete()
            return JsonResponse({'soniguales': False, 'delta': {'inserts': [], 'updates': [], 'deletes': []}})   

    # Campos a comparar y sus tipos de datos
    campos_a_comparar = {
        'ID': int,
        'IDPedido': int,
        'IDArt': int,
        'Estado': str,
        'Precio': float,  # Precio siempre será comparado como float
        'Descripcion': str,
        'IDMesa': int,
        'nomMesa': str,
        'IDZona': int,
        'servido': int,
        'descripcion_t': str
    }

    # Función para comparar dos diccionarios con conversiones de tipo específicas
    def comparar_lineas(linea_cliente, linea_servidor):
        for campo, tipo in campos_a_comparar.items():
            valor_cliente = linea_cliente.get(campo, None)
            valor_servidor = linea_servidor.get(campo, None)
            
            # Si ambos valores son None, son iguales
            if valor_cliente is None and valor_servidor is None:
                continue
            
            # Si uno es None y el otro no, son diferentes
            if valor_cliente is None or valor_servidor is None:
                logger_sync.debug(f"Valores diferentes para {campo} en get_cuenta (uno es None): Cliente={valor_cliente}, Servidor={valor_servidor}")
                return False
            
            # Convertir ambos valores al tipo esperado
            try:
                valor_cliente_convertido = tipo(valor_cliente)
                valor_servidor_convertido = tipo(valor_servidor)
            except (ValueError, TypeError) as e:
                logger_sync.debug(f"Error de conversión para {campo} en get_cuenta: Cliente={valor_cliente} (tipo: {type(valor_cliente)}), Servidor={valor_servidor} (tipo: {type(valor_servidor)}), Error: {e}")
                return False  # Si falla la conversión, consideramos los valores diferentes
            
            # Comparar los valores convertidos
            if valor_cliente_convertido != valor_servidor_convertido:
                logger_sync.debug(f"Valores diferentes para {campo} en get_cuenta: Cliente={valor_cliente} -> {valor_cliente_convertido}, Servidor={valor_servidor} -> {valor_servidor_convertido}")
                return False

        return True
    
    
    # Crear una copia de reg para poder modificarla
    reg_copia = reg.copy()
    inserts = []
    updates = []
    deletes = []
    
    # Comparar cada elemento de lstArt buscándolo en reg por ID
    for linea_servidor in lstArt:
        encontrado = False
        for i, linea_cliente in enumerate(reg_copia):
            # Buscar por ID - convertir ambos a int para comparar
            id_cliente = linea_cliente.get('ID')
            id_servidor = linea_servidor.get('ID')
            
            # Convertir IDs a entero para comparación
            try:
                id_cliente_int = int(id_cliente) if id_cliente is not None else None
                id_servidor_int = int(id_servidor) if id_servidor is not None else None
            except (ValueError, TypeError):
                continue  # Si no se puede convertir, continuar con el siguiente
            
            if id_cliente_int == id_servidor_int:
                # Si encontramos el ID, comparamos las líneas
                if not comparar_lineas(linea_cliente, linea_servidor):
                    # Si las líneas son diferentes, agregar a updates
                    updates.append(linea_servidor)
                    logger_sync.debug(f"GET_CUENTA ___ ID {linea_servidor.get('ID')} diferentes. Cliente: {linea_cliente}, Servidor: {linea_servidor}")
                # Eliminar de reg_copia ya que lo hemos procesado
                reg_copia.pop(i)
                encontrado = True
                break
        
        # Si no encontramos el ID en reg, agregar a inserts
        if not encontrado:
            inserts.append(linea_servidor)
            logger_sync.debug(f"GET_CUENTA ___ ID {linea_servidor.get('ID')} no encontrado en reg, agregar a inserts.")
    
    # Si quedan elementos en reg_copia, agregar a deletes
    for linea_extra in reg_copia:
        deletes.append({'ID': linea_extra.get('ID')})
        logger_sync.debug(f"GET_CUENTA ___ ID {linea_extra.get('ID')} extra en cliente, agregar a deletes.")
    
    # Si hay diferencias, devolver delta
    if inserts or updates or deletes:
        delta = {'inserts': inserts, 'updates': updates, 'deletes': deletes}
        logger_sync.debug(f"GET_CUENTA ___ Diferencias encontradas. Delta: {delta}")
        return JsonResponse({'soniguales': False, 'delta': delta})

    # Si todas las líneas son iguales
    logger_sync.debug("GET_CUENTA ___ Todas las líneas son iguales.")
    return JsonResponse({'soniguales': True, 'delta': {'inserts': [], 'updates': [], 'deletes': []}})


@verificar_uid_activo
def juntarmesas(request):
    Mesasabiertas.juntar_mesas_abiertas( request.POST["idp"], request.POST["ids"])
    return HttpResponse('success')

@verificar_uid_activo
def cambiarmesas(request):
    Mesasabiertas.cambiar_mesas_abiertas(request.POST["idp"], request.POST["ids"])
    return HttpResponse('success')



@verificar_uid_activo
def mvlineamultiple(request):
    idm = request.POST["idm"]
    idLineas = json.loads(request.POST["idlineas"])  # Array de IDs de líneas
    
    if not idLineas:
        return HttpResponse('success')
    
    # Obtener todas las líneas de una vez
    lineas = Lineaspedido.objects.filter(pk__in=idLineas)
    
    if not lineas.exists():
        return HttpResponse('success')
    
    # Obtener datos comunes de la primera línea
    primera_linea = lineas.first()
    pedido = Pedidos.objects.get(pk=primera_linea.pedido_id)
    idc = pedido.camarero_id
    uid = primera_linea.infmesa.uid
    
    # Modificar composición de todas las líneas
    for linea in lineas:
        linea.modifiar_composicion()
    
    infmesa_aux = primera_linea.infmesa
    
    # Obtener o crear mesa destino
    mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
    if not mesa:
        infmesa = Infmesa()
        infmesa.camarero_id = idc
        infmesa.hora = datetime.now().strftime("%H:%M")
        infmesa.fecha = datetime.now().strftime("%Y/%m/%d")
        infmesa.id = f"{idm}-{str(uuid4())}"
        infmesa.save()
        mesa = Mesasabiertas()
        mesa.infmesa_id = infmesa.pk
        mesa.mesa_id = idm
        mesa.save()
       
    # Crear nuevo pedido
    pedido_destino = Pedidos()
    pedido_destino.infmesa_id = mesa.infmesa.pk
    pedido_destino.hora = datetime.now().strftime("%H:%M")
    pedido_destino.camarero_id = idc
    pedido_destino.save()

    # Mover todas las líneas al nuevo pedido
    for linea in lineas:
        linea.infmesa_id = mesa.infmesa.pk
        linea.pedido_id = pedido_destino.pk
        linea.save()
      
    # Recomponer artículos
    infmesa_aux.componer_articulos()
    pedido_destino.infmesa.componer_articulos()

    # Verificar si quedan artículos en la mesa origen
    numart = Lineaspedido.objects.filter((Q(estado='P') | Q(estado='R')) & Q(infmesa__pk=uid)).count()
    if numart <= 0:
        for m in Mesasabiertas.objects.filter(infmesa__pk=uid):
            obj = m.serialize()
            obj["abierta"] = 0
            obj["num"] = 0
            m.delete()
        

    return HttpResponse('success')

@verificar_uid_activo
def cuenta_add(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    uid_device = request.POST.get("uid_device", str(uuid4()))
    lineas = json.loads(request.POST["pedido"])
    
    if not lineas:
        return JsonResponse({"status": "success", "uid_device": uid_device})
    
    pedido = Pedidos.agregar_nuevas_lineas(idm, idc, lineas, uid_device)
    if pedido:
        return JsonResponse({"status": "success", "uid_device": uid_device})
    else:
        return JsonResponse({"status": "duplicate", "uid_device": uid_device})

@verificar_uid_activo
def cuenta_cobrar(request):
    idm = request.POST["idm"]
    idc = request.POST["idc"]
    entrega = request.POST["entrega"]
    recibo = request.POST["recibo"] if "recibo" in  request.POST else ""
    idsCobrados = json.loads(request.POST["idsCobrados"])
    if idsCobrados:
        total, id = Ticket.cerrar_cuenta(idm, idc, entrega, idsCobrados=idsCobrados, recibo=recibo)
         
    if (id > 0):
        send_imprimir_ticket(request, id)
        
    return JsonResponse({"totalcobro": str(total), "entrega": entrega})


@verificar_uid_activo
def cuenta_rm_linea(request):
    idsABorrar = json.loads(request.POST["idsABorrar"])
    idCam = request.POST["idc"]
    idMesa = request.POST["idm"]
    motivo = request.POST["motivo"] if "motivo" in request.POST else ""
    Lineaspedido.borrar_linea_pedido_by_ids(idMesa, idsABorrar, idCam, motivo)
    return HttpResponse('success')


@verificar_uid_activo
def cuenta_ls_ticket(request):
    offset = request.POST['offset'] if 'offset' in request.POST else 0

    rows = Ticket.objects.all().order_by("-id")[offset:50]
    
    lineas = []
    
    for r in rows:
        total = r.ticketlineas_set.aggregate(total=Sum("linea__precio"))["total"]
        
        lineas.append({
            'ID': r.id,
            'Fecha': r.fecha,
            'Hora': r.hora,
            'Entrega': float(r.entrega),
            'Mesa': r.mesa,
            'Total': float(total),
            })
    

    return JsonResponse(lineas)

@verificar_uid_activo
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
            "Precio": float(r["linea__precio"]),
            "Total": float(r["total"]),
            "Can": r["can"]
        })


    total = ticket.ticketlineas_set.aggregate(total=Sum("linea__precio"))["total"]
    return JsonResponse({'lineas':lineas, 'total': float(total), 'IDTicket': id})