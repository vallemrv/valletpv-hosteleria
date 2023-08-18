from django.urls import path
from valle_tpv.api import api_camareros as api_views

urlpatterns  =[
    path("add", api_views.add, name="add_camarero"),
    path("set_password", api_views.set_password, name="set_password_camarero"),
    path("set_autorizado", api_views.set_autorizado, name="set_autorizado_camarero"),
]
