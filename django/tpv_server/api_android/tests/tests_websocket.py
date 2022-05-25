from api_android.tools import send_mensaje_devices
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt


@csrf_exempt
def send_test_mensaje(request, msg):
    #enviar notficacion de update
    update = {
       "OP": "MENSAJE",
       "msg": msg,
       "receptor": "comandas",
    }
    send_mensaje_devices(update)
    return HttpResponse("success")


@csrf_exempt
def test_websocket(request):
    use_channels_layers()
    return HttpResponse("hola colega")

def create_ws():
    
    from websocket import create_connection

    
    ws = create_connection("ws://0.0.0.0:8000/ws/comunicacion/caja")

    print("Sending 'Hello, World'...")
    ws.send('{"content":"holla"}')
    print("Sent")

    ws.close()


def use_channels_layers():
    from asgiref.sync import async_to_sync
    from channels.layers import get_channel_layer

    layer = get_channel_layer()
    async_to_sync(layer.group_send)('testTPV_comuicaciones_caja', {
    'type': 'send_message',
    'content': 'hola manolo que te esta pasando gilipollas'
    })
    return HttpResponse('<p>Done</p>')