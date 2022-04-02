from django.urls import path
from api_android import views as api_views

urlpatterns  =[
    path("getupdate", api_views.getupdate, name="sync_getupdate"),
    path("get_eco", api_views.get_eco, name="get_eco"),
    path("know_connect", api_views.know_connect, name="know_connect"),
    path("lastsync", api_views.lastsync, name="lastsync"),
    path("firstsync", api_views.firstsync, name="firstsync"),
    path("get_update_tables", api_views.get_update_tables, name="get_update_tables"),
    path("update_for_devices", api_views.update_for_devices, name="update_for_devices"),
    path("get_tb_up_last", api_views.get_tb_up_last, name="get_tb_up_last"),
    path("update_from_devices", api_views.update_from_devices, name="update_from_devices")
]