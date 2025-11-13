from django.urls import path
from api.views import api_sync as api_views

urlpatterns  =[
    path("sync_devices", api_views.sync_devices, name="sync_devices")
]