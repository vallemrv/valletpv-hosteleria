from django.urls import path
from api.views import api_planing

urlpatterns = [
    path('descargar_planing', api_planing.descargar_planing, name='descargar_planing'),
    path('guardar_planing', api_planing.guardar_planing, name='guardar_planing'),
    path('get_iconos', api_planing.get_iconos, name='get_iconos'),
]
