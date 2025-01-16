from django.db import models
from django.db.models import Q
from valle_tpv.tools.ws import comunicar_cambios_devices 
from valle_tpv.models import Historialnulos
from datetime import datetime

class Mesasabiertas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase 
    infmesa = models.ForeignKey('Infmesa', on_delete=models.CASCADE, db_column='UID') 
    mesa = models.ForeignKey('Mesas', on_delete=models.CASCADE, db_column='IDMesa') 
 

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
    def cambiar_mesas_abiertas(idOrg, idDest):
        if idOrg != idDest:
            mesaOrg = Mesasabiertas.objects.filter(mesa__pk=idOrg).first()
            mesaDest = Mesasabiertas.objects.filter(mesa__pk=idDest).first()
           
            if mesaOrg and mesaDest:
                infmesaOrg = mesaOrg.infmesa
                infmesaDest = mesaDest.infmesa

                mesaOrg.infmesa = infmesaDest
                mesaOrg.save()

                mesaDest.infmesa = infmesaOrg
                mesaDest.save()

                comunicar_cambios_devices("update", "mesas", [mesaOrg.serialize(), mesaDest.serialize()])
            elif mesaOrg:
                mesaOrg.mesa_id = idDest
                mesaOrg.save()
                comunicar_cambios_devices("update", "mesas", [mesaOrg.serialize()])

           

    @staticmethod
    def juntar_mesas_abiertas(idOrg, idDest):
        if idOrg != idDest:
            mesaOrg = Mesasabiertas.objects.filter(mesa__pk=idOrg).first()
            mesaDest = Mesasabiertas.objects.filter(mesa__pk=idDest).first()

            if mesaDest:
                infmesaDest = mesaDest.infmesa
                pedidosDest = infmesaDest.pedidos_set.all()
                
                lineasModificadas = []
                for pedido in pedidosDest:
                    pedido.infmesa_id = mesaOrg.infmesa.pk
                    pedido.save()
                    for l in pedido.lineaspedido_set.all():
                        l.infmesa_id = mesaOrg.infmesa.pk
                        l.save()
                        lineasModificadas.append(l.serialize())
                    comunicar_cambios_devices("update", "lineaspedido", lineasModificadas)
                
                mesaDest.delete()
                comunicar_cambios_devices("update", "mesas", [mesaDest.serialize(), mesaOrg.serialize()])
                

    
    def serialize(self):
        obj = self.mesa.serialize()
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

