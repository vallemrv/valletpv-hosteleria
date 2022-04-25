from django.urls import path
from . import views

app_name = "app"
urlpatterns = [
    path('', views.inicio, name="inicio"),
    path('getListado', views.getlistado, name="getlistado"),
    path('getListadoCompuesto', views.get_listado_compuesto, name="getListadoCompuesto"),
    path("mod_regs", views.mod_regs, name="mod_regs"),
    path("add_reg", views.add_reg, name="add_reg")
]