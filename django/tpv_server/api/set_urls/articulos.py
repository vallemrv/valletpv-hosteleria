from django.urls import path
from api.views.api_articulos import agregar_articulo, borrar_articulo

urlpatterns = [
    path('agregar', agregar_articulo, name='agregar_articulo'),
    path('borrar', borrar_articulo, name='borrar_articulo'),  # Cambiar para aceptar POST
]
