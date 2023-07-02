from django.urls import path
from valle_tpv.api import api_user_profile as api_views

urlpatterns  =[
    path("get_profile/", api_views.get_profile, name="ge_profile"),
    path("update_profile/", api_views.update_profile, name="update_profile"),
]