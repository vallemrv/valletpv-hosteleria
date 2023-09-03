from django.urls import path, include
    
app_name = "valle_tpv"

urlpatterns = [
    path("api/", include("valle_tpv.urls.api"), name="api")
]
