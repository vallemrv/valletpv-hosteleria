from django.db import transaction
from django.db import models
from .historiales import Historialnulos
from .basemodels import BaseModels
from comunicacion.tools import comunicar_cambios_devices
from gestion.decorators.log_excepciones import log_excepciones
from gestion.tools.config_logs import logger_sync as logger
from datetime import datetime
from django.db.models import Count, Subquery, OuterRef, Q
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.infmesa import Infmesa
from .mesas import Mesaszona
from uuid import uuid4

class Pedidos(BaseModels):
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    infmesa = models.ForeignKey('Infmesa',  on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    uid_device = models.CharField(max_length=150, default="589274e0-4ae3-4008-a00b-ec7a19d74d01")
    
    
    @staticmethod
    @transaction.atomic
    @log_excepciones
    def agregar_nuevas_lineas(idm, idc, lineas, uid_device):
       
        # --- Lógica de Pedido y Mesa (sin cambios) ---
        if Pedidos.objects.filter(uid_device=uid_device).exists():
            return None

        mesa = Mesasabiertas.objects.select_related('infmesa').filter(mesa__pk=idm).first()

        if not mesa:
            infmesa = Infmesa.objects.create(
                id=f"{idm}-{str(uuid4())}",
                camarero_id=idc,
                hora=datetime.now().strftime("%H:%M"),
                fecha=datetime.now().strftime("%Y/%m/%d")
            )
            mesa = Mesasabiertas.objects.create(
                mesa_id=idm,
                infmesa=infmesa
            )
        else:
            infmesa = mesa.infmesa

        pedido = Pedidos.objects.create(
            infmesa=infmesa,
            hora=datetime.now().strftime("%H:%M"),
            camarero_id=idc,
            uid_device=uid_device
        )

        # Recolectar objetos para bulk_create y IDs para comunicar
        lineas_objs = []
        lineas_a_eliminar = []
        for pd in lineas:
            id_articulo = pd.get("IDArt") or pd.get("ID") or -1
            linea = Lineaspedido(
                infmesa=infmesa,
                idart=id_articulo,
                pedido=pedido,
                descripcion=pd.get("Descripcion", ""),
                descripcion_t=pd.get("descripcion_t", ""),
                precio=pd["Precio"],
                estado='P',
                tecla_id=id_articulo if int(id_articulo) > 0 else None,
                id_local=pd.get("ID", None)
            )
            lineas_objs.append(linea)
            lineas_a_eliminar.append({"ID": pd.get("ID")})

        # Crear todas las líneas en una sola query
        if lineas_objs:
            Lineaspedido.objects.bulk_create(lineas_objs)

            # --- Re-consultar para obtener PKs y serializar ---
            # Subqueries para la serialización optimizada
            mesa_abierta_sq = Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id'))
            id_mesa_sq = mesa_abierta_sq.values('mesa_id')[:1]
            nom_mesa_sq = mesa_abierta_sq.values('mesa__nombre')[:1]
            zona_id_sq = Mesaszona.objects.filter(
                mesa__mesasabiertas__infmesa_id=OuterRef('infmesa_id')
            ).values('zona_id')[:1]

            # Consultamos las líneas recién creadas, ahora con PKs
            lineas_creadas = Lineaspedido.objects.filter(pedido=pedido).select_related(
                'pedido', 'tecla__familia__receptor'
            ).annotate(
                servido_count=Count('servidos'),
                id_mesa=Subquery(id_mesa_sq),
                nom_mesa=Subquery(nom_mesa_sq),
                id_zona=Subquery(zona_id_sq)
            )
            
            # Serializamos y comunicamos la inserción
            lineas_serializadas = [l.serialize(prefetched=True) for l in lineas_creadas]
            if lineas_serializadas:
                comunicar_cambios_devices("insert", "lineaspedido", lineas_serializadas)

        if lineas_a_eliminar:
            comunicar_cambios_devices("rm", "lineaspedido", lineas_a_eliminar)
        

       
        # --- Actualización y comunicación (con la corrección) ---
        infmesa.numcopias = 0
        infmesa.save()
        infmesa.componer_articulos()
        infmesa.unir_en_grupos()

        return pedido


    class Meta:
        db_table = 'pedidos'
        ordering = ['-id']

ESTADO_CHOICES=[
    ("A", "Anulado"),
    ("P", "Pedido activo"),
    ("R", "Regalo"),
    ("C", "Linea cobrada"),
    ("M", "Pertenece a promocion o grupo")
]
class Lineaspedido(BaseModels):
    pedido = models.ForeignKey('Pedidos',  on_delete=models.CASCADE, db_column='IDPedido')  # Field name made lowercase.
    infmesa = models.ForeignKey('Infmesa', on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    idart = models.IntegerField(db_column='IDArt')  # Field name made lowercase.
    estado = models.CharField(db_column='Estado', choices=ESTADO_CHOICES,  max_length=1, default="P")  # Field name made lowercase.
    precio = models.DecimalField(db_column='Precio', max_digits=6, decimal_places=2)  # Field name made lowercase.
    descripcion = models.CharField(db_column='Descripcion', default=None,  max_length=400, null=True)  # Field name made lowercase.
    tecla = models.ForeignKey('Teclas', on_delete=models.SET_NULL, null=True)  # Field name made lowercase.
    es_compuesta = models.BooleanField("Grupo o simple", default=False)
    cantidad = models.IntegerField("Cantidad de articulos que lo compone", default=0)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
    id_local = models.BigIntegerField(db_column='IDLocal', null=True)  # Cambiado a BigIntegerField para valores grandes
   


          
    def modifiar_composicion(self):
        mesa_a = Mesasabiertas.objects.filter(infmesa=self.infmesa).first()
        if self.estado == "R":
            self.lrToP(mesa_a)
            for l in self.infmesa.lineaspedido_set.filter(es_compuesta=True):
                composicion = l.tecla.familia.compToList()
                if self.tecla.familia.nombre in composicion:
                    l.es_compuesta = False
                    l.cantidad -= 1
                    l.save()
                    comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                    break

        elif self.es_compuesta:
            composicion = self.tecla.familia.compToList()
            if (len(composicion) > 0):
                for l in self.infmesa.lineaspedido_set.filter(estado="R", tecla__familia__nombre__in=composicion).order_by('-id'):
                    if self.cantidad == 0:
                        break
                    
                    l.lrToP(mesa_a)
                    comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                    self.cantidad -= 1

                self.es_compuesta = False
                self.save()
            comunicar_cambios_devices("md", "lineaspedido", self.serialize())

          

    def lrToP(self, mesa_a):
        
        tarifa = 1
        if mesa_a:
            mz = Mesaszona.objects.filter(mesa__pk=mesa_a.mesa_id).first()
            if mz:
                tarifa = mz.zona.tarifa
            
        self.precio = self.tecla.p1 if tarifa == 1 else self.tecla.p2
        self.estado = "P"
        self.save()

     

    @classmethod
    def compare_regs(cls, regs):
       
        result = []
        client_ids = {int(r.get('id') or r.get('ID')) for r in regs if r.get('id') or r.get('ID')}
 
        mesas_abiertas_info = {ma.mesa_id: ma.infmesa_id for ma in Mesasabiertas.objects.all()}
        
        for r in regs:
            client_id = int(r.get('id') or r.get('ID'))
            id_mesa = int(r.get('IDMesa'))
            if id_mesa not in mesas_abiertas_info:
                print(f"Lineaspedido ID: {client_id} - Mesa ID: {id_mesa} not found in open tables. Marking for removal."   )
                result.append({'tb': cls.__name__.lower(), 'op': 'rm', 'obj': {'ID': int(client_id)}})

        # --- Subqueries CORREGIDAS ---
        # Para obtener id_mesa y nom_mesa (estas estaban bien)
        mesa_abierta_sq = Mesasabiertas.objects.filter(infmesa_id=OuterRef('infmesa_id'))
        id_mesa_sq = mesa_abierta_sq.values('mesa_id')[:1]
        nom_mesa_sq = mesa_abierta_sq.values('mesa__nombre')[:1]

        # Para obtener id_zona (ESTA ES LA VERSIÓN CORREGIDA Y SIMPLIFICADA)
        # Buscamos en Mesaszona directamente, enlazando hacia atrás hasta infmesa_id
        zona_id_sq = Mesaszona.objects.filter(
            mesa__mesasabiertas__infmesa_id=OuterRef('infmesa_id')
        ).values('zona_id')[:1]

        # --- Obtención y comparación de registros ---
        server_records_qs = cls.objects.filter(id__in=client_ids).select_related(
            'pedido', 'tecla__familia__receptor'
        ).annotate(
            servido_count=Count('servidos'),
            id_mesa=Subquery(id_mesa_sq),
            nom_mesa=Subquery(nom_mesa_sq),
            id_zona=Subquery(zona_id_sq)
        )
        server_records_dict = {rec.id: rec for rec in server_records_qs}

        for r in regs:
            client_id = int(r.get('id') or r.get('ID'))
            id_mesa_str = r.get('IDMesa', -1)
            if not id_mesa_str:
                id_mesa_str = -1
            
            id_mesa = int(id_mesa_str)
            if id_mesa not in mesas_abiertas_info:
                continue

            server_record = server_records_dict.get(client_id)
            if server_record:
                # Si el registro existe en el servidor pero pertenece a otra infmesa
                # (es decir, a otro ticket/UID) entonces no pertenece a la mesa
                # abierta actual del cliente y debemos indicarle que lo borre.
                infmesa_abierta_para_mesa = mesas_abiertas_info.get(id_mesa)
                if getattr(server_record, 'infmesa_id', None) != infmesa_abierta_para_mesa:
                    logger.debug(
                        f"Server record ID: {client_id} belongs to infmesa {getattr(server_record, 'infmesa_id', None)} "
                        f"not to open infmesa {infmesa_abierta_para_mesa} for mesa {id_mesa}. Marking for removal"
                    )
                    result.append({'tb': cls.__name__.lower(), 'op': 'rm', 'obj': {'ID': int(client_id)}})
                    continue
                # --- NUEVA CONDICIÓN DE BORRADO AÑADIDA AQUÍ ---
                # Si el registro en el servidor está Cobrado ('C') o Anulado ('A'),
                # le decimos al cliente que lo borre y pasamos al siguiente.
                if server_record.estado in ['C', 'A']:
                    logger.debug(f"Server record ID: {client_id} has state '{server_record.estado}'. Marking for removal from client.")
                    print(f"Lineaspedido ID: {client_id} - State: {server_record.estado} - marking for removal")
                    result.append({'tb': cls.__name__.lower(), 'op': 'rm', 'obj': {'ID': int(client_id)}})
                    continue  # Importante: Saltamos al siguiente registro

                # Si no se borra, entonces comparamos si ha cambiado
                if not cls.is_equals_optimized(r, server_record):
                    logger.debug(f"Difference found for Lineaspedido ID: {client_id}")
                    result.append({'tb': cls.__name__.lower(), 'op': 'md', 'obj': server_record.serialize(prefetched=True)})
            else:
                # Si el registro del cliente no se encuentra en el servidor, se marca para borrar
                logger.debug(f"Server record not found for Lineaspedido ID: {client_id} - marking for removal")
                print(f"Lineaspedido ID: {client_id} - not found on server - marking for removal")
                result.append({'tb': cls.__name__.lower(), 'op': 'rm', 'obj': {'ID': int(client_id)}})

    
        server_only_records = cls.objects.exclude(id__in=client_ids).exclude(Q(estado='C') | Q(estado='A')).filter(infmesa_id__in=mesas_abiertas_info.values())

        server_only_records = server_only_records.select_related(
            'pedido', 'tecla__familia__receptor'
        ).annotate(
            servido_count=Count('servidos'),
            id_mesa=Subquery(id_mesa_sq),
            nom_mesa=Subquery(nom_mesa_sq),
            id_zona=Subquery(zona_id_sq)
        )

        for rec in server_only_records:
            logger.debug(f"New server record found for insertion - Lineaspedido ID: {rec.id}")
            result.append({'tb': cls.__name__.lower(), 'op': 'insert', 'obj': rec.serialize(prefetched=True)})

        return result

    # AÑADE este nuevo método estático justo debajo de compare_regs, dentro de la clase Lineaspedido.
    @staticmethod
    def is_equals_optimized(client_reg, server_instance):
        """
        Versión optimizada de is_equals que no realiza queries.
        Asume que server_instance tiene 'servido_count' anotado.
        """
        # Usamos .get con un valor por defecto para evitar errores.
        client_servido = int(client_reg.get("servido", 0))
        server_servido = getattr(server_instance, 'servido_count', -1)
        
        if client_servido != server_servido:
            logger.debug(f"Servido count mismatch: Client={client_servido}, Server={server_servido}")
            return False
            
        client_estado = client_reg.get("Estado")
        server_estado = server_instance.estado
        
        if client_estado != server_estado:
            logger.debug(f"Estado mismatch: Client='{client_estado}', Server='{server_estado}'")
            return False
            
        return True

    # Reemplaza el método serialize entero en la clase Lineaspedido

    def serialize(self, prefetched=False):
       
        if prefetched:
            # Esto no cambia: usamos los datos que ya vienen precargados y es súper rápido
            id_mesa = self.id_mesa
            nom_mesa = self.nom_mesa
            id_zona = self.id_zona
            servido_count = self.servido_count
        else:
            # ESTE ES EL BLOQUE CORREGIDO PARA CUANDO SE LLAMA A SERIALIZE DIRECTAMENTE
            # Hacemos las consultas paso a paso, de forma correcta
            id_mesa, nom_mesa, id_zona = -1, "", -1
            
            # 1. Obtenemos la mesa abierta y la mesa física
            mesa_abierta = Mesasabiertas.objects.filter(infmesa_id=self.infmesa_id).select_related('mesa').first()
            
            if mesa_abierta:
                mesa = mesa_abierta.mesa
                id_mesa = mesa.id
                nom_mesa = mesa.nombre
                
                # 2. Desde la mesa, buscamos su zona de forma segura
                mesaszona_obj = Mesaszona.objects.filter(mesa=mesa).select_related('zona').first()
                if mesaszona_obj:
                    id_zona = mesaszona_obj.zona.id
            
            # 3. Contamos los servidos
            servido_count = self.servidos_set.count()

        # El resto del método para construir el diccionario no cambia
        receptor_pk = -1
        if self.tecla and hasattr(self.tecla, 'familia') and self.tecla.familia and hasattr(self.tecla.familia, 'receptor') and self.tecla.familia.receptor:
            receptor_pk = self.tecla.familia.receptor.pk

        return {
            'ID': self.pk,
            'IDPedido': self.pedido_id,
            'UID': self.infmesa_id,
            'IDArt': self.idart,
            'Estado': self.estado,
            'Precio': float(self.precio),
            'Descripcion': self.descripcion,
            'IDMesa': id_mesa,
            'nomMesa': nom_mesa,
            'IDZona': id_zona,
            'servido': servido_count,
            'descripcion_t': self.descripcion_t,
            'receptor': receptor_pk,
            'camarero': self.pedido.camarero_id,
        }
    
    @staticmethod
    @transaction.atomic
    def borrar_linea_pedido_by_ids(idm, ids, idc, motivo):
        if not ids:
            return -1
        
        # Obtener las líneas a borrar con select_for_update para bloqueo
        lineas_a_borrar = Lineaspedido.objects.select_for_update().filter(Q(id__in=ids) | Q(id_local__in=ids))
        
        if not lineas_a_borrar.exists():
            return -1
        
        # Asumimos que todas las líneas pertenecen a la misma mesa
        mesa_abierta = Mesasabiertas.objects.filter(mesa_id=idm).first()
        if not mesa_abierta:
            return -1

        uid = mesa_abierta.infmesa.pk
        ids_borrados = []
        
        for linea in lineas_a_borrar:
            ids_borrados.append(linea.id)
            
            if motivo != 'null' and motivo.strip() != '':
                Historialnulos.objects.create(
                    lineapedido_id=linea.id,
                    camarero_id=idc,
                    motivo=motivo,
                    hora=datetime.now().strftime("%H:%M")
                )
                linea.estado = 'A'
                linea.save()  # Se necesita save para disparar la lógica post-save
            else:
                linea.delete()  # Esto también dispara la señal post-delete

            linea.modifiar_composicion()
        
        # Componer artículos una sola vez al final
        mesa_abierta.infmesa.componer_articulos()

        if not Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).exists():
            mesa_abierta.delete()
            return 0
            
        return Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).count()
    

    # Reemplaza tu método borrar_linea_pedido
    @staticmethod
    @transaction.atomic # <-- Asegura la atomicidad de la operación
    def borrar_linea_pedido(idm, p, idArt, can, idc, motivo, s, n): 
        mesa_abierta = Mesasabiertas.objects.filter(mesa__pk=idm).select_related('infmesa').first()
        if not mesa_abierta:
            return -1
        
        uid = mesa_abierta.infmesa.pk
        # Usamos select_for_update() para bloquear las filas hasta que la transacción termine
        lineas_a_borrar = Lineaspedido.objects.select_for_update().filter(
            infmesa__pk=uid, idart=idArt, estado=s, precio=p, descripcion=n
        )[:can]

        ids_borrados = []
        precios_borrados = []

        for linea in lineas_a_borrar:
            ids_borrados.append(linea.id)
            precios_borrados.append(float(linea.precio))
            
            if motivo != 'null':
                Historialnulos.objects.create(
                    lineapedido_id=linea.id,
                    camarero_id=idc,
                    motivo=motivo,
                    hora=datetime.now().strftime("%H:%M")
                )
                linea.estado = 'A'
                linea.save() # Se necesita save para disparar la lógica post-save
            else:
                linea.delete() # Esto también dispara la señal post-delete

            linea.modifiar_composicion()
        
       
        # Componer artículos una sola vez al final
        mesa_abierta.infmesa.componer_articulos()

        if not Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).exists():
            mesa_abierta.delete()
            return 0
            
        return Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).count()


    def save(self, *args, **kwargs):
        is_new = self.pk is None
        super().save(*args, **kwargs)
        
        # Si el estado es 'C' (Cobrada), se envía como eliminación
        if self.estado in ['C', 'A']:
            comunicar_cambios_devices("rm", "lineaspedido", {"ID": self.id})
        else:
            op = "insert" if is_new else "md"
            comunicar_cambios_devices(op, "lineaspedido", self.serialize())

    def delete(self, *args, **kwargs):
        # Obtener ID antes de borrar
        linea_id = self.id
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "lineaspedido", {"ID": linea_id})

    
    class Meta:
        db_table = 'lineaspedido'




class Servidos(BaseModels):
    linea = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE, db_column='IDLinea')  # Field name made lowercase.
    
    def save(self, *args, **kwargs):
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        # Comunicar cambios de la línea relacionada (afecta contador de servidos)
        comunicar_cambios_devices("md", "lineaspedido", self.linea.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener la línea relacionada antes de borrar
        linea_relacionada = self.linea
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar cambios de la línea relacionada (afecta contador de servidos)
        comunicar_cambios_devices("md", "lineaspedido", linea_relacionada.serialize())
 
    class Meta:
        db_table = 'servidos'
        ordering = ['-id']


