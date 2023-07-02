from django.urls import path
from valle_tpv import api as api_views

urlpatterns  = [
   path("add", api_views.sugerencia_add, name="sugericia_add"),
]
