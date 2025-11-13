from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include('webhook.urls')),  # Raíz para home y telegram
    path('api/tpv/', include('tpv_api.urls')),
]
