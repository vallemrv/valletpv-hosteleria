from django.urls import path
from . import views

app_name = "app"
urlpatterns = [
    path('', views.inicio, name="inicio"),
    path('getListado', views.getlistado, name="getlistado"),
    path('getListadoCompuesto', views.get_listado_compuesto, name="getListadoCompuesto"),
    path("mod_regs", views.mod_regs, name="mod_regs"),
    path("add_reg", views.add_reg, name="add_reg"),
    path("mod_sec", views.mod_sec, name="mod_sec"),
    path("reset_db", views.reset_db, name="reset_db"),
    path("get_datos_empresa", views.get_datos_empresa, name="get_datos_empresa"),
    path("send_cierre", views.send_cierre_by_id, name="send_cierre_by_id" )
]