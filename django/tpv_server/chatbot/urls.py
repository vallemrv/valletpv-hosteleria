# chatbot/urls.py

from django.urls import path

from chatbot.views.saved_messages_views import (
    saved_messages_list_create,
    saved_messages_detail,
    saved_messages_by_category,
    saved_messages_bulk_delete,
    saved_messages_stats,
    categories_list_create,
    categories_detail,
)


urlpatterns = [
    # Endpoints para mensajes guardados
    path('saved-messages/', saved_messages_list_create, name='saved-messages-list-create'),
    path('saved-messages/<int:message_id>/', saved_messages_detail, name='saved-messages-detail'),
    
    # Endpoints para categorías
    path('categories/', categories_list_create, name='categories-list-create'),
    path('categories/<int:category_id>/', categories_detail, name='categories-detail'),

    # Endpoints especiales
    path('saved-messages/by_category/<int:category_id>/', saved_messages_by_category, name='saved-messages-by-category'),
    path('saved-messages/bulk_delete/', saved_messages_bulk_delete, name='saved-messages-bulk-delete'),
    path('saved-messages/stats/', saved_messages_stats, name='saved-messages-stats'),
]
