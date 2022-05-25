from django.urls import path
from api_android import views as api_views

urlpatterns  = [
    path("getpendientes", api_views.get_pendientes, name="pedidos_get_pendientes"),
    path("servido", api_views.servido, name="pedidos_servido"),
]
