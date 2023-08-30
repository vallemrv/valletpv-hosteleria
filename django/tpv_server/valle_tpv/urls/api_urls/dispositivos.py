from django.urls import path
from valle_tpv.api import api_dispositivos as api

urlpatterns  = [
    path('new', api.add, name="api_dispositivo_add"),
]