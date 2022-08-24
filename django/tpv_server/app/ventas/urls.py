from django.urls import path
from . import views

urlpatterns = [
   path("datasets", views.datasets, name="ventas_datasets"),
   path("borrar_mesa", views.cuenta_rm, name="cuenta_rm"),
   path("get_infomesa", views.get_infomesa, name="get_infomesa"),
   path("send_cobrar_mesa", views.send_cobrar_mesa, name="send_cobrar_mesa"),
   path("get_nulos", views.get_nulos, name="get_nulos"),
   path("get_list_mesas", views.get_list_mesas, name="get_list_mesas"),
   path("get_list_arqueos", views.get_list_arqueos, name="get_list_arqueos"),
]