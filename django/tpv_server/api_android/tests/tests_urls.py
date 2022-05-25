from unicodedata import name
from django.urls import path
from api_android import tests

urlpatterns  = [
   path("send_last_cierre", tests.send_last_cierre, name="send_last_cierre"),
   path("reparar_subteclas", tests.reparar_subteclas, name="reparar_subteclas"),
   path("test_ws", tests.test_websocket, name="test_ws"),
   path("send_test_mensaje/<str:msg>", tests.send_test_mensaje, name="send_test_mensaje"),
   path("lista_mens_autorizaciones/<str:id>", tests.lista_mens_autorizaciones, name="lista_mens_autorizaciones"),
   path("actualiza_sync/<str:tb_name>", tests.actualiza_sync , name="actualiza_sync"),
   path("test_composiciones", tests.composicion, name="composicion")
]