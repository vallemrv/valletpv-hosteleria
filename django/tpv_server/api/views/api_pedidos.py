# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:52:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-26T14:53:11+02:00
# @License: Apache License v2.0

from django.db.models import Count, Q
from comunicacion.tools import comunicar_cambios_devices
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.camareros import Camareros
from gestion.models.familias import Receptores
from gestion.models.pedidos import Pedidos, Lineaspedido, Servidos


from api.tools import is_float
import json


def find(id, lineas):
    for l in lineas:
        if is_float(l["ID"]) and int(l["ID"]) == int(id):
            lineas.remove(l)
            return l
        
    return None

@csrf_exempt
def  get_pendientes(request):
    if "idz" in request.POST:
        idz = request.POST["idz"]
        mesas = Mesasabiertas.objects.filter(mesa__mesaszona__zona__pk=idz).distinct()
    else:
        mesas = Mesasabiertas.objects.filter()

        
    lineas = json.loads(request.POST["lineas"]) if "lineas" in request.POST else json.loads(request.POST["reg"])
    
    result = []
    lineas_server = []
    
    for m in mesas:
        lineas_server = [*lineas_server, *m.get_lineaspedido()]

    
    for l in lineas_server:
        linea = find(l["ID"], lineas)
        if linea:
            if not Lineaspedido.is_equals(l, linea):
                result.append({'op':'md', 'tb':"lineaspedido", 'obj': l})
        else:
            result.append({'op':'insert', 'tb':"lineaspedido", 'obj': l})


   
    #Las linesa que no esten en el servidor o esten cobradas se borran.      
    for l in lineas:
        result.append({'op':'rm', 'tb':"lineaspedido", 'obj': {"ID":l["ID"]}})  

   
    return JsonResponse(result)



@csrf_exempt
def  comparar_lineaspedido(request):
    return get_pendientes(request)


@csrf_exempt # CUIDADO: Revisa si realmente necesitas desactivar la protección CSRF.
def servido(request):
    # --- 1. Preparación de datos (Igual que antes) ---
    try:
        articles_data = json.loads(request.POST["art"])
        if not isinstance(articles_data, list):
            articles_data = [articles_data]
    except (json.JSONDecodeError, KeyError):
        return JsonResponse({"error": "Datos de artículo inválidos o faltantes."}, status=400)

    # --- 2. Consulta Única y Eficiente a la BD (Mantenemos esta gran mejora) ---
    query = Q()
    for art in articles_data:
        if "IDArt" in art and "IDPedido" in art:
             query |= Q(idart=art["IDArt"], pedido_id=art["IDPedido"])
    
    if not query:
        return JsonResponse({})
        
    # Usamos prefetch_related para cargar los datos que 'serialize' pueda necesitar
    lineas_a_servir = Lineaspedido.objects.filter(query)

    # --- 3. Procesamiento y Guardado ---
    # La comunicación "md" se delega completamente al método .save() del modelo.
    # La vista solo se preocupa de la comunicación "rm".
    
    result_rm = [] # Solo necesitamos esta lista

    # Este bucle es AHORA NECESARIO para que se ejecute el .save() de cada objeto Servidos
    for linea in lineas_a_servir:
        # Al crear y guardar, el método .save() del modelo Servidos se ejecuta automáticamente
        # y se encarga de la comunicación "md". No necesitamos hacer nada más para eso.
        Servidos.objects.create(linea=linea)
        
        # Ahora, gestionamos el único caso que el modelo no cubre: la comunicación "rm"
        obj = linea.serialize()
        if not obj:
            result_rm.append({"ID": linea.pk})

    # --- 4. Comunicación Final (Solo lo que no hace el modelo) ---
    # Comunicamos los cambios 'rm' al final, en un solo lote.
    if result_rm:
        comunicar_cambios_devices("rm", "lineaspedido", result_rm)
    
    return JsonResponse({"status": "ok", "message": "Operación completada."})

@csrf_exempt
def get_pedidos_by_receptor(request):
    rec = json.loads(request.POST["receptores"])
    result = []
    for r in rec:
        partial = Lineaspedido.objects.filter(Q(tecla__familia__receptor_id=int(r)) & (Q(estado='P') | 
                                              Q(estado="R") | Q(estado="M"))).order_by("-pedido_id")
        lineas = partial.values("pedido").distinct()
        for l in lineas:
            p = Pedidos.objects.get(pk=l["pedido"])
            infmesa = p.infmesa
            camarero = Camareros.objects.get(pk=p.camarero_id)
            mesa_abierta = infmesa.mesasabiertas_set.first()
            if mesa_abierta:
                mesa = mesa_abierta.mesa
                result.append({
                "hora": infmesa.hora,
                "camarero": camarero.nombre+" "+camarero.apellidos,
                "mesa": mesa.nombre,
                "id": p.pk,
                "idReceptor": r
                })


    return JsonResponse(result)


@csrf_exempt
def recuperar_pedido(request):
    p = json.loads(request.POST["pedido"])
    partial = Lineaspedido.objects.filter(Q(tecla__familia__receptor_id=p["idReceptor"])  &
                                          Q(pedido_id=p["id"]) &
                                          (Q(estado='P') | Q(estado="R") | Q(estado="M")))
    lineas = partial.values("idart",
                            "descripcion",
                            "estado",
                            "pedido_id").annotate(can=Count('idart'))
    receptor = Receptores.objects.get(pk=p["idReceptor"])
    
    pedido = Pedidos.objects.get(pk=p["id"])
    mesa = pedido.infmesa.mesasabiertas_set.first().mesa

    camarero = Camareros.objects.get(pk=pedido.camarero_id)
    result = {
        "op": "pedido",
        "hora": pedido.hora,
        "receptor": receptor.nomimp,
        "nom_receptor": receptor.nombre,
        "receptor_activo": receptor.activo,
        "camarero": camarero.nombre + " " + camarero.apellidos,
        "mesa": mesa.nombre,
        "lineas": []
    }

    for l in lineas:
        result["lineas"].append(l)

    return JsonResponse(result)

