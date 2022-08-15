
from django.db import models

class Suscripcion(models.Model):
    email = models.EmailField()
    fecha = models.DateTimeField(auto_now=True)
    estado = models.BooleanField(default=True)
