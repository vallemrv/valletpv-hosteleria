# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-05-05T00:12:11+02:00
# @License: Apache license vesion 2.0


"""service_web URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.10/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url, include
from api_android import views as api_views
from django.contrib.staticfiles.storage import staticfiles_storage
from django.views.generic.base import RedirectView



urlpatterns = [
    url(r'^articulos/listado', api_views.art_listado, name="articulos_listado"),
    url(r'^secciones/listado', api_views.sec_listado, name="secciones_listado"),
    url(r'^cuenta/add', api_views.cuenta_add, name="cuenta_add"),
    url(r'^almacen/', include("almacen.urls"), name="almacen"),
    url(r'^contabilidad/', include("contabilidad.urls"), name="conta"),
    url(r'^gestion/', include("gestion.urls"), name="gestion"),
    url(r'^ventas/', include("ventas.urls"), name="ventas"),
    url(r'^sync/', include("api_android.urls"), name="api_android_sync"),
    url(r'^pedidos/', include("api_android.urls"), name="api_android_pedidos"),
    url(r'^camareros/', include("api_android.urls"), name="api_android_camareros"),
    url(r'^cuenta/', include("api_android.urls"), name="api_android_cuenta"),
    url(r'^mesas/', include("api_android.urls"), name="api_android_mesas"),
    url(r'^comandas/', include("api_android.urls"), name="api_android_comandas"),
    url(r'^arqueos/', include("api_android.urls"), name="api_android_arqueos"),
    url(r'^impresion/', include("api_android.urls"), name="api_android_impresion"),
    url(r'^secciones/', include("api_android.urls"), name="api_android_secciones"),
    url(r'^articulos/', include("api_android.urls"), name="api_android_articulos"),
    url(r'^sugerencias/', include("api_android.urls"), name="api_android_sugerencias"),
    url(r'^receptores/', include("api_android.urls"), name="api_android_receptores"),
    url(r'^favicon.ico$',
            RedirectView.as_view( # the redirecting function
                url=staticfiles_storage.url('favicon.ico'), # converts the static directory + our favicon into a URL
                # in my case, the result would be http://www.tumblingprogrammer.com/static/img/favicon.ico
            ),
            name="favicon" # name of our view
        ),
    url(r"simpleapi/", include('simpleapi.urls')),
    url(r'^token/', include('tokenapi.urls')),
    url(r'', include("inicio.urls")),
    

]
