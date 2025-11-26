from django.urls import path
from api.views import api_mesas as api_views

urlpatterns  =[
    path("lszonas", api_views.ls_zonas, name="mesas_ls_zonas"),
    path("lstodaslasmesas", api_views.lstodaslasmesas, name="mesas_lstodaslasmesas"),
    path("lsmesasabiertas", api_views.lsmesasabiertas, name="mesas_lsmesasabiertas"),
    path("mesa_action", api_views.mesa_action, name="mesas_mesa_action"),
]
