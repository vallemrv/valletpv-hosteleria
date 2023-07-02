from django.urls import path, include
    
app_name = "valle_tpv"

urlpatterns = [
    path("api/", include("valle_tpv.urls.api"), name="api")
]

'''
urlpatterns = [
    path('', views.inicio, name="inicio"),
    path('getListado', views.getlistado, name="getlistado"),
    path('getListadoCompuesto', views.get_listado_compuesto, name="getListadoCompuesto"),
    path("mod_regs", views.mod_regs, name="mod_regs"),
    path("add_reg", views.add_reg, name="add_reg"),
    path("mod_sec", views.mod_sec, name="mod_sec"),
    path("reset_db", views.reset_db, name="reset_db"),
    path("get_datos_empresa", views.get_datos_empresa, name="get_datos_empresa"),
    path("send_cierre", views.send_cierre_by_id, name="send_cierre_by_id" ),
    path("ventas/", include("valle_tpv.ventas.urls"), name="ventas"),
    path("facturas/", include("valle_tpv.facturas.urls"), name="facturas"),
    path("dashboard/", include("valle_tpv.dash_board.urls"), name="dashboard"),
    path("gestion_ia/", include("valle_tpv.ws.valle_ia.urls"), name="gestion_ia"),
]'''