from django.db import models
from django.forms.models import model_to_dict
from datetime import datetime
from comunicacion.tools import send_mensaje_impresora
from .basemodels import BaseModels


class HistorialMensajes(BaseModels):
    camarero = models.ForeignKey("Camareros", on_delete=models.CASCADE)
    mensaje = models.CharField(max_length=300)
    receptor = models.ForeignKey("Receptores", on_delete=models.CASCADE)
    hora = models.CharField(max_length=10)
    fecha = models.CharField( max_length=10)


    @staticmethod
    def update_from_device(row):
        c = None
        if "ID" in row:
            c = HistorialMensajes.objects.filter(id=row["ID"]).first()
        if not c:
            c = HistorialMensajes()
            
        c.camarero_id = row["camarero"]
        c.receptor_id = row["receptor"]
        c.mensaje = row["mensaje"]
        c.hora = datetime.now().strftime("%H:%M:%S")
        c.fecha = datetime.now().strftime("%Y-%m-%d")
        c.save()
        mensaje = {
            "op": "mensaje",
            "camarero": c.camarero.nombre + " " + c.camarero.apellidos,
            "msg": c.mensaje,
            "receptor": c.receptor.nomimp,
            "hora": c.hora,
            "nom_receptor": c.receptor.nombre
        }
        send_mensaje_impresora(mensaje)

    @staticmethod
    def update_for_devices():
        rows = HistorialMensajes.objects.all()
        tb = []
        for r in rows:
            tb.append(model_to_dict(r))
        return tb
    
class Historialnulos(BaseModels):
    lineapedido = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE, db_column='IDLPedido')  # Field name made lowercase.
    camarero = models.ForeignKey('Camareros',  on_delete=models.CASCADE, db_column='IDCam')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    motivo = models.CharField(db_column='Motivo', max_length=200)  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        a = []
        for obj in Historialnulos.objects.all():
            a.append(obj.serialize())
        return a

    def serialize(self):
        obj = model_to_dict(self)
        obj["camarero"] = self.camarero.nombre + " " + self.camarero.apellidos
        obj["lineapedido"] = self.lineapedido.serialize()
        return obj

    

    class Meta:
        db_table = 'historialnulos'
        ordering = ['-id']