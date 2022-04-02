from django.urls import path
from api_android import views as api_views

urlpatterns  =[
    path("lssubteclas", api_views.lssubteclas, name="comandas_lssubteclas"),
    path("lsAll", api_views.lsall, name="comandas_lsall"),
    path("pedir", api_views.pedir, name="comandas_pedir"),
    path("lspedidos", api_views.lspedidos, name="comandas_lspedidos"),
    path("marcar_rojo", api_views.marcar_rojo, name="comandas_marcar_rojo"),
    path("lssecciones", api_views.lssecciones, name="comandas_lssecciones"),
    path("get_ultimas", api_views.get_ultimas, name="get_ultimas"),

]
