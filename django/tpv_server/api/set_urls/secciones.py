from django.urls import path
from api.views import api_secciones as api_views

urlpatterns  = [
  path('listado', api_views.sec_listado, name="secciones_listado"),
]