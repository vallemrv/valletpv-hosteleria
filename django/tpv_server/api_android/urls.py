# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T19:42:58+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T15:51:11+02:00
# @License: Apache License v2.0

from django.urls import path, include
from api_android import views
from api_android.views import api_pedidos
from api_android.views.api_dispositivos import create_uid

urlpatterns = [
    path("openai/", include("api_android.set_urls.openai"), name="api_android_openai"),
    path("camareros/", include('api_android.set_urls.camareros'), name="api_android_camareros"),
    path('sync/', include("api_android.set_urls.sync"), name="api_android_sync"),
    path('pedidos/', include("api_android.set_urls.pedidos"), name="api_android_pedidos"),
    path('cuenta/', include("api_android.set_urls.cuenta"), name="api_android_cuenta"),
    path('comandas/', include("api_android.set_urls.comandas"), name="api_android_comandas"),
    path('arqueos/', include("api_android.set_urls.arqueos"), name="api_android_arqueos"),
    path('impresion/', include("api_android.set_urls.impresion"), name="api_android_impresion"),
    path('sugerencias/', include("api_android.set_urls.sugerencias"), name="api_android_sugerencias"),
    path('receptores/', include("api_android.set_urls.receptores"), name="api_android_receptores"),
    path("autorizaciones/", include("api_android.set_urls.autorizaciones"), name="api_android_autorizaciones"),
    path("get_datos_empresa", views.get_datos_empresa, name="get_datos_empresa"),
    path("get_pedidos_by_receptor", api_pedidos.get_pedidos_by_receptor, name="get_pedidos_by_receptor"),
    path("recuperar_pedido", api_pedidos.recuperar_pedido, name="recuperar_pedido"),
    path("get_uuid_factura/<int:num>", views.get_uuid_factura, name="get_uuid_factura"),
    path("dispositivos/get_device_uid", create_uid, name="create_uid")
] 
