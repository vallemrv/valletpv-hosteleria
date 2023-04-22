from django.urls import path
from . import views

urlpatterns = [
    path('upload/', views.upload_audio, name='upload_audio'),
    path('gpt3/', views.GPT3View.as_view(), name='gpt3'),
    path('sql_query_view/', views.sql_query_view, name="sql_query_view"),
]
