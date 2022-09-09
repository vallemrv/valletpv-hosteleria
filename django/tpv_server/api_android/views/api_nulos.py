from django.db.models import Count, Sum
from tokenapi.http import JsonResponse, JsonError
from django.views.decorators.csrf import csrf_exempt
from gestion.models import (Historialnulos, Infmesa, Mesasabiertas, Camareros, Mesas)


import datetime
import json

@csrf_exempt
def get_nulos(request):
    respose=[]
    for c in Historialnulos.objects.all().values("lineapedido__infmesa").annotate(lineas=Count("lineapedido"), total=Sum("lineapedido__precio")).order_by("-lineapedido__infmesa__fecha", "-lineapedido__infmesa__hora")[:100]:
        mesa = Mesas.objects.get(pk=c["lineapedido__infmesa"].split("-")[0])
        inf = Infmesa.objects.get(pk=c["lineapedido__infmesa"])
        respose.append({
            "mesa": mesa.nombre,
            "fecha": datetime.datetime.strptime(inf.fecha,"%Y/%m/%d").strftime("%d/%m/%Y") +" - " + inf.hora,
            "camarero": inf.camarero.nombre + " " + inf.camarero.apellidos,
            "nulos": "{0:.2f}".format(c["total"]),
            "lineas": c["lineas"],
            "id": c["lineapedido__infmesa"] 
        })
    return JsonResponse(respose)


@csrf_exempt
def get_infmesa(request):
    if request.method == 'POST':
        id = request.POST["id"]
        inf = Infmesa.objects.get(pk=id)
        res = {"abierta":True if Mesasabiertas.objects.filter(infmesa_id=inf.pk).first() else False,
               "apertura": datetime.datetime.strptime(inf.fecha,"%Y/%m/%d").strftime("%d/%m/%Y") +" - " + inf.hora,
               "camarero": inf.camarero.nombre + " " + inf.camarero.apellidos,
               "nom_mesa": Mesas.objects.get(pk=inf.uid.split("-")[0]).nombre,
               "pedidos": []}
        pedidos = inf.pedidos_set.all()
        desglose = {"cobrados":0,"nulos":0,"pendientes":0}
        for p in pedidos:
            lineas = []
            for l in p.lineaspedido_set.all().values("precio","nombre","estado").annotate(can=Count('nombre')):
                if l["estado"] == "P":
                    desglose["pendientes"] +=  l["precio"] * l["can"]
                elif l["estado"] == "A":
                    desglose["nulos"] +=  l["precio"] * l["can"]
                else:
                    desglose["cobrados"] +=  l["precio"] * l["can"] 
                lineas.append({
                    "can": l["can"],
                    "nombre": l["nombre"],
                    "precio": l["precio"] * l["can"],
                    "estado": l["estado"]
                })
            cam = Camareros.objects.get(pk=p.camarero_id)
            res_pedidos = res["pedidos"]
            res_pedidos.append({
                "hora": p.hora,
                "camarero": cam.nombre + " " + cam.apellidos,
                "lineas": lineas,
            })
        res["desglose"] = desglose

        return JsonResponse(res)

    return JsonError("Solo llamadas POST...")