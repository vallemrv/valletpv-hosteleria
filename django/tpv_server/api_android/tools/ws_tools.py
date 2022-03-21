# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-16T23:51:40+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-01T01:10:16+02:00
# @License: Apache License v2.0

from django.contrib.sites.shortcuts import get_current_site
from django.core.serializers.json import DjangoJSONEncoder
import websocket
import json

def send_pedidos_ws(request, datos):
    for k, v in datos.items():
        url = ''.join(['ws://', get_current_site(request).domain, '/ws/impresion/', v["receptor"], "/"])
        ws = websocket.create_connection(url)
        ws.send(json.dumps({"message": v}))
        ws.close()

def send_ticket_ws(request, v):
    try:
        url = ''.join(['ws://', get_current_site(request).domain, '/ws/impresion/', v["receptor"], "/"])
        ws = websocket.create_connection(url)
        ws.send(json.dumps({"message": v}, cls=DjangoJSONEncoder))
        ws.close()
    except Exception as e:
        print(e)


def send_update_ws(request, v):
    try:
        url = ''.join(['ws://', get_current_site(request).domain, '/ws/comunicacion/', v["receptor"]])
        ws = websocket.create_connection(url)
        ws.send(json.dumps({"content": v}))
        ws.close()
    except Exception as e:
        print(e)
