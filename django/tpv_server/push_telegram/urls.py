# push_telegram/urls.py
# URLs para el módulo de push de Telegram

from django.urls import path

app_name = 'push_telegram'

# Este módulo solo envía notificaciones push
# NO tiene endpoints propios (los webhooks los maneja valletpvbot_webhook)
urlpatterns = []
