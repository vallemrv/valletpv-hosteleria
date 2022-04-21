import imp
from django.urls import path
from . import views

app_name = "app"
urlpatterns = [
    path('', views.inicio, name="inicio"),
    path('getlistado', views.getlistado, name="getlistado")
]