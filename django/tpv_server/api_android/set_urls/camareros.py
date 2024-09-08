from django.urls import path
from api_android.views.api_camareros import crear_password, camarero_add, listado

urlpatterns  = [
  path("crear_password", crear_password, name="crear_password" ),
  path("camarero_add", camarero_add, name="camarero_add"),
  path("listado", listado, name="listado")
]