from django.urls import path
from api_android.views import api_openai as api_views

urlpatterns  =[
    path("transcribe", api_views.subir_audio, name="transcribe"),
]