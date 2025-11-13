# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-08-02T15:16:09+02:00
# @License: Apache License v2.0

import json
from tokenapi.http import  JsonResponse
from django.apps import apps
from gestion.tools.config_logs import logger_sync as logger
from api.decorators.uid_activo import verificar_uid_activo

@verificar_uid_activo
def sync_devices(request):
    
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tb_name = request.POST["tb"] 
    
    reg = json.loads(request.POST["reg"])
   

    model = apps.get_model(app_name, tb_name)
    result = []
    
    compare_func = getattr(model, 'compare_regs', None)
    if compare_func is not None:
        result = model.compare_regs(reg)  # Llamar a la función específica de comparación del modelo
    else:
        logger.debug(f"El modelo {tb_name} no tiene el método 'compare_regs' implementado.")

    if len(result) > 0:
        print("...................................")
        print("Tabla:", tb_name)
        print("===========================================")
        print("Registros cliente:", reg)
        print("-------------------------------------------")
        print("Registros servidor:", result)
    return JsonResponse(result)
