from django.urls import path
from valle_tpv import api as api_views

urlpatterns  = [
   path("pedir_autorizacion", api_views.pedir_autorizacion, name="pedir_autorizacion"),
   path("get_lista_autorizaciones", api_views.get_lista_autorizaciones, name="get_lista_autorizaciones"),
   path("gestionar_peticion", api_views.gestionar_peticion, name="gestionar_peticion"),
   path("send_informacion", api_views.send_informacion, name="send_informacion")
]