from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path("crear_password", api_views.crear_password, name="crear_password" ),
  path("camarero_add", api_views.camarero_add, name="camarero_add"),
  path("listado", api_views.listado, name="listado")
]