from django.urls import path
from valle_tpv import api as api_views

urlpatterns  = [
  path("crear_password/", api_views.crear_password, name="crear_password" ),
  path("permisos/", api_views.permissions_list, name="permisos" ),
]