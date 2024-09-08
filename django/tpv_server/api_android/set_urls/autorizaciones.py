from django.urls import path
from api_android.views.api_autorizaciones import (pedir_autorizacion, get_lista_autorizaciones, 
                                                  gestionar_peticion, send_informacion)

urlpatterns  = [
   path("pedir_autorizacion", pedir_autorizacion, name="pedir_autorizacion"),
   path("get_lista_autorizaciones", get_lista_autorizaciones, name="get_lista_autorizaciones"),
   path("gestionar_peticion", gestionar_peticion, name="gestionar_peticion"),
   path("send_informacion", send_informacion, name="send_informacion")
]