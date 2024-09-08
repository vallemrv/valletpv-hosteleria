from django.urls import path
from api_android.views.api_cuenta import (cuenta_add, cuenta_ls_linea, cuenta_ls_ticket, get_cuenta,
                                          juntarmesas, cambiarmesas, mvlinea, cuenta_cobrar, cuenta_rm, cuenta_rm_linea,
                                          )


urlpatterns  = [
    path("lsticket", cuenta_ls_ticket, name="lsticket"),
    path("lslineas", cuenta_ls_linea, name="lslineas"),
    path("get_cuenta", get_cuenta, name="get_cuenta"),
    path("juntarmesas", juntarmesas, name="cuenta_juntarmesas"),
    path("cambiarmesas", cambiarmesas, name="cuenta_cambiarmesas"),
    path("mvlinea", mvlinea, name="cuenta_mvlinea"),
    path("cobrar", cuenta_cobrar, name="cuenta_cobrar"),
    path("rm", cuenta_rm, name="cuenta_rm"),
    path("rmlinea", cuenta_rm_linea, name="cuenta_rm_linea"),
    path('add', cuenta_add, name="cuenta_add"), 
]