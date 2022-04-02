from django.urls import path
from api_android import views as api_views

urlpatterns  = [
    path("ticket", api_views.ticket, name="cuenta_ticket"),
    path("get_cuenta", api_views.get_cuenta, name="get_cuenta"),
    path("juntarmesas", api_views.juntarmesas, name="cuenta_juntarmesas"),
    path("cambiarmesas", api_views.cambiarmesas, name="cuenta_cambiarmesas"),
    path("mvlinea", api_views.mvlinea, name="cuenta_mvlinea"),
    path("lsaparcadas", api_views.ls_aparcadas, name="cuenta_ls_aparcadas"),
    path("cobrar", api_views.cuenta_cobrar, name="cuenta_cobrar"),
    path("rm", api_views.cuenta_rm, name="cuenta_rm"),
    path("rmlinea", api_views.cuenta_rm_linea, name="cuenta_rm_linea"),
    path("lsticket", api_views.cuenta_ls_ticket, name="cuenta_ls_ticket"),
    path("lslineas", api_views.cuenta_ls_linea, name="cuenta_ls_linea"),
    path('add', api_views.cuenta_add, name="cuenta_add"),
    
]