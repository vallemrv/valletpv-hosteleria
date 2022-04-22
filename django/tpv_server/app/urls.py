from django.urls import path
from . import views

app_name = "app"
urlpatterns = [
    path('', views.inicio, name="inicio"),
    path('getListado', views.getlistado, name="getlistado"),
    path('getListadoCompuesto', views.get_listado_compuesto, name="getListadoCompuesto"),
    path("getTeclados", views.get_teclados, name="getTeclados")
]