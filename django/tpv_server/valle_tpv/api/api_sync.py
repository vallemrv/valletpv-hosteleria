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
from valle_tpv.decorators import check_dispositivo



@check_dispositivo
def sync_devices(request):
    # Asumiendo que el JSON se envía en el cuerpo de la solicitud
    model_name = request.POST.get('tb_name')
    client_records = json.loads(request.POST.get('regs'))
    app_name = request.POST["app"] if "app" in request.POST else "valle_tpv"
    
    Model = apps.get_model(app_name, model_name) # Reemplaza 'myapp' con el nombre de tu app

    if model_name == "lineaspedido":
        server_records = Model.objects.filter(estado__in=["P", "M", "R"])
    else:
        server_records = Model.objects.all()

    response_data = {'update': [], 'delete': [], 'create': []}

    # Diccionario de registros del cliente para facilitar la búsqueda por ID
    client_dict = {rec['id']: rec for rec in client_records}
 
    if model_name == "lineaspedido":
        print(len(client_records), len(server_records)  )


    # Comparar con los registros del servidor
    for rec in server_records:
        
        row = rec.serialize() if hasattr(rec, "serialize") else model_to_dict(rec)
        client_rec = client_dict.get(rec.pk)
       
        if client_rec:
            
            for k, v in client_rec.items():
                if v != row[k]:
                    print("update", v, k, row[k])
                    response_data['update'].append(row)
                    break
        
            
            del client_dict[rec.pk]
        else:
            response_data['create'].append(row)  

    # Los registros restantes en el diccionario del cliente no existen en el servidor, devolver sus IDs para borrarlos en el cliente
    response_data['delete'] = list(client_dict.keys())
    
    return JsonResponse({'sync': json.dumps(response_data)})

@check_dispositivo
def update_from_devices(request):
    tb = request.POST["tb"]
    rows = json.loads(request.POST["rows"])

    
    model = apps.get_model("valle_tpv", tb)
    if hasattr(model, "update_from_device"):
        for row in rows:
            model.update_from_device(row)
                
    
    return JsonResponse({})



