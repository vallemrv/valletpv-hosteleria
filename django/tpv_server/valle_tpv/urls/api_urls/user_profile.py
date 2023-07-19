from django.urls import path
from valle_tpv.api import api_user_profile as api_views

urlpatterns  =[
    path("get_profile/", api_views.get_profile, name="ge_profile"),
    path("update_profile/", api_views.update_profile, name="update_profile"),
    path("create/", api_views.create_user, name="create"),
    path("get_list/", api_views.get_list, name="get_list"),
]