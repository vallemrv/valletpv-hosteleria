from django.urls import path
from valle_tpv.api import api_pedidos as api

urlpatterns  = [
    path('add', api.pedir, name="api_pedidos_pedir"),
]