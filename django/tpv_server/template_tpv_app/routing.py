# @Author: Manuel Rodriguez <valle>
# @Date:   10-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 19-Jun-2018
# @License: Apache license vesion 2.0



from channels.auth import AuthMiddlewareStack
from channels.routing import ProtocolTypeRouter, URLRouter
from comunicacion.routing import websocket_urlpatterns as comunicacion_websocket_urlpatterns
from chatbot.routing import websocket_urlpatterns as chatbot_websocket_urlpatterns
from django.core.asgi import get_asgi_application

from gestion.middleware.database_connection import DatabaseConnectionMiddleware 

application = ProtocolTypeRouter({
    # (http->django views is added by default)
    "http": get_asgi_application(),
    'websocket': AuthMiddlewareStack(
         DatabaseConnectionMiddleware(
            URLRouter(
                comunicacion_websocket_urlpatterns + chatbot_websocket_urlpatterns
            )
        )
    ),
})
