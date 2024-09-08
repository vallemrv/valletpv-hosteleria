from django.urls import path
from api_android.views.api_comandas import pedir, marcar_rojo

urlpatterns  =[
    path("pedir", pedir, name="comandas_pedir"),
    path("marcar_rojo", marcar_rojo, name="comandas_marcar_rojo"),
]
