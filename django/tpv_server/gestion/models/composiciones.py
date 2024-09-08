from django.db import models
from .basemodels import BaseModels
import json


class ComposicionTeclas(BaseModels):
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.
    composicion = models.CharField(max_length=300)  # Field name made lowercase.
    cantidad = models.IntegerField()  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        rows = ComposicionTeclas.objects.all()
        objs = []
        for r in rows: 
            objs.append(r.serialize())
        return objs


    def compToList(self):
        try:
            return json.loads(self.composicion.replace("'", '"'))
        except:
            return []

    def serialize(self):
        # Llamar al método `serialize` del padre usando `super()`
        aux = super().serialize()  # Invoca el método `serialize` de la clase padre
        
        # Modificar o añadir información a lo que devuelve `super().serialize()`
        aux["nombre"] = self.tecla.nombre  # Añades o modificas el campo `nombre` en el resultado
        
        # Retornar el diccionario modificado
        return aux


    class Meta:
        db_table = 'composicionteclas'
        ordering = ['id']

class LineasCompuestas(models.Model):
    linea_principal = models.ForeignKey('Lineaspedido', models.CASCADE)
    linea_compuesta = models.IntegerField()
    composicion = models.ForeignKey(ComposicionTeclas, models.CASCADE)
