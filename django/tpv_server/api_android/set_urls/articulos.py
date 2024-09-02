from django.urls import path
from api_android import views as api_views

urlpatterns  =[
  path("lstodos", api_views.ls_todos, name="articulos_ls_todos"),
  path('listado', api_views.art_listado, name="articulos_listado"),
]
