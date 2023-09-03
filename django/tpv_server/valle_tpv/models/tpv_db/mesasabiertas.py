from django.db import models
from django.db.models import Q
from valle_tpv.tools.ws import comunicar_cambios_devices 
from valle_tpv.models import Historialnulos
from datetime import datetime

class Mesasabiertas(models.Model):
    id = models.AutoField(primary_key=True) 
    infmesa = models.ForeignKey('Infmesa', on_delete=models.CASCADE, db_column='UID') 
    mesa = models.ForeignKey('Mesas', on_delete=models.CASCADE) 
 

    @staticmethod
    def borrar_mesa_abierta(idm, idc, motivo):
        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        if mesa_abierta:
            reg = mesa_abierta.infmesa.lineaspedido_set.filter((Q(estado="P") | 
                                                        Q(estado="M") | 
                                                        Q(estado="R")))
            delete = []
            for r in reg:
                historial = Historialnulos()
                historial.lineapedido_id = r.pk
                historial.camarero_id = idc
                historial.motivo = motivo
                historial.hora = datetime.now().strftime("%H:%M")
                historial.save()
                r.estado = 'A'
                r.save()
                delete.append(r.id)
               
            if (len(delete) > 0):
                comunicar_cambios_devices("delete", "lineaspedido", delete)


            obj = mesa_abierta.mesa.serialize()
            obj["abierta"] = False
            obj["num"] = 0
            comunicar_cambios_devices("update", "mesas", [obj])
            mesa_abierta.delete()
            



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
                comunicar_cambios_devices("updae", "mesas", [mesaP.serialize()])
           

    @staticmethod
    def juntar_mesas_abiertas(idp, ids):
        
        if idp != ids:
            mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
            mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()
            if mesaS:
                infmesa = mesaS.infmesa
                pedidos = infmesa.pedidos_set.all()
                for pedido in pedidos:
                    pedido.infmesa_id = mesaP.infmesa.pk
                    pedido.save()
                    for l in pedido.lineaspedido_set.all():
                        l.infmesa_id = mesaP.infmesa.pk
                        l.save()
                        comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                        
                obj = mesaS.serialize()
                obj["abierta"] = False
                obj["num"] = 0
                comunicar_cambios_devices("md", "mesasabiertas", obj)
                infmesa.delete()
    
    def serialize(self):
        obj = self.infmesa.serialize()
        obj["mesa_abierta_id"] = self.pk
        obj["abierta"] = True
        return obj

    def get_lineaspedido(self):
        lineas = []
        for l in self.infmesa.lineaspedido_set.filter(Q(infmesa__pk=self.infmesa.pk) 
                    & (Q(estado='P') | Q(estado="R") | Q(estado="M"))):
            lineas.append(l.serialize())
        return lineas


    class Meta:
        db_table = 'mesasabiertas'
        ordering = ['-id']

