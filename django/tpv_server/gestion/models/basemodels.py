from django.db import models
from .sync import Sync
from django.forms.models import model_to_dict

class BaseModels(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    

    def serialize(self):
        return model_to_dict(self)   
    

    def save(self, *args, **kwargs):
        sync = Sync.objects.filter(nombre=self._meta.db_table).first()
        if not sync:
            sync = Sync()
        sync.nombre = self._meta.db_table
        sync.save()
        return super().save(*args, **kwargs)
        

    def delete(self, *args, **kwargs):
        sync = Sync.objects.filter(nombre=self._meta.db_table).first()
        if not sync:
            sync = Sync()
        sync.nombre = self._meta.db_table
        sync.save()
        return super().delete( *args, **kwargs)
    
    class Meta:
        abstract = True  # Indica que este modelo es abstracto