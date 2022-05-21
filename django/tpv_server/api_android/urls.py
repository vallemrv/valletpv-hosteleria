# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T19:42:58+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-10T15:51:11+02:00
# @License: Apache License v2.0

from django.urls import path, include

urlpatterns = [
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
    path("tests/", include("api_android.tests.tests_urls"), name="tests")
    
] 
