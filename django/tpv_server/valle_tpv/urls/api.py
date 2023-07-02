# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T19:42:58+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T15:51:11+02:00
# @License: Apache License v2.0

from django.urls import path, include

urlpatterns = [
    path("camareros/", include('valle_tpv.urls.api_urls.camareros'), name="api_android_camareros"),
    path('sync/', include("valle_tpv.urls.api_urls.sync"), name="api_android_sync"),
    path('pedidos/', include("valle_tpv.urls.api_urls.pedidos"), name="api_android_pedidos"),
    path('cuenta/', include("valle_tpv.urls.api_urls.cuenta"), name="api_android_cuenta"),
    path('comandas/', include("valle_tpv.urls.api_urls.comandas"), name="api_android_comandas"),
    path('arqueos/', include("valle_tpv.urls.api_urls.arqueos"), name="api_android_arqueos"),
    path('impresion/', include("valle_tpv.urls.api_urls.impresion"), name="api_android_impresion"),
    path('sugerencias/', include("valle_tpv.urls.api_urls.sugerencias"), name="api_android_sugerencias"),
    path('receptores/', include("valle_tpv.urls.api_urls.receptores"), name="api_android_receptores"),
    path("autorizaciones/", include("valle_tpv.urls.api_urls.autorizaciones"), name="api_android_autorizaciones"),
    path("dash_board/", include("valle_tpv.urls.api_urls.dash_board"), name="api_android_autorizaciones"),
    path("user/", include("valle_tpv.urls.api_urls.user_profile"), name="api_android_autorizaciones"),
    path("listados/", include("valle_tpv.urls.api_urls.listados"), name="api_android_listados"),
    path("acciones/", include("valle_tpv.urls.api_urls.acciones"), name="api_android_acciones"),
]


'''

    path("get_datos_empresa", views.get_datos_empresa, name="get_datos_empresa"),
    path("get_pedidos_by_receptor", views.get_pedidos_by_receptor, name="get_pedidos_by_receptor"),
    path("recuperar_pedido", views.recuperar_pedido, name="recuperar_pedido"),
    path("get_uuid_factura/<int:num>", views.get_uuid_factura, name="get_uuid_factura"),
] '''
