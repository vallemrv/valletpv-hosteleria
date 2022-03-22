from django.test import TestCase

# Create your tests here.
from websocket import create_connection

ws = create_connection("ws://0.0.0.0:8000/ws/comunicacion/caja")
print(ws.recv())


