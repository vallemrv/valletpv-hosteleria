from django.db import models
from django.db.models import Count, Sum, Q
from valle_tpv.tools.ws import comunicar_cambios_devices
from valle_tpv.models import (Camareros, Mesas, Mesasabiertas,
                             ComposicionTeclas, LineasCompuestas)



class Infmesa(models.Model):
    uid = models.CharField(db_column='UID', primary_key=True, unique=True, max_length=100)  # Field name made lowercase.
    camarero = models.ForeignKey(Camareros,  on_delete=models.CASCADE, db_column='IDCam')  # Field name made lowercase.
    fecha = models.DateField(db_column='Fecha')  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    numcopias = models.IntegerField(db_column='NumCopias', default=0)  # Field name made lowercase

    def unir_en_grupos(self):
        grupos = self.lineaspedido_set.filter(tecla_id__in=ComposicionTeclas.objects.values_list("tecla_id"))
        for gr in grupos:
            for comp in ComposicionTeclas.objects.filter(tecla_id=gr.tecla_id):
                comp_realizadas = LineasCompuestas.objects.filter(composicion__pk=comp.id,
                                                                    linea_principal__pk=gr.id)
                cantidad_realizada = comp_realizadas.count()
                if (comp.cantidad <= cantidad_realizada):
                    continue
                for a in self.lineaspedido_set.filter(estado="P").exclude(pk=gr.pk):
                    obj = comp_realizadas.filter(linea_compuesta=a.id).first()
                    if obj:
                        continue
                    composicion = comp.compToList()
                    
                    if a.tecla.familia.nombre in composicion:
                        a.precio = 0
                        a.estado = "M"
                        a.save()
                        comunicar_cambios_devices("md", "lineaspedido", a.serialize())
                        lComp = LineasCompuestas()
                        lComp.linea_principal = gr
                        lComp.linea_compuesta = a.id
                        lComp.composicion = comp
                        lComp.save()
                        cantidad_realizada = cantidad_realizada +1

                    if (comp.cantidad <= cantidad_realizada):
                        break

    def componer_articulos(self):
        lineas = self.lineaspedido_set.filter(Q(es_compuesta=False) & (Q(estado="P") | Q(estado="M")))
        obj_comp = []
        for l in lineas:
            if hasattr(l.tecla, "familia"):
                familia = l.tecla.familia
                composicion = familia.compToList()

                if (len(composicion) > 0):
                    cantidad = familia.cantidad - l.cantidad
                    
                    for a in lineas.order_by("id"):
                        if a.id == l.id:
                            continue
                        
                        if hasattr(a.tecla, "familia") and (a.tecla.familia.nombre in composicion):
                            if a.id not in obj_comp and cantidad > 0:
                                a.precio = 0
                                a.estado = "R"
                                a.save()
                                obj_comp.append(a.id)
                                cantidad = cantidad - 1
                                l.cantidad = l.cantidad + 1
                                l.save()

                            if cantidad == 0:
                                l.es_compuesta = True
                                l.save()
                                break; 
     
    def serialize(self):
        total_pedido = self.lineaspedido_set.filter(estado='P').aggregate(total=Sum('precio')) ["total"]
        total_r = self.lineaspedido_set.filter(estado='R').aggregate(total=Count('precio'))["total"]
        total_m = self.lineaspedido_set.filter(estado='M').aggregate(total=Count('precio'))["total"]
        total_cobrado = self.lineaspedido_set.filter(estado='C').aggregate(total=Sum('precio'))["total"]
        total_anulado = self.lineaspedido_set.filter(estado='A').aggregate(total=Sum('precio'))["total"]
        total_pedido = total_pedido if total_pedido else 0
        total_r = total_r if total_r else 0
        total_m = total_m if total_m else 0
        total_cobrado = total_cobrado if total_cobrado else 0
        total_anulado = total_anulado if total_anulado else 0
        mesa = Mesasabiertas.objects.filter(infmesa__pk=self.pk).first()
        if not mesa:
            split = self.pk.split("-")
            mesa = Mesas.objects.filter(pk=split[0]).first()
        else:
            mesa = mesa.mesa  

        mesa_id = -1 
        nomMesa = ""
        zona_id = -1
        if mesa:
            mesa_id = mesa.id
            nomMesa = mesa.nombre
            zona = mesa.mesaszona_set.first()
            if zona:
                zona_id = zona.id

        return {
            "UID": self.pk,
            "mesa_id": mesa_id,
            "num": self.numcopias,
            "abierta": 0,
            "nomMesa": nomMesa,
            "total_pedido": float(total_pedido),
            "total_regalado": float(total_r) + float(total_m),
            "total_anulado": float(total_anulado),
            "total_cobrado": float(total_cobrado),
            "hora": self.hora,
            "camarero": self.camarero.nombre + " " + self.camarero.apellidos,
            "zona_id": zona_id
        }

    def get_pedidos(self):
        pedidos = []
        for p in self.pedidos_set.all():
            camarero = Camareros.objects.filter(id=p.camarero_id).first()
            if not camarero: camarero = "Camarero borrado"
            else: camarero = camarero.nombre + " " + camarero.apellidos
            lineas = []
            for l in p.lineaspedido_set.values_list("descripcion", 
                                 "precio", 
                                 "estado").annotate(Cam=Count("idart"),
                                                    Total=Sum("precio")):
                lineas.append({
                    "Descripcion": l[0],
                    "Precio": float(l[4]),
                    "Can": l[3],
                    "Estado": l[2]
                })

            pedidos.append({
                "hora": p.hora,
                "camarero": camarero,
                "lineas": lineas
            })
        return pedidos

    class Meta:
        db_table = 'infmesa'
        ordering = ['-fecha']