from django.db import models
from .basemodels import BaseModels
from .camareros import Camareros
from .mesas import Mesas
import json

class PeticionesAutoria(BaseModels):
    idautorizado = models.ForeignKey("Camareros", on_delete=models.CASCADE)
    accion = models.CharField(max_length=150)
    instrucciones = models.CharField(max_length=300)


    def serialize(self):
        inst = json.loads(self.instrucciones)
        if self.accion == "borrar_mesa":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            motivo = inst["motivo"]
            return {"tipo": "peticion", "idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje":camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar la mesa completa "
            + mesa.nombre +" por el motivo:  "+ motivo}
        elif self.accion == "borrar_linea":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            nombre = inst["Descripcion"]
            can = inst["can"]
            motivo = inst["motivo"]
            return {"tipo": "peticion", "idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje":camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar " + can +" "+nombre + " de la mesa "
            + mesa.nombre +" por el motivo:  "+ motivo}
        elif self.accion == "informacion":
            return {"tipo": "informacion", "idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje": "Mensaje de "+inst["autor"]+": \n "+ inst["mensaje"]}
        elif self.accion == "cobrar_ticket":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            return {"tipo":"peticion","idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje": camarero.nombre + " "
            + camarero.apellidos+" solicita permiso para cobrar "+ mesa.nombre}
        elif self.accion == "abrir_cajon":
            camarero = Camareros.objects.get(id=inst["idc"])
            return {"tipo":"peticion","idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje": camarero.nombre + " "
            + camarero.apellidos+" solicita permiso para abrir cajon" }


    class Meta:
        ordering = ['-id']

   
