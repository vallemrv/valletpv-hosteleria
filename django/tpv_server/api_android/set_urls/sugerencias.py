from django.urls import path
from api_android.views import api_sugerencias as api_views

urlpatterns  = [
   path("add", api_views.sugerencia_add, name="sugericia_add"),
]
