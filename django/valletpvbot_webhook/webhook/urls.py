from django.urls import path
from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('telegram/', views.telegram_webhook, name='telegram_webhook'),
    path('info/', views.webhook_info, name='webhook_info'),
]
