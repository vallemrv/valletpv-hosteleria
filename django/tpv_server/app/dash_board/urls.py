from django.urls import path
from . import views

urlpatterns = [
   path("get_estado_ventas_by_cam", views.get_estado_ventas_by_cam, name="get_estado_ventas_by_cam"),
   path("get_estado_ventas", views.get_estado_ventas, name="get_estado_ventas"),
]