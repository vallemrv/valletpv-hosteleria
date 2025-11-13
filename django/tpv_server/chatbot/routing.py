from django.urls import path
from .consumers.chatbot_consumer import ChatConsumer
from .consumers.pedidos_consumer import PedidosConsumer

websocket_urlpatterns = [
    path('ws/chatbot/<str:room>/', ChatConsumer.as_asgi()),
    path('ws/pedidos/<str:room>/', PedidosConsumer.as_asgi()),
]
