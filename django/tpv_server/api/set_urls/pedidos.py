from django.urls import path
from api.views import api_pedidos as api_views

urlpatterns  = [
    path("servido", api_views.servido, name="pedidos_servido"),
    path("get_pedidos_by_receptor", api_views.get_pedidos_by_receptor, name="get_pedidos_by_receptor"),
]
