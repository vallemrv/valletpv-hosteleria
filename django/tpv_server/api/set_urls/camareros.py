from django.urls import path
from api.views.api_camareros import (crear_password,
                                      camarero_add, 
                                      listado,
                                      authorize_waiter)

urlpatterns  = [
  path("crear_password", crear_password, name="crear_password" ),
  path("camarero_add", camarero_add, name="camarero_add"),
  path("listado", listado, name="listado"),
  path("authorize_waiter", authorize_waiter, name="authorize_waiter"),
]