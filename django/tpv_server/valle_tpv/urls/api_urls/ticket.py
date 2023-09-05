from django.urls import path
from valle_tpv.api import api_ticket as api

urlpatterns  = [
    path('lista', api.ticket_lista, name="api_tickets_lista"),
    path('lineas', api.ticket_lista_lineas, name="api_tickets_lista_lineas"),
]