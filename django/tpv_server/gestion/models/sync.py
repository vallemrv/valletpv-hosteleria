from django.db import models
from datetime import datetime

class Sync(models.Model):
    nombre = models.CharField(max_length=50) 
    last = models.CharField(max_length=26)

    @staticmethod
    def actualizar(tb_name):
        sync = Sync.objects.filter(nombre=tb_name).first()
        if not sync:
            sync = Sync()
        sync.nombre = tb_name.lower()
        sync.last = datetime.now().strftime("%Y-%m-%d-%H:%M:%S")
        sync.save()
       