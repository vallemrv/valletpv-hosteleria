from django.db import models
from django.db.models import  Q
from .historiales import Historialnulos
from .basemodels import BaseModels
from comunicacion.tools import comunicar_cambios_devices
from datetime import datetime

class Mesasabiertas(BaseModels):
    infmesa = models.ForeignKey('Infmesa', on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    mesa = models.ForeignKey('Mesas', on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.

    @staticmethod
    def update_for_devices():
        mesas = Mesasabiertas.objects.all()
        objs = []
        for m in mesas:
            objs.append(m.serialize())
        return objs

    @staticmethod
    def borrar_mesa_abierta(idm, idc, motivo):
        from .pedidos import Lineaspedido
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
        if idp == ids:
            return  # No need to do anything if both IDs are the same

        mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
        mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()

        if mesaP:
            if mesaS:
                # Swap the infmesa between Mesa P and Mesa S
                mesaP.infmesa, mesaS.infmesa = mesaS.infmesa, mesaP.infmesa
                mesaP.save()
                mesaS.save()

                mesaS.infmesa.componer_articulos()
                mesaS.infmesa.unir_en_grupos()
            else:
                # If Mesa S doesn't exist, move Mesa P to the new ID (ids)
                mesaP.mesa_id = ids
                mesaP.save()
                mesaP.infmesa.componer_articulos()
                mesaP.infmesa.unir_en_grupos()

                # Notify devices of the change and close the original mesa
                comunicar_cambios_devices("md", "mesasabiertas", [mesaP.serialize(), {"ID": idp, "num": 0, "abierta": 0}])
        else:
            # If Mesa P doesn't exist, ensure sync is updated
            Sync.actualizar("mesasabiertas")


    @staticmethod
    def juntar_mesas_abiertas(idp, ids):
        from .pedidos import Pedidos
        if idp != ids:
            mesaP = Mesasabiertas.objects.filter(mesa__pk=idp).first()
            mesaS = Mesasabiertas.objects.filter(mesa__pk=ids).first()
            if mesaS:
                infmesa = mesaS.infmesa
                infmesaP = mesaP.infmesa
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
                infmesaP.componer_articulos()
                infmesaP.unir_en_grupos()

    
    def serialize(self):
        obj = self.infmesa.serialize()
        obj["PK"] = self.pk
        obj["abierta"] = 1
        return obj

    def get_lineaspedido(self):
        from .pedidos import Lineaspedido
        lineas = []
        for l in Lineaspedido.objects.filter(Q(infmesa__pk=self.infmesa.pk) 
                    & (Q(estado='P') | Q(estado="R") | Q(estado="M"))):
            lineas.append(l.serialize())
        return lineas



    class Meta:
        db_table = 'mesasabiertas'
        ordering = ['-id']
