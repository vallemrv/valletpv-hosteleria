from django.urls import path
from api_android.views import api_openai as api_views

urlpatterns  =[
    path("transcribe", api_views.subir_audio, name="transcribe"),
    path('buscar_ids', api_views.buscar_ids, name='buscar_ids'),
    path('procesar_pedido', api_views.procesar_pedido, name='procesar_pedido'),
]