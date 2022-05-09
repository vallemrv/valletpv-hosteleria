from django.urls import path
from api_android import views as api_views

urlpatterns  =[
    path("pedir", api_views.pedir, name="comandas_pedir"),
    path("marcar_rojo", api_views.marcar_rojo, name="comandas_marcar_rojo"),
    path("get_ultimas", api_views.get_ultimas, name="get_ultimas"),
]
