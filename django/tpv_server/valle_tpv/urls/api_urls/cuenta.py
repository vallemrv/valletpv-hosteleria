from django.urls import path
from valle_tpv.api import api_cuenta as api_views

urlpatterns  = [
    path("get_cuenta", api_views.get_cuenta, name="get_cuenta"),
    path("juntarmesas", api_views.juntarmesas, name="cuenta_juntarmesas"),
    path("cambiarmesas", api_views.cambiarmesas, name="cuenta_cambiarmesas"),
    path("mvlinea", api_views.mvlinea, name="cuenta_mvlinea"),
    path("cobrar", api_views.cuenta_cobrar, name="cuenta_cobrar"),
    path("rm", api_views.cuenta_rm, name="cuenta_rm"),
    path("editar_cuenta", api_views.editar_cuenta, name="editar_cuenta"),
]