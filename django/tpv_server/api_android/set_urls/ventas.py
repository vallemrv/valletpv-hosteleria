from django.urls import path
from api_android import views as api_views

urlpatterns  = [
  path("get_nulos", api_views.get_nulos, name="get_nulos"),
  path("get_infmesa", api_views.get_infmesa, name="get_infmesa"),
]