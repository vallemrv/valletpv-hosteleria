# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T19:42:58+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T15:51:11+02:00
# @License: Apache License v2.0

from django.urls import path, include
from api import views
from api.views import api_pedidos
from api.views.api_dispositivos import create_uid, set_alias, activate_device, deactivate_device, dispositivo_action

urlpatterns = [
    path("health", views.health_check, name="health_check"),
    path("chatbot/", include('chatbot.urls')),
    path("articulos/", include('api.set_urls.articulos'), name="api_articulos"),
    path("camareros/", include('api.set_urls.camareros'), name="api_camareros"),
    path('sync/', include("api.set_urls.sync"), name="api_sync"),
    path('pedidos/', include("api.set_urls.pedidos"), name="api_pedidos"),
    path('cuenta/', include("api.set_urls.cuenta"), name="api_cuenta"),
    path('comandas/', include("api.set_urls.comandas"), name="api_comandas"),
    path('arqueo/', include("api.set_urls.arqueos"), name="api_arqueos"),
    path('impresion/', include("api.set_urls.impresion"), name="api_impresion"),
    path('sugerencias/', include("api.set_urls.sugerencias"), name="api_sugerencias"),
    path('receptores/', include("api.set_urls.receptores"), name="api_receptores"),
    path("autorizaciones/", include("api.set_urls.autorizaciones"), name="api_autorizaciones"),
    path("sincronizar_pedidos", api_pedidos.get_pedidos_by_receptor, name="sincronizar_pedidos"),
    path("dispositivo/create_uid", create_uid, name="create_uid"),
    path("dispositivo/set_alias", set_alias, name="set_alias"),
    path("dispositivo/action", dispositivo_action, name="dispositivo_action"),
    path("dispositivo/activate", activate_device, name="activate_device"),
    path("dispositivo/deactivate", deactivate_device, name="deactivate_device")
]
