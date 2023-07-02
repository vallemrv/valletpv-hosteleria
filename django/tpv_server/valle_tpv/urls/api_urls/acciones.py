from valle_tpv.api import api_acciones 
from django.urls import path

urlpatterns = [
    path('add/', api_acciones.add_reg),
    path('exec_inst/', api_acciones.exec_inst),
    path('delete/', api_acciones.delete_reg),
]