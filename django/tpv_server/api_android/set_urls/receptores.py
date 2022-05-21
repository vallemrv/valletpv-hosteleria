from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path("set_settings", api_views.set_settings, name="receptores_set_settings"),
]