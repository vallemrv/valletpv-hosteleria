# @Author: Manuel Rodriguez <valle>
# @Date:   10-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-26T14:13:20+01:00
# @License: Apache license vesion 2.0
    
from django.urls import path
from . import consumers

websocket_urlpatterns = [
    path('ws/gestion_ia/<user>/', consumers.ChatConsumer.as_asgi()),
]
