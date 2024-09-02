from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path('listado', api_views.sec_listado, name="secciones_listado"),
]