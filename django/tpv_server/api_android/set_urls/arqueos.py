from django.urls import path
from api_android.views.api_arqueos import arquear, get_cambio, update_last_cambio_stacke

urlpatterns  = [
  path("arquear", arquear, name="arqueos_arquear"),
  path("getcambio", get_cambio, name="arqueos_get_cambio"),
  path("setcambio", update_last_cambio_stacke, name="update_last_cambio_stake"),
]

