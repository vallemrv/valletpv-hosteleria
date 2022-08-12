# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:52:10+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-26T14:53:11+02:00
# @License: Apache License v2.0

import re
from unittest import result
from django.db.models import Count, Q
from comunicacion.tools import comunicar_cambios_devices
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from gestion.models import  Camareros, Lineaspedido, Mesasabiertas, Pedidos, Receptores, Servidos
import json


def find(id, lineas):
    for l in lineas:
        if int(l["ID"]) == int(id):
            lineas.remove(l)
            return l
    return None

@csrf_exempt
def  get_pendientes(request):
    idz = request.POST["idz"]
    mesas = Mesasabiertas.objects.filter(mesa__mesaszona__zona__pk=idz).distinct()
    lineas = json.loads(request.POST["lineas"])
    result = []
    lineas_server = []
    
    for m in mesas:
        lineas_server = [*lineas_server, *m.get_lineaspedido()]

    
    for l in lineas_server:
        linea = find(l["ID"], lineas)
        if linea:
            if not Lineaspedido.is_equals(l, linea):
                result.append({'op':'md', 'tb':"lineaspedido", 'reg': l})
        else:
            result.append({'op':'insert', 'tb':"lineaspedido", 'reg': l})
            
    for l in lineas:
        result.append({'op':'rm', 'tb':"lineaspedido", 'reg': {"ID":l["ID"]}})   
    
    return JsonResponse(result)    

@csrf_exempt
def  comparar_lineaspedido(request):
    mesas = Mesasabiertas.objects.all()
    lineas = json.loads(request.POST["lineas"])
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
            
    for l in lineas:
        result.append({'op':'rm', 'tb':"lineaspedido", 'obj': {"ID":l["ID"]}})   

    
    return JsonResponse(result)

@csrf_exempt
def servido(request):
    arts = json.loads(request.POST["art"])
    ver = 0 if "ver" not in request.POST else request.POST["ver"]
    if not hasattr(arts, "append"):
        arts = [arts]
    
    result_md = []
    result_rm = []
    for art in arts:
        lineas = Lineaspedido.objects.filter(idart=art["IDArt"],
                                            descripcion=art["Descripcion"],
                                            precio=art["Precio"],
                                            pedido_id=art["IDPedido"])
        
        
        for l in lineas:
            serv = Servidos()
            serv.linea_id = l.pk
            serv.save() 
            obj = l.serialize()
            if obj:
                if ver == "1":
                    result_md.append(obj) 
                else:
                    comunicar_cambios_devices("md", "lineaspedido", l.serialize())
            else:
                if ver == "1":
                    result_rm.append({"ID":l.pk})  
                else:
                    comunicar_cambios_devices("rm", "lineaspedido", {"ID":l.pk})

   
    if len(result_rm) > 0:
        comunicar_cambios_devices("rm", "lineaspedido", result_rm)
    if len(result_md) > 0:
        comunicar_cambios_devices("md", "lineaspedido", result_md)       
    
    return JsonResponse({})

@csrf_exempt
def get_pedidos_by_receptor(request):
    rec = json.loads(request.POST["receptores"])
    result = []
    for r in rec:
        partial = Lineaspedido.objects.filter(Q(tecla__familia__receptor_id=int(r)) 
                                          & (Q(estado='P') | 
                                             Q(estado="R") | 
                                             Q(estado="M"))).order_by("-pedido_id")
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

