from django.urls import path
from valle_tpv.api import api_dispositivo as api

urlpatterns  = [
    path('new/', api.add, name="api_dispositivo_add"),
]