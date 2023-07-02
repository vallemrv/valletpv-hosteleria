from django.urls import path
from valle_tpv import api as api_views

urlpatterns  =[
    path("update_from_devices/", api_views.update_from_devices, name="update_from_devices"),
    path("sync_devices/", api_views.sync_devices, name="sync_devices"),
    
]