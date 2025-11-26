from django.db import models
from django.db.models import Count, Subquery, OuterRef
from django.db import transaction
from .historiales import Historialnulos

from .basemodels import BaseModels
from comunicacion.tools import comunicar_cambios_devices
from datetime import datetime

class Mesasabiertas(BaseModels):
    infmesa = models.ForeignKey('Infmesa', on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    mesa = models.ForeignKey('Mesas', on_delete=models.CASCADE, db_column='IDMesa')  # Field name made lowercase.

    def save(self, *args, **kwargs): 
        # Guardar el objeto
        super().save(*args, **kwargs)
        self.refresh_from_db()  # Forzar refresco desde DB después de guardar
        
        comunicar_cambios_devices("md", "mesas", self.mesa.serialize())
    
    
    def delete(self, *args, **kwargs):
        # Obtener datos antes de borrar para comunicar el cierre de mesa
        mesa = self.mesa
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar cierre de mesa a devices
        comunicar_cambios_devices("md", "mesas", mesa.serialize())

    
    @staticmethod
    @transaction.atomic
    def borrar_mesa_abierta(idm, idc, motivo):
        from .pedidos import Lineaspedido
        mesa = Mesasabiertas.objects.select_related('infmesa').filter(mesa__pk=idm).first()
        if not mesa:
            return

        lineas_a_anular = Lineaspedido.objects.filter(
            infmesa_id=mesa.infmesa_id, estado__in=["P", "M", "R"]
        )
        
        if lineas_a_anular.exists():
            historiales = [
                Historialnulos(lineapedido_id=linea.pk, camarero_id=idc, motivo=motivo, hora=datetime.now().strftime("%H:%M"))
                for linea in lineas_a_anular
            ]
            Historialnulos.objects.bulk_create(historiales)
            lineas_a_anular.update(estado='A')
            for linea in lineas_a_anular:
                comunicar_cambios_devices("rm", "lineaspedido", {"ID": linea.id})
        
        mesa.delete()
        

    @staticmethod
    @transaction.atomic
    def cambiar_mesas_abiertas(idp, ids):
        from .mesas import Mesas, Mesaszona
        from .pedidos import Lineaspedido
        from push_telegram.push_sender import mesa_cambiada
        
        if idp == ids: return
        
        mesaP = Mesasabiertas.objects.select_related('infmesa', 'mesa').select_for_update().filter(mesa__pk=idp).first()
        mesaS = Mesasabiertas.objects.select_related('infmesa', 'mesa').select_for_update().filter(mesa__pk=ids).first()

        if mesaP:
            if mesaS:
                # Intercambiar infmesa
                infmesa_id_p = mesaP.infmesa_id
                infmesa_id_s = mesaS.infmesa_id
                
                mesaP.infmesa_id = infmesa_id_s
                mesaS.infmesa_id = infmesa_id_p
                
                mesaP.save()
                mesaS.save()
                
                mesaP.refresh_from_db()
                mesaS.refresh_from_db()
                
                # Actualizar artículos y grupos
                mesaP.infmesa.componer_articulos()
                mesaP.infmesa.unir_en_grupos()
                mesaS.infmesa.componer_articulos()
                mesaS.infmesa.unir_en_grupos()
                
                # Obtener todas las líneas afectadas en una sola query optimizada
                lineas_afectadas = Lineaspedido.objects.filter(
                    infmesa_id__in=[mesaP.infmesa_id, mesaS.infmesa_id],
                    estado__in=['P', 'R', 'M']
                ).select_related('pedido', 'tecla__familia__receptor').annotate(
                    servido_count=Count('servidos'),
                    id_mesa=Subquery(Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id')).values('mesa_id')[:1]),
                    nom_mesa=Subquery(Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id')).values('mesa__nombre')[:1]),
                    id_zona=Subquery(Mesaszona.objects.filter(mesa__mesasabiertas__infmesa_id=OuterRef('infmesa_id')).values('zona_id')[:1])
                )
                
                # Serializar y comunicar en lote
                lineas_serializadas = [l.serialize(prefetched=True) for l in lineas_afectadas]
                if lineas_serializadas:
                    comunicar_cambios_devices("md", "lineaspedido", lineas_serializadas)
                    
            else:
                # Solo mesaP existe, cambiar a ids
                mesaP.mesa_id = ids
                mesaP.save()
                mesaP.refresh_from_db()
                
                mesaP.infmesa.componer_articulos()
                mesaP.infmesa.unir_en_grupos()
                
                # Obtener líneas afectadas
                lineas_afectadas = Lineaspedido.objects.filter(
                    infmesa_id=mesaP.infmesa_id,
                    estado__in=['P', 'R', 'M']
                ).select_related('pedido', 'tecla__familia__receptor').annotate(
                    servido_count=Count('servidos'),
                    id_mesa=Subquery(Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id')).values('mesa_id')[:1]),
                    nom_mesa=Subquery(Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id')).values('mesa__nombre')[:1]),
                    id_zona=Subquery(Mesaszona.objects.filter(mesa__mesasabiertas__infmesa_id=OuterRef('infmesa_id')).values('zona_id')[:1])
                )
                
                # Serializar y comunicar en lote
                lineas_serializadas = [l.serialize(prefetched=True) for l in lineas_afectadas]
                if lineas_serializadas:
                    comunicar_cambios_devices("md", "lineaspedido", lineas_serializadas)
                    
                # Comunicar la mesa cerrada
                mesa_cerrada = Mesas.objects.get(pk=idp)
                comunicar_cambios_devices("md", "mesas", mesa_cerrada.serialize())
                
                mesa_cambiada(mesaP, mesaP.infmesa_id, lineas_afectadas, tipo_cambio="mesa_completa")
                 
                
    @staticmethod
    @transaction.atomic
    def juntar_mesas_abiertas(idp, ids):
        from .pedidos import Pedidos, Lineaspedido
        from .infmesa import Infmesa
        from .mesas import Mesaszona
        if idp == ids: return

        mesaP = Mesasabiertas.objects.select_related('infmesa', 'mesa').filter(mesa__pk=idp).first()
        mesaS = Mesasabiertas.objects.select_related('infmesa', 'mesa').filter(mesa__pk=ids).first()

        if mesaP and mesaS:
            infmesa_origen_id = mesaS.infmesa_id
            infmesa_destino_id = mesaP.infmesa_id

            # Actualizamos todas las líneas y pedidos en lote
            Lineaspedido.objects.filter(infmesa_id=infmesa_origen_id).update(infmesa_id=infmesa_destino_id)
            Pedidos.objects.filter(infmesa_id=infmesa_origen_id).update(infmesa_id=infmesa_destino_id)
 
            # Obtener líneas movidas (ahora en infmesa_destino) en una sola query optimizada
            lineas_movidas = Lineaspedido.objects.filter(
                infmesa_id=infmesa_destino_id,
                estado__in=['P', 'R', 'M']
            ).select_related('pedido', 'tecla__familia__receptor').annotate(
                servido_count=Count('servidos'),
                id_mesa=Subquery(Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id')).values('mesa_id')[:1]),
                nom_mesa=Subquery(Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id')).values('mesa__nombre')[:1]),
                id_zona=Subquery(Mesaszona.objects.filter(mesa__mesasabiertas__infmesa_id=OuterRef('infmesa_id')).values('zona_id')[:1])
            )
            
            # Serializar y comunicar en lote las líneas movidas
            lineas_serializadas = [l.serialize(prefetched=True) for l in lineas_movidas]
            if lineas_serializadas:
                comunicar_cambios_devices("md", "lineaspedido", lineas_serializadas)
            
            # Borramos la mesa e infmesa que se han quedado vacías
            mesaS.delete()
            Infmesa.objects.filter(pk=infmesa_origen_id).delete()
            
            # Actualizar artículos y grupos en la mesa destino
            mesaP.infmesa.componer_articulos()
            mesaP.infmesa.unir_en_grupos()
            
            
    def get_lineaspedido(self):
        from .pedidos import Lineaspedido
        from .mesas import Mesaszona
       
        # --- Subconsultas CORREGIDAS Y SIMPLIFICADAS ---
        mesa_abierta_sq = Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id'))
        id_mesa_sq = mesa_abierta_sq.values('mesa_id')[:1]
        nom_mesa_sq = mesa_abierta_sq.values('mesa__nombre')[:1]

        # Esta es la versión robusta que no anida subconsultas
        zona_id_sq = Mesaszona.objects.filter(
            mesa__mesasabiertas__infmesa_id=OuterRef('infmesa_id')
        ).values('zona_id')[:1]

        # Hacemos la consulta optimizada con las subconsultas correctas
        lineas_qs = Lineaspedido.objects.filter(
            infmesa_id=self.infmesa_id, 
            estado__in=['P', 'R', 'M']
        ).select_related(
            'pedido', 'tecla__familia__receptor'
        ).annotate(
            servido_count=Count('servidos'),
            id_mesa=Subquery(id_mesa_sq),
            nom_mesa=Subquery(nom_mesa_sq),
            id_zona=Subquery(zona_id_sq)
        ).order_by("-id")

        # Serializamos usando el camino rápido
        return [l.serialize(prefetched=True) for l in lineas_qs]


    class Meta:
        db_table = 'mesasabiertas'
        ordering = ['-id']
