from django.db import models

class InfModelos(models.Model):
    nombre = models.CharField(max_length=100)
    sinonimos = models.TextField(blank=True)
    columnas = models.TextField(blank=True)
    relaciones = models.TextField(blank=True)

    def __str__(self):
        return self.nombre