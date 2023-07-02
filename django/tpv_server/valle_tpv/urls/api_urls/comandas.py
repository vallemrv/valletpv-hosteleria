from django.urls import path
from valle_tpv import api as api_views

urlpatterns  =[
    path("pedir", api_views.pedir, name="comandas_pedir"),
    path("marcar_rojo", api_views.marcar_rojo, name="comandas_marcar_rojo"),
]
