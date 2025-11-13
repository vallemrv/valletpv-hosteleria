"""
Celery configuration for valletpvbot_webhook project.
"""

import os
from celery import Celery

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')

app = Celery('valletpvbot_webhook')
app.config_from_object('django.conf:settings', namespace='CELERY')
app.autodiscover_tasks()
