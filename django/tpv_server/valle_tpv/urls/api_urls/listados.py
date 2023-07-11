from valle_tpv.api import api_listados 
from django.urls import path

urlpatterns = [
    path('multiple/', api_listados.listado_compuesto),
    path('simple/', api_listados.listado),
    path('permisos_camareros/', api_listados.permissions_list),
]