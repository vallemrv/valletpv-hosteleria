import json
from django.shortcuts import render
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.apps import apps
from django.forms.models import model_to_dict

def inicio(request):
    return render(request, "index.html")


@csrf_exempt
def getlistado(request):
    app_name = request.POST["app"] if "app" in request.POST else "gestion"
    tb_name = request.POST["tb"]
    filter = json.loads(request.POST["filter"]) if "filter" in request.POST else {}
    model = apps.get_model(app_name, tb_name)
    objs = model.objects.filter(**filter)
    regs = []
    for obj in objs:
        regs.append(model_to_dict(obj))

    return JsonResponse({'tb':tb_name, "regs": regs})

