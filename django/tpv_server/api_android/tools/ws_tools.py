# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-16T23:51:40+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-01T01:10:16+02:00
# @License: Apache License v2.0

from django.core.serializers.json import DjangoJSONEncoder
from django.conf import settings
from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer

import json

def send_pedidos_ws(datos):
    for k, v in datos.items():
        try:
            channel_name = settings.EMPRESA + "_impresion_" + v["receptor"]
            layer = get_channel_layer()
            async_to_sync(layer.group_send)(channel_name, {
                'type': 'send_message',
                'content': json.dumps(v, cls=DjangoJSONEncoder)
            })
        except Exception as e:
            print(e)

def send_mensaje_ws(v):
    try:
        
        channel_name = settings.EMPRESA + "_impresion_" + v["receptor"]
        layer = get_channel_layer()
        async_to_sync(layer.group_send)(channel_name, {
            'type': 'send_message',
            'content': json.dumps(v, cls=DjangoJSONEncoder)
         })
    except Exception as e:
        print(e)


def send_update_ws(v):
    try:
        channel_name = settings.EMPRESA + "_comunicaciones_" + v["receptor"]
        layer = get_channel_layer()
        async_to_sync(layer.group_send)(channel_name, {
        'type': 'send_message',
        'content': json.dumps(v)
        })
    except Exception as e:
        print(e)


    
