from django.db import models
from .historiales import Historialnulos
from .basemodels import BaseModels, Sync
from comunicacion.tools import comunicar_cambios_devices
from datetime import datetime
from uuid import uuid4

class Pedidos(BaseModels):
    hora = models.CharField(db_column='Hora', max_length=5)  # Field name made lowercase.
    infmesa = models.ForeignKey('Infmesa',  on_delete=models.CASCADE, db_column='UID')  # Field name made lowercase.
    camarero_id = models.IntegerField(db_column='IDCam')  # Field name made lowercase.
    uid_device = models.CharField(max_length=150, default="589274e0-4ae3-4008-a00b-ec7a19d74d01")
    
   
    @staticmethod
    def agregar_nuevas_lineas(idm, idc, lineas, uid_device):
        from .mesasabiertas import Mesasabiertas
        from .infmesa import Infmesa
      

        p = Pedidos.objects.filter(uid_device=uid_device).first()
        if p: return None
        
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        
        if not mesa:
            infmesa = Infmesa()
            infmesa.camarero_id = idc
            infmesa.hora = datetime.now().strftime("%H:%M")
            infmesa.fecha = datetime.now().strftime("%Y/%m/%d")
            infmesa.id = idm + '-' + str(uuid4())
            infmesa.save()
            
            mesa = Mesasabiertas()
            mesa.mesa_id = idm
            mesa.infmesa_id = infmesa.pk
            mesa.save()
            Sync.actualizar("mesasabiertas")

        pedido = Pedidos()
        pedido.infmesa_id = mesa.infmesa.pk
        pedido.hora = datetime.now().strftime("%H:%M")
        pedido.camarero_id = idc
        pedido.uid_device = uid_device
        pedido.save()

        for pd in lineas:
            can = int(pd["Can"])
            for i in range(0, can):
                linea = Lineaspedido()
                linea.infmesa_id = mesa.infmesa.pk
                linea.idart = pd["IDArt"] if "IDArt" in pd else pd["ID"]
                linea.pedido_id = pedido.pk
                linea.descripcion = pd["Descripcion"]
                linea.descripcion_t = pd["descripcion_t"]
                linea.precio = pd["Precio"]
                linea.estado =  'P'
                linea.tecla_id = linea.idart if int(linea.idart) > 0 else None
                linea.save()
               
                
        
        pedido.infmesa.numcopias = 0
        pedido.infmesa.save()   
        pedido.infmesa.componer_articulos()
        pedido.infmesa.unir_en_grupos()
        comunicar_cambios_devices("md", "mesasabiertas", mesa.serialize())
           
        lineas = []
        for l in pedido.lineaspedido_set.all():
            lineas.append(l.serialize())

        comunicar_cambios_devices("insert", "lineaspedido", lineas)

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
   
    @staticmethod
    def update_for_devices():
        from .mesasabiertas import Mesasabiertas
     
        mesas = Mesasabiertas.objects.all()
        lineas = []
        for m in mesas:
            lineas = [*lineas, *m.get_lineaspedido()]
                
        return lineas
    
    @staticmethod
    def is_equals(self, linea):
        equal = True
        if int(linea["servido"]) != Servidos.objects.filter(linea__pk=self["ID"]).count() :
            return False
        if self["Estado"] != linea["Estado"]:
            return False
         
        return equal


    def modifiar_composicion(self):
        from .mesasabiertas import Mesasabiertas
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
                    break;

        elif self.es_compuesta:
            composicion = self.tecla.familia.compToList()
            if (len(composicion) > 0):
                for l in self.infmesa.lineaspedido_set.filter(estado="R", tecla__familia__nombre__in=composicion).order_by('-id'):
                    if self.cantidad == 0:
                        break;
                    
                    l.lrToP(mesa_a)
                    comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                    self.cantidad -= 1

                self.es_compuesta = False
                self.save()
            comunicar_cambios_devices("md", "lineaspedido", self.serialize())

          

    def lrToP(self, mesa_a):
        from .mesas import Mesaszona
        
        tarifa = 1
        if mesa_a:
            mz = Mesaszona.objects.filter(mesa__pk=mesa_a.mesa_id).first()
            if mz:
                tarifa = mz.zona.tarifa
            
        self.precio = self.tecla.p1 if tarifa == 1 else self.tecla.p2
        self.estado = "P"
        self.save()

    @classmethod
    def get_normalization_rules(cls):
        """
        Reglas personalizadas para la tabla 'Camareros'.
        """
        return {
            'precio': {'type': 'float'},
        }
    
    @classmethod
    def compare_regs(cls, regs):
        from gestion.models.mesasabiertas import Mesasabiertas
        """
        Compara los datos del cliente con los datos del servidor buscando por ID directamente en la base de datos
        y asegurando que el idmesa esté en mesas abiertas.
        """
        result = []
        ids_procesados = []

        # Recorrer los registros recibidos del cliente
        for r in regs:
            key_id = next((k for k in ['id', 'ID'] if k in r), None)
            client_id = r.get(key_id)
            idmesa = r.get('IDMesa')  # Asumimos que 'idmesa' está presente en el registro
            
            if client_id and idmesa:
                try:
                    # Verificar si la mesa está abierta en Mesasabiertas
                    mesa_abierta = Mesasabiertas.objects.filter(mesa__id=idmesa).exists()

                    if mesa_abierta:
                        # Buscar el registro en cls que esté vinculado a esa mesa abierta
                        try:
                            server_record = cls.objects.get(id=client_id).serialize()
                        except cls.DoesNotExist:
                            server_record = None
                        except Exception as e:
                            # Manejar otros posibles errores de la base de datos
                            print(f"Error al consultar la base de datos: {e}")
                            continue

                        if server_record:
                            # Si el registro existe y está vinculado a una mesa abierta, normalizar y comparar
                            if not cls.normalize_and_compare(r, server_record):
                                result.append({
                                    'tb': cls.__name__.lower(),
                                    'op': 'md',  # Operación de modificación
                                    'obj': server_record
                                })
                        else:
                            # Si no existe en la base de datos, el registro debe ser eliminado
                            result.append({
                                'tb': cls.__name__.lower(),
                                'op': 'rm',  # Operación de eliminación
                                'obj': {'id': client_id}
                            })
                    else:
                        # Si no está vinculado a una mesa abierta, eliminar
                        result.append({
                            'tb': cls.__name__.lower(),
                            'op': 'rm',  # Operación de eliminación
                            'obj': {'id': client_id}
                        })

                    # Agregar el ID a la lista de procesados
                    ids_procesados.append(client_id)

                except Exception as e:
                    print(f"Error al verificar mesa abierta: {e}")
                    continue

        
        # Buscar registros en la base de datos que no están en los registros del cliente
        # Excluir registros que:
        # - Tienen estado "C"
        # - Cuya idmesa no esté en mesas abiertas
        server_records = cls.objects.exclude(id__in=ids_procesados).exclude(
                estado='C'  # Excluir registros con estado "C"
            ).filter(
                infmesa__in=Mesasabiertas.objects.values_list('infmesa', flat=True)  # Solo los infmesa que están en mesas abiertas
            )

        # Procesar los registros que cumplen los criterios anteriores
        for server_record in server_records:
            result.append({
                'tb': cls.__name__.lower(),
                'op': 'insert',  # Operación de inserción
                'obj': server_record.serialize()
            })

        return result


    def serialize(self):
        from .mesasabiertas import Mesasabiertas
        from .mesas import Mesas
        
        mesa = Mesasabiertas.objects.filter(infmesa__pk=self.infmesa.pk).first()
        if mesa:
            mesa = mesa.mesa
        else:
            split = self.infmesa.pk.split("-")
            mesa = Mesas.objects.filter(id=split[0]).first()
        obj = {
            'ID': self.pk,
            'IDPedido': self.pedido_id,
            'UID': self.infmesa.pk,
            'IDArt': self.idart,
            'Estado': self.estado,
            'Precio': float(self.precio),
            'Descripcion': self.descripcion,
            'IDMesa': mesa.pk if mesa else -1,
            'nomMesa': mesa.nombre if mesa else "",
            'IDZona': mesa.mesaszona_set.all().first().zona.pk if mesa else -1,
            'servido': Servidos.objects.filter(linea__pk=self.pk).count(),
            'descripcion_t': self.descripcion_t,
            'receptor': self.tecla.familia.receptor.pk if self.tecla else -1,
            'camarero': self.pedido.camarero_id,
            }
        return obj
            

    def borrar_linea_pedido(idm, p, idArt, can, idc, motivo, s, n):
        from .mesasabiertas import Mesasabiertas
        
        num = -1
        mesa = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        if mesa:
            uid = mesa.infmesa.pk
            reg = Lineaspedido.objects.filter(infmesa__pk=uid, idart=idArt, estado=s, precio=p, descripcion=n)[:can]
    
            for r in reg:
                if motivo != 'null':
                    h = Historialnulos()
                    h.lineapedido_id = r.id
                    h.camarero_id = idc
                    h.motivo = motivo
                    h.hora = datetime.now().strftime("%H:%M")
                    h.save()
                    r.estado = 'A'
                    r.save()
                else:
                    r.delete()
                
                r.modifiar_composicion()
                r.infmesa.componer_articulos()
                comunicar_cambios_devices("rm", "lineaspedido", {"ID":r.id}, {"op": "borrado", "precio": float(r.precio)})
                

            num = Lineaspedido.objects.filter(estado='P', infmesa__pk=uid).count()
            if num <= 0:
                for m in Mesasabiertas.objects.filter(infmesa__pk=uid):
                    obj = m.serialize()
                    obj["abierta"] = 0
                    obj["num"] = 0
                    obj["nomMesa"] = m.mesa.nombre
                    comunicar_cambios_devices("md", "mesasabiertas", obj)
                    m.delete()
                Sync.actualizar(Mesasabiertas._meta.db_table)

        return num


    class Meta:
        db_table = 'lineaspedido'

class Servidos(BaseModels):
    linea = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE, db_column='IDLinea')  # Field name made lowercase.
 
    class Meta:
        db_table = 'servidos'
        ordering = ['-id']
