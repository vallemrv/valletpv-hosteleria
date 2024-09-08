from django.urls import path
from api_android.views.api_articulos import ls_todos, art_listado

urlpatterns  =[
  path("lstodos", ls_todos, name="articulos_ls_todos"),
  path('listado', art_listado, name="articulos_listado"),
]
