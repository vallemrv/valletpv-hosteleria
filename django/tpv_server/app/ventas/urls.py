from django.urls import path
from . import views

urlpatterns = [
   path("datasets", views.datasets, name="ventas_datasets"),
   path("borrar_mesa", views.cuenta_rm, name="cuenta_rm"),
   path("get_infomesa", views.get_infomesa, name="get_infomesa")
]