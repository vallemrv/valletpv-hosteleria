from django.urls import path
from . import views

urlpatterns = [
   path("get_estado_ventas_by_cam", views.get_estado_ventas_by_cam, name="get_estado_ventas_by_cam"),
   path("get_estado_ventas", views.get_estado_ventas, name="get_estado_ventas"),
   path("ventas_por_intervalos", views.ventas_por_intervalos, name="ventas_por_intervalos"),
   path('articulos_vendidos/', views.articulos_vendidos, name='articulos_vendidos'),
]