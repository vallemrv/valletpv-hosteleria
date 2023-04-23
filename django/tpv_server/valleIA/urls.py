from django.urls import path
from . import views

urlpatterns = [
    path('upload/', views.upload_audio, name='upload_audio'),
    path('gpt3_api/', views.gpt3_api, name='gpt3_api'),
    path('sql_query_view/', views.sql_query_view, name="sql_query_view"),
]
