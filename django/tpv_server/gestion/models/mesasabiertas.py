from django.db import models
from django.db.models import Q
from datetime import datetime
from comunicacion.tools import comunicar_cambios_devices 
from gestion.models import (Infmesa, Mesas, Pedidos, 
                            Historialnulos, Lineaspedido)

class Mesasabiertas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    infmesa = models.ForeignKey(Infmesa, on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    mesa = models.ForeignKey(Mesas, on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        mesas = Mesasabiertas.objects.all()
        objs = []
        for m in mesas:
            objs.append(m.serialize())
        return objs

    @staticmethod
    def borrar_mesa_abierta(idm, idc, motivo):
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        if mesa:
            uid = mesa.infmesa.pk
            reg = Lineaspedido.objects.filter((Q(estado="P") | Q(estado="M") | Q(estado="R")) & Q(infmesa__pk=uid))
            for r in reg:
                historial = Historialnulos()
                historial.lineapedido_id = r.pk
                historial.camarero_id = idc
                historial.motivo = motivo
                historial.hora = datetime.now().strftime("%H:%M")
                historial.save()
                r.estado = 'A'
                r.save()
                comunicar_cambios_devices("rm", "lineaspedido", {"ID":r.id}, {"op": "borrado", "precio": float(r.precio)})


            obj = mesa.serialize()
            obj["abierta"] = 0
            obj["num"] = 0
            comunicar_cambios_devices("md", "mesasabiertas", obj)
            mesa.delete()

    @staticmethod
    def cambiar_mesas_abiertas(idp, ids):
        if idp != ids:
            mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
            mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()
            if mesaS:
                infmesa = mesaS.infmesa
                mesaS.infmesa = mesaP.infmesa
                mesaS.save()
                mesaP.infmesa = infmesa;
                mesaP.save()
            elif mesaP:
                mesaP.mesa_id = ids
                mesaP.save()
                comunicar_cambios_devices("md", "mesasabiertas", [mesaP.serialize(), {"ID":idp,"num":0, "abierta":0}])
           

    @staticmethod
    def juntar_mesas_abiertas(idp, ids):
        if idp != ids:
            mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
            mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()
            if mesaS:
                infmesa = mesaS.infmesa
                pedidos = Pedidos.objects.filter(infmesa__pk=infmesa.pk)
                for pedido in pedidos:
                    pedido.infmesa_id = mesaP.infmesa.pk
                    pedido.save()
                    for l in pedido.lineaspedido_set.all():
                        l.infmesa_id = mesaP.infmesa.pk
                        l.save()
                        comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                        
                obj = mesaS.serialize()
                obj["abierta"] = 0
                obj["num"] = 0
                comunicar_cambios_devices("md", "mesasabiertas", obj)
                infmesa.delete()
    
    def serialize(self):
        obj = self.infmesa.serialize()
        obj["PK"] = self.pk
        obj["abierta"] = 1
        return obj

    def get_lineaspedido(self):
        lineas = []
        for l in Lineaspedido.objects.filter(Q(infmesa__pk=self.infmesa.pk) 
                    & (Q(estado='P') | Q(estado="R") | Q(estado="M"))):
            lineas.append(l.serialize())
        return lineas


    class Meta:
        db_table = 'mesasabiertas'
        ordering = ['-id']
