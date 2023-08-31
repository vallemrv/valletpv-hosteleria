# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T19:42:58+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T15:51:11+02:00
# @License: Apache License v2.0

from django.urls import path, include

urlpatterns = [
    path('dispositivos/', include("valle_tpv.urls.api_urls.dispositivos"), name="api_android_dispositivos"),
    path('camareros/', include("valle_tpv.urls.api_urls.camareros"), name="api_android_camareros"),
    path('sync/', include("valle_tpv.urls.api_urls.sync"), name="api_android_sync"),
    path("dash_board/", include("valle_tpv.urls.api_urls.dash_board"), name="api_android_autorizaciones"),
    path("user/", include("valle_tpv.urls.api_urls.user_profile"), name="api_android_autorizaciones"),
    path("listados/", include("valle_tpv.urls.api_urls.listados"), name="api_android_listados"),
    path("acciones/", include("valle_tpv.urls.api_urls.acciones"), name="api_android_acciones"),
    path("pedidos/", include("valle_tpv.urls.api_urls.pedidos"), name="api_android_pedidos"),
    path("impresoras/", include("valle_tpv.urls.api_urls.impresion"), name="api_android_impresoras"),
]

