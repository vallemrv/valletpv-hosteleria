from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path("get_lista", api_views.get_lista, name="receptores_get_lista"),
  path("set_settings", api_views.set_settings, name="receptores_set_settings"),
]