from django.urls import path
from . import views

urlpatterns = [
   path("datasets", views.datasets, name="ventas_datasets")
]