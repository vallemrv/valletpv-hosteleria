from django.urls import path
from api_android.views import api_pedidos as api_views

urlpatterns  = [
    path("getpendientes", api_views.get_pendientes, name="pedidos_get_pendientes"),
    path("servido", api_views.servido, name="pedidos_servido"),
    path("comparar_lineaspedido", api_views.comparar_lineaspedido, name="comparar_lineaspedido")
]
