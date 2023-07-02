# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-08-02T15:16:09+02:00
# @License: Apache License v2.0

import json
from tokenapi.http import  JsonResponse       
from django.apps import apps
from django.forms import model_to_dict
from tokenapi.decorators import token_required
from valle_tpv.api.api_pedidos import comparar_lineaspedido
from django.http import JsonResponse


@token_required
def sync_data(request):
    client_data = json.loads(request.POST["params"]) # Asumiendo que el JSON se envía en el cuerpo de la solicitud
    model_name = client_data.get('tb_name')
    client_records = client_data.get('regs')
    app_name = request.POST["app"] if "app" in request.POST else "valle_tpv"
   

    Model = apps.get_model('myapp', model_name) # Reemplaza 'myapp' con el nombre de tu app

    server_records = Model.objects.all().values()

    response_data = {'update': [], 'delete': [], 'create': []}

    # Diccionario de registros del cliente para facilitar la búsqueda por ID
    client_dict = {rec['id']: rec for rec in client_records}

    # Comparar con los registros del servidor
    for rec in server_records:
        client_rec = client_dict.get(rec['id'])
        if client_rec:
            # Si alguna key es diferente, devolver el registro con los valores buenos del servidor
            if rec != client_rec:
                response_data['update'].append(rec)
            # Eliminar el registro del diccionario del cliente una vez procesado
            del client_dict[rec['id']]
        else:
            # Si el registro del servidor no existe en el cliente, devolverlo para crearlo en el cliente
            response_data['create'].append(rec)

    # Los registros restantes en el diccionario del cliente no existen en el servidor, devolver sus IDs para borrarlos en el cliente
    response_data['delete'] = list(client_dict.keys())

    return JsonResponse(response_data)

@token_required
def update_from_devices(request):
    tb = request.POST["tb"]
    rows = json.loads(request.POST["rows"])

    
    model = apps.get_model("valle_tpv", tb)
    if hasattr(model, "update_from_device"):
        for row in rows:
            model.update_from_device(row)
                
    
    return JsonResponse({})


@token_required
def sync_devices(request):
    app_name = request.POST["app"] if "app" in request.POST else "valle_tpv"
    tb_name = request.POST["tb"] 
    if (tb_name == "lineaspedido"): return comparar_lineaspedido(request)
    reg = json.loads(request.POST["reg"])
    model = apps.get_model(app_name, tb_name)
    result = []
    pks = []
    
    for r in reg:
        try:
            key, v = ("ID", r["ID"]) if "ID" in r.keys() else ("id", r['id'])
            if (tb_name == "mesasabiertas"):
                obj = model.objects.filter(mesa__id=v).first()
                if not obj:
                    result.append({"tb":tb_name, "op": "md", "obj":{ 'ID':v, 'abierta': 0, "num":0 }})
                    continue
           
            else:
                obj = model.objects.filter(pk=v).first()
                if not obj:
                    result.append({"tb":tb_name, "op": "rm", "obj":{key:v}})
                    continue
           
            pks.append(v)
           
            if hasattr(obj, "serialize"):
                obj = obj.serialize()
            else:
                obj = model_to_dict(obj)
            for k, v in r.items():
                obj_v =  obj[k] if k in obj else obj[k.lower()]
                if not equals(k, str(obj_v), str(v)):
                    result.append({"tb":tb_name, "op": "md", "obj":obj})
                    break

        except Exception as e:
            print(e)
            print(tb_name, r)  
        
   
   
    op = "insert"
    if (tb_name == "mesasabiertas"):
        objs = model.objects.exclude(mesa__id__in=pks)
        op = "md"
    else:    
        objs = model.objects.exclude(pk__in=pks)
        

    for obj in objs:
        if hasattr(obj, "serialize"):
            obj = obj.serialize()
        else:
            obj = model_to_dict(obj)
        result.append({"tb":tb_name, "op": op, "obj":obj})    

    

    return JsonResponse(result)



def equals(k, obj1, obj2):
    if k.lower() in ["p1", "p2", "precio", "incremento", "entrega"]:
        return float(obj1) == float(obj2)

    if obj1 in ["None", "null"]:
        if obj2 in ["None", "null"]:
            return True
        else:
            return False

    
    return obj1.lower() == obj2.lower()