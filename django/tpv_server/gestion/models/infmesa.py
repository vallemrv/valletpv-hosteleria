from django.db import models
from django.db.models import Q, Sum, Count, Value, FloatField
from django.db.models.functions import Coalesce
from django.db import transaction
from .basemodels import BaseModels
from .camareros import Camareros
from .composiciones import ComposicionTeclas, LineasCompuestas
from .mesasabiertas import Mesasabiertas

 

class Infmesa(BaseModels):
    id = models.CharField(db_column='UID', primary_key=True, unique=True, max_length=100)  # Field name made lowercase.
    camarero = models.ForeignKey('Camareros',  on_delete=models.CASCADE, db_column='IDCam')  # Field name made lowercase.
    fecha = models.CharField(db_column='Fecha', max_length=10)  # Field name made lowercase.
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    numcopias = models.IntegerField(db_column='NumCopias', default=0)  # Field name made lowercase
    
    @staticmethod
    def annotate_totals(queryset):
        
        return queryset.annotate(
            total_pedido=Coalesce(Sum('lineaspedido__precio', filter=Q(lineaspedido__estado='P')), Value(0), output_field=FloatField()),
            count_regalado=Coalesce(Count('lineaspedido__id', filter=Q(lineaspedido__estado='R')), Value(0)),
            count_promo=Coalesce(Count('lineaspedido__id', filter=Q(lineaspedido__estado='M')), Value(0)),
            total_cobrado=Coalesce(Sum('lineaspedido__precio', filter=Q(lineaspedido__estado='C')), Value(0), output_field=FloatField()),
            total_anulado=Coalesce(Sum('lineaspedido__precio', filter=Q(lineaspedido__estado='A')), Value(0), output_field=FloatField()),
        ) 
    
    @property
    def uid(self):
        return self.id  # Definir alias para acceder a `uid` como `id`
    
    @property
    def pk(self):
        return self.id  # Definir alias para acceder a `uid` como `pk`

    
    @transaction.atomic
    def unir_en_grupos(self):
        # ... (la lógica interna es compleja, pero podemos optimizar la creación)
        grupos = self.lineaspedido_set.filter(tecla_id__in=ComposicionTeclas.objects.values_list("tecla_id"))
        ids_modificados = []
        for gr in grupos:
            for comp in ComposicionTeclas.objects.filter(tecla_id=gr.tecla_id):
                cantidad_realizada = LineasCompuestas.objects.filter(composicion_id=comp.id, linea_principal_id=gr.id).count()
                if comp.cantidad <= cantidad_realizada:
                    continue
                
                lineas_a_modificar_pks = []
                lineas_compuestas_a_crear = []

                for a in self.lineaspedido_set.filter(estado="P").exclude(pk=gr.pk):
                    # ... (la lógica de la composición es muy específica, la mantenemos)
                    if LineasCompuestas.objects.filter(composicion_id=comp.id, linea_principal_id=gr.id, linea_compuesta=a.id).exists():
                        continue

                    if a.tecla.familia.nombre in comp.compToList():
                        lineas_a_modificar_pks.append(a.pk)
                        lineas_compuestas_a_crear.append(
                            LineasCompuestas(linea_principal=gr, linea_compuesta=a.id, composicion=comp)
                        )
                        cantidad_realizada += 1
                        if comp.cantidad <= cantidad_realizada:
                            break
                
                if lineas_a_modificar_pks:
                    # Actualizamos todas las líneas a la vez
                    lineas_modificadas = self.lineaspedido_set.filter(pk__in=lineas_a_modificar_pks)
                    lineas_modificadas.update(precio=0, estado="M")
                    ids_modificados.extend(lineas_a_modificar_pks)
                   
                if lineas_compuestas_a_crear:
                    # Creamos los objetos en lote
                    LineasCompuestas.objects.bulk_create(lineas_compuestas_a_crear)
        return ids_modificados

    @transaction.atomic
    def componer_articulos(self):
        # Esta función es difícil de optimizar con bulk_update por la lógica de 'cantidad',
        # pero la mantenemos dentro de una transacción para seguridad.
        # (El código interno no cambia, pero ahora es más seguro)
        lineas = self.lineaspedido_set.filter(Q(es_compuesta=False) & (Q(estado="P") | Q(estado="M")))
        obj_comp = []
        ids_modificados = []
        for l in lineas:
            if hasattr(l.tecla, "familia"):
                familia = l.tecla.familia
                composicion = familia.compToList()
                if (len(composicion) > 0):
                    cantidad = familia.cantidad - l.cantidad
                    for a in lineas.order_by("id"):
                        if a.id == l.id: continue
                        if hasattr(a.tecla, "familia") and (a.tecla.familia.nombre in composicion):
                            if a.id not in obj_comp and cantidad > 0:
                                a.precio = 0
                                a.estado = "R"
                                a.save()
                                obj_comp.append(a.id)
                                ids_modificados.append(a.id)
                                cantidad -= 1
                                l.cantidad += 1
                                l.save()
                                ids_modificados.append(l.id)
                            if cantidad == 0:
                                l.es_compuesta = True
                                l.save()
                                ids_modificados.append(l.id)
                                break;
        return ids_modificados

        
    # En tu clase Infmesa, REEMPLAZA tu método serialize por este:

    def serialize(self, prefetched=False):
        
        # Si los datos vienen precargados desde una consulta optimizada
        if prefetched:
            total_pedido = float(getattr(self, 'total_pedido', 0))
            total_regalado = float(getattr(self, 'count_regalado', 0) + getattr(self, 'count_promo', 0))
            total_anulado = float(getattr(self, 'total_anulado', 0))
            total_cobrado = float(getattr(self, 'total_cobrado', 0))
            mesa_id = getattr(self, 'mesa_id', -1)
            nomMesa = getattr(self, 'nomMesa', '')
            zona_id = getattr(self, 'zona_id', -1)
            abierta = 1
        else:
            # Camino lento (cuando se serializa un solo objeto) con la consulta CORREGIDA Y OPTIMIZADA
            # 1. Hacemos todos los cálculos de totales en UNA SOLA consulta
            totales = self.lineaspedido_set.aggregate(
                total_pedido=Coalesce(Sum('precio', filter=Q(estado='P')), Value(0), output_field=FloatField()),
                count_regalado=Coalesce(Count('id', filter=Q(estado='R')), Value(0)),
                count_promo=Coalesce(Count('id', filter=Q(estado='M')), Value(0)),
                total_cobrado=Coalesce(Sum('precio', filter=Q(estado='C')), Value(0), output_field=FloatField()),
                total_anulado=Coalesce(Sum('precio', filter=Q(estado='A')), Value(0), output_field=FloatField())
            )
            total_pedido = totales['total_pedido']
            total_regalado = float(totales['count_regalado'] + totales['count_promo'])
            total_anulado = totales['total_anulado']
            total_cobrado = totales['total_cobrado']
            
            # 2. Obtenemos la mesa y la zona de forma correcta
            mesa_abierta = Mesasabiertas.objects.filter(infmesa_id=self.pk).select_related('mesa').first()
            mesa_id, nomMesa, zona_id, abierta = -1, "", -1, 0
            if mesa_abierta:
                mesa = mesa_abierta.mesa
                mesa_id = mesa.id
                nomMesa = mesa.nombre
                abierta = 1
                # La búsqueda correcta de la zona
                mesazona = mesa.mesaszona_set.select_related('zona').first() 
                if mesazona:
                    zona_id = mesazona.zona.id
                
        return {
            "PK": self.pk,
            "ID": mesa_id,
            "num": self.numcopias,
            "abierta": abierta,
            "nomMesa": nomMesa,
            "fecha": self.fecha,
            "total_pedido": total_pedido,
            "total_regalado": total_regalado,
            "total_anulado": total_anulado,
            "total_cobrado": total_cobrado,
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
