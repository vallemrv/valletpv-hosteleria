from django.urls import path
from api_android import views as api_views

urlpatterns  = [
    path("listado", api_views.listado, name="camareros_listado"),
    path("es_autorizado", api_views.es_autorizado, name="camareros_es_autorizado"),
    path("crear_password", api_views.crear_password, name="camareros_crear_password"),
    path("listado_activos", api_views.listado_activos, name="camareros_listado_activos"),
    path("sel_camareros", api_views.sel_camareros, name="camareros_sel_camareros"),
    path("listado_autorizados", api_views.listado_autorizados, name="camareros_listado_autorizados"),
    path("camarero_add", api_views.camarero_add, name="camarero_add"),
]