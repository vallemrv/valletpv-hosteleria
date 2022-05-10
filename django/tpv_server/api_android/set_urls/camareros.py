from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path("listado", api_views.listado_camareros, name="listado_camareros" ),
  path("crear_password", api_views.crear_password, name="crear_password" ),
  path("camarero_add", api_views.camarero_add, name="camarero_add")
]