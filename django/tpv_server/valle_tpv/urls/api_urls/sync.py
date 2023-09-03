from django.urls import path
from valle_tpv.api import api_sync as api_views

urlpatterns  =[
    path("update_from_devices", api_views.update_from_devices, name="update_from_devices"),
    path("sync_devices", api_views.sync_devices, name="sync_devices"),
    
]