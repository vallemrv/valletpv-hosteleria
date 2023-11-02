from django.db import models
from valle_tpv.models import (Infmesa, Mesas, Historialnulos, Mesasabiertas)
from valle_tpv.tools.ws import comunicar_cambios_devices
from datetime import datetime
from uuid import uuid4

class Servidos(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True) 
    linea = models.ForeignKey('Lineaspedido',  on_delete=models.CASCADE, db_column='IDLinea') 

    class Meta:
        db_table = 'servidos'
        ordering = ['-id']


class Pedidos(models.Model):
    id = models.AutoField( primary_key=True) 
    hora = models.CharField( max_length=5) 
    infmesa = models.ForeignKey('Infmesa',  on_delete=models.CASCADE, db_column='UID') 
    camarero_id = models.IntegerField() 
    uid_device = models.CharField(max_length=150, default="")
    
   
    @staticmethod
    def agregar_nuevas_lineas(idm, idc, lineas, uid_device):
       
        #Esto era para que no se repitieran los pedidos
        p = Pedidos.objects.filter(uid_device=uid_device).first()
        if p: return None
        
        mesaabierta = Mesasabiertas.objects.filter(mesa__pk=idm).first()
        
        if not mesaabierta:
            infmesa = Infmesa()
            infmesa.camarero_id = idc
            infmesa.hora = datetime.now().strftime("%H:%M")
            infmesa.fecha = datetime.now() 
            infmesa.uid = idm + '-' + str(uuid4())
            infmesa.save()
            
            mesaabierta = Mesasabiertas()
            mesaabierta.mesa_id = idm
            mesaabierta.infmesa_id = infmesa.pk
            mesaabierta.save()
           

        pedido = Pedidos()
        pedido.infmesa_id = mesaabierta.infmesa.pk
        pedido.hora = datetime.now().strftime("%H:%M")
        pedido.camarero_id = idc
        pedido.uid_device = uid_device
        pedido.save()

        for pd in lineas:
            linea = Lineaspedido()
            linea.infmesa_id = mesaabierta.infmesa.pk
            linea.pedido_id = pedido.pk
            linea.descripcion = pd["descripcion"]
            linea.descripcion_t = pd["descripcionT"]
            linea.precio = pd["precio"]
            linea.estado =  'P'
            linea.tecla_id = int(pd["teclaId"]) if "teclaId" in pd else None
            linea.save()
               
                
        
        pedido.infmesa.numcopias = 0
        pedido.infmesa.save()   
        pedido.infmesa.componer_articulos()
        pedido.infmesa.unir_en_grupos()
        comunicar_cambios_devices("update", "mesas", [mesaabierta.mesa.serialize()])
           
        lineas_new = []
        for l in pedido.lineaspedido_set.all():
            lineas_new.append(l.serialize())

        comunicar_cambios_devices("create", "lineaspedido", lineas_new)


        ids = [l["id"] for l in lineas]
        #delete array de lineas, solo los ids
        comunicar_cambios_devices("delete", "lineaspedido", ids)
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


class Lineaspedido(models.Model):
    id = models.AutoField(primary_key=True) 
    pedido = models.ForeignKey('Pedidos',  on_delete=models.CASCADE) 
    infmesa = models.ForeignKey(Infmesa, on_delete=models.CASCADE, db_column='UID') 
    estado = models.CharField( choices=ESTADO_CHOICES,  max_length=1, default="P") 
    precio = models.DecimalField( max_digits=6, decimal_places=2) 
    descripcion = models.CharField( default=None,  max_length=400, null=True) 
    tecla = models.ForeignKey('Teclas', on_delete=models.SET_NULL, null=True) 
    es_compuesta = models.BooleanField("Grupo o simple", default=False)
    can_composicion = models.IntegerField("Cantidad de articulos que lo compone", default=0)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
     
    
    def modifiar_composicion(self):
        mesa_a = Mesasabiertas.objects.filter(infmesa=self.infmesa).first()
        if self.estado == "R":
            self.lrToP(mesa_a)
            for l in self.infmesa.lineaspedido_set.filter(es_compuesta=True):
                composicion = l.tecla.familia.compToList()
                if self.tecla.familia.nombre in composicion:
                    l.es_compuesta = False
                    l.can_composicion -= 1
                    l.save()
                    comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                    break;

        elif self.es_compuesta:
            composicion = self.tecla.familia.compToList()
            if (len(composicion) > 0):
                for l in self.infmesa.lineaspedido_set.filter(estado="R", tecla__familia__nombre__in=composicion).order_by('-id'):
                    if self.can_composicion == 0:
                        break;
                    
                    l.lrToP(mesa_a)
                    comunicar_cambios_devices("md", "lineaspedido", l.serialize())
                    self.can_composicion -= 1

                self.es_compuesta = False
                self.save()
            comunicar_cambios_devices("md", "lineaspedido", self.serialize())

          

    def lrToP(self, mesa_a):
        tarifa = 1
        if mesa_a:
            mz = mesa_a.mesa.zona
            if mz:
                tarifa = mz.zona.tarifa
            
        self.precio = self.tecla.p1 if tarifa == 1 else self.tecla.p2
        self.estado = "P"
        self.save()


    def serialize(self):
        mesa = Mesasabiertas.objects.filter(infmesa__pk=self.infmesa.pk).first()
        if mesa:
            mesa = mesa.mesa
        else:
            split = self.infmesa.pk.split("-")
            mesa = Mesas.objects.filter(id=split[0]).first()
        obj = {
            'id': self.pk,
            'pedidoId': self.pedido_id,
            'uid': self.infmesa.pk,
            'teclaId': self.tecla.pk if self.tecla else -1,
            'estado': self.estado,
            'precio': float(self.precio),
            'descripcion': self.descripcion,
            'mesaId': mesa.pk if mesa else -1,
            'nomMesa': mesa.nombre if mesa else "",
            'zonaId': mesa.zona.pk if mesa else -1,
            'servido': Servidos.objects.filter(linea__pk=self.pk).count()>0,
            'descripcionT': self.descripcion_t,
            'receptorId': self.tecla.familia.receptor.pk if self.tecla else -1,
            'camareroId': self.pedido.camarero_id,
            }
        
        return obj
            

    def borrar_linea_pedido(ids, idc, motivo, idm):
        deleted = []
        for id in ids:
            reg = Lineaspedido.objects.filter(pk=id)
    
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

            deleted.append({"id": id})
                
        r.modifiar_composicion()
        r.infmesa.componer_articulos()
            
        comunicar_cambios_devices("delete", "lineaspedido", deleted)
                

        mesaabierta = Mesasabiertas.objects.filter(id=idm).first()
        if mesaabierta:
            for m in Mesasabiertas.objects.filter(infmesa__uid=mesaabierta.infmesa.uid):
                comunicar_cambios_devices("update", "mesas", [m.mesa.serialize()])
                m.delete()
               


    class Meta:
        db_table = 'lineaspedido'

