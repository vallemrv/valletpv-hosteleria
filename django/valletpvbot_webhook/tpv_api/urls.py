from django.urls import path
from . import views

urlpatterns = [
    # Registro y gestión de TPV
    path('register/', views.register_tpv, name='register_tpv'),
    path('info/', views.tpv_info, name='tpv_info'),
    path('heartbeat/', views.heartbeat, name='heartbeat'),
    path('stats/', views.stats, name='tpv_stats'),
    
    # Instrucciones
    path('instructions/', views.get_instructions, name='get_instructions'),
    path('instructions/<uuid:instruction_id>/', views.update_instruction, name='update_instruction'),
    
    # Push messages
    path('push/', views.send_push_message, name='send_push_message'),
    
    # Usuarios
    path('users/', views.get_users, name='get_users'),
]
