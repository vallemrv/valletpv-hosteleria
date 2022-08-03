from django.urls import path
from . import views

urlpatterns = [
   path("<int:id>/<str:uid>", views.index, name="index"),
   path("crear_factura", views.crear_factura, name="crear_factura")
]