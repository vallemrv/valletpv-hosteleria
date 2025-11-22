from django.urls import path
from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('telegram/', views.telegram_webhook, name='telegram_webhook'),
    path('info/', views.webhook_info, name='webhook_info'),
    path('api/register_notification/', views.register_notification, name='register_notification'),
    path('api/delete_message/', views.delete_message, name='delete_message'),
    path('api/edit_message/', views.edit_message, name='edit_message'),
]
