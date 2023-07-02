from django.urls import path
from ..facturas import ventas

urlpatterns = [
   path("<int:id>/<str:uid>", ventas.index, name="index"),
   path("crear_factura", ventas.crear_factura, name="crear_factura")
]