from django.urls import path
from . import views

urlpatterns = [
   path("datasets", views.datasets, name="ventas_datasets"),
   path("mesas_abiertas", views.mesas_abiertas, name="ventas_mesas_abiertas")
]