# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-11T14:30:01+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-11T02:33:10+02:00
# @License: Apache License v2.0

from tokenapi.http import JsonResponse
from api_android.tools.ws_tools import send_update_ws
from django.contrib.auth.decorators import login_required
from django.shortcuts import render

@login_required(login_url='login_tk')
def menu_sync(request):
    return render(request, "sync_menu.html")

@login_required(login_url='login_tk')
def sync_camareros(request):
    send_update_ws(request,{
       "OP": "UPDATE",
       "Tabla": "camareros",
       "receptor": "comandas",
    })
    return JsonResponse({})

@login_required(login_url='login_tk')
def sync_secciones(request):
    send_update_ws(request,{
       "OP": "UPDATE",
       "Tabla": "secciones",
       "receptor": "comandas",
    })
    return JsonResponse({})

@login_required(login_url='login_tk')
def sync_subteclas(request):
    send_update_ws(request,{
       "OP": "UPDATE",
       "Tabla": "subteclas",
       "receptor": "comandas",
    })
    return JsonResponse({})

@login_required(login_url='login_tk')
def sync_teclascom(request):
    send_update_ws(request,{
       "OP": "UPDATE",
       "Tabla": "teclascom",
       "receptor": "comandas",
    })
    return JsonResponse({})

@login_required(login_url='login_tk')
def sync_mesas(request):
    send_update_ws(request,{
       "OP": "UPDATE",
       "Tabla": "zonas",
       "receptor": "comandas",
    })
    return JsonResponse({})
