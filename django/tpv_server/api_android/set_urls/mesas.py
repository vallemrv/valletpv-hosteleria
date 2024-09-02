from django.urls import path
from api_android import views as api_views

urlpatterns  =[
    path("lszonas", api_views.ls_zonas, name="mesas_ls_zonas"),
    path("lstodaslasmesas", api_views.lstodaslasmesas, name="mesas_lstodaslasmesas"),
    path("lsmesasabiertas", api_views.lsmesasabiertas, name="mesas_lsmesasabiertas"),
]
