from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path("arquear", api_views.arquear, name="arqueos_arquear"),
  path("getcambio", api_views.get_cambio, name="arqueos_get_cambio"),
]

