# @Author: Manuel Rodriguez <valle>
# @Date:   27-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 28-Jun-2018
# @License: Apache license vesion 2.0

try:
    from django.conf.urls import url
except :
    from django.urls import re_path as url
    
from . import views

app_name = "ventas"
urlpatterns = [
    url(r'^$', views.index, name="index"),
    url(r'^imprimir_pedido/(?P<id>\d*)/$', views.imprimir_pedido, name="imprimir_pedido"),
    url(r'^imprimir_desglose/(?P<id>\d*)/$', views.imprimir_desglose, name="imprimir_desglose"),
    url(r'^imprimir_ticket/(?P<id>\d*)/$', views.imprimir_ticket, name="imprimir_ticket"),
    url(r'^preimprimir_ticket/(?P<id>\d*)/$', views.preimprimir_ticket, name="preimprimir_ticket"),
    url(r'^reenviarlinea/(?P<id>\d*)/(?P<idl>\d*)/(?P<nombre>.*)/$', views.reenviarlinea, name="reenviarlinea"),
    url(r'^abrircajon/$', views.abrircajon, name="abrircajon"),
]
