from django.urls import path
from api_android.views import api_sync as api_views

urlpatterns  =[
    path("update_for_devices", api_views.update_for_devices, name="update_for_devices"),
    path("get_tb_up_last", api_views.get_tb_up_last, name="get_tb_up_last"),
    path("update_from_devices", api_views.update_from_devices, name="update_from_devices"),
    path("sync_devices", api_views.sync_devices, name="sync_devices")
]