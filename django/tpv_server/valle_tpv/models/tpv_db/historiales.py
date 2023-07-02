
from django.db import models
from django.forms.models import model_to_dict
from datetime import datetime
from valle_tpv.tools.ws import send_mensaje_impresora


class HistorialMensajes(models.Model):
    camarero = models.ForeignKey("Camareros", on_delete=models.CASCADE)
    mensaje = models.CharField(max_length=300)
    receptor = models.ForeignKey("Receptores", on_delete=models.CASCADE)
    hora = models.CharField(max_length=10)
    fecha = models.DateField()


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
        c.fecha = datetime.now()   
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

   
    class Meta:
        tb_name = 'historialmensajes'
        ordering = ['-id']

class Historialnulos(models.Model):
    id = models.AutoField(primary_key=True)  # Field name made lowercase.
    lineapedido = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE)  # Field name made lowercase.
    camarero = models.ForeignKey('Camareros',  on_delete=models.CASCADE)  # Field name made lowercase.
    hora = models.CharField(max_length=5)  # Field name made lowercase.
    motivo = models.CharField(max_length=200)  # Field name made lowercase.
    

    def serialize(self):
        obj = model_to_dict(self)
        obj["camarero"] = self.camarero.nombre + " " + self.camarero.apellidos
        obj["lineapedido"] = self.lineapedido.serialize()
        obj["fecha"] = self.lineapedido.infmesa.fecha.strftime("%d/%m/%Y")
        return obj


    class Meta:
        tb_name = 'historialnulos'
        ordering = ['-id']
