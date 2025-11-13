# @Author: Manuel Rodriguez <valle>
# @Date:   01-Jan-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-22T00:11:47+02:00
# @License: Apache license vesion 2.0
from __future__ import unicode_literals
from django.db import models
from .basemodels import BaseModels
from datetime import date
from comunicacion.tools import comunicar_cambios_devices
from gestion.tools.config_logs import logger_sync as logger

ICON_CHOICES = (
    ("bar", "Bar"),
    ("bocadillo", "Bocadillo"),
    ("carne", "Carne"),
    ("cocktel", "Cocktel"),
    ("copa_con_limon", "Copa con rodaja de limon"),
    ("copa_vino", "Copa de vino"),
    ("cubalibre", "Cubalibre"),
    ("donut", "Donut"),
    ("jarra_cerveza", "Jarra de cerveza"),
    ("llevar", "Icono para llevar"),
    ("magdalena", "Magdalena"),
    ("menu", "Menu"),
    ("pescado", "Pescado"),
    ("pincho", "Pincho"),
    ("pizza", "Pizza"),
    ("plato", "Plato humeante"),
    ("plato_combinado", "Plato combinado"),
    ("sopa", "Plato sopa"),
    ("sopa_cuchara", "Plato sopa con cuchara"),
    ("tarta", "Tarta"),
    ("taza_cafe", "Taza cafe"),
    ("aperitivo", "Plato doritos"),
)


class SeccionesCom(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=11)
    icono = models.CharField(db_column='Icono', max_length=15, choices=ICON_CHOICES, default="bar")

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Validación de descuento existente
        if hasattr(self, 'descuento'):
            if self.descuento and int(self.descuento) > 100:
                self.descuento = 100
            elif not self.descuento or int(self.descuento) < 0:
                self.descuento = 0
        
        # Guardar el objeto
        super(SeccionesCom, self).save(*args, **kwargs)
        
        # Comunicar cambios a devices
        if is_new:
            comunicar_cambios_devices("insert", "seccionescom", self.serialize())
        else:
            comunicar_cambios_devices("md", "seccionescom", self.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener ID antes de borrar
        seccion_id = self.id
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "seccionescom", {"id": seccion_id})

    @staticmethod
    def update_for_devices():
        a = []
        for obj in SeccionesCom.objects.all():
            a.append(obj.serialize())
        return a

  

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'secciones_com'



class Sugerencias(BaseModels):
    # La relación con Teclas sigue igual
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE, db_column='IDTecla', default=-1)
    sugerencia = models.CharField(db_column='Sugerencia', max_length=300)
    incremento = models.DecimalField(db_column="Incremento", max_digits=6, decimal_places=2, default=0.0)

   

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        # Comunicar cambios a devices
        if is_new:
            comunicar_cambios_devices("insert", "sugerencias", self.serialize())
        else:
            comunicar_cambios_devices("md", "sugerencias", self.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener ID antes de borrar
        sugerencia_id = self.id
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "sugerencias", {"id": sugerencia_id})

    def serialize(self):
        data = super().serialize()
        # Convertir incremento a float si existe
        if 'incremento' in data and data['incremento'] is not None:
            data['incremento'] = float(data['incremento'])
        return data
    

    class Meta:
        db_table = 'sugerencias'

TIPO_TECLA_CHOICE = [
    ("SP", "SIMPLE"),        # Artículo simple/vendible directamente
    ("CM", "COMPUESTA"),     # Artículo que requiere elegir subteclas  
]

class Teclas(BaseModels):
    nombre = models.CharField(db_column='Nombre', max_length=50)
    p1 = models.DecimalField(db_column='P1', max_digits=6, decimal_places=2, help_text="Precio  (Tarifa 1)", default=0.0) # Precio base para SP/CM
    p2 = models.DecimalField(db_column='P2', max_digits=6, decimal_places=2, help_text="Precio (Tarifa 2)", default=0.0) # Precio base para SP/CM
    familia = models.ForeignKey('Familias',  on_delete=models.CASCADE, db_column='IDFamilia')
    tag = models.CharField(db_column='Tag', max_length=100, default='', blank=True)
    descripcion_r = models.CharField("Descripción recepción", db_column='Descripcion_r', max_length=300, null=True, blank=True)
    descripcion_t = models.CharField("Descripción ticket", db_column='Descripcion_t', max_length=300, null=True, blank=True)
    tipo = models.CharField(max_length=2, choices=TIPO_TECLA_CHOICE, default="SP", help_text="Tipo de tecla: Simple o Compuesta")
    orden = models.IntegerField(db_column='Orden', default=0, help_text="Orden de la tecla dentro de su familia")

    # --- Nuevos Campos ---
    parent_tecla = models.ForeignKey(
        'self',
        on_delete=models.SET_NULL, # O models.SET_NULL si quieres mantener modificadores huérfanos
        null=True,
        blank=True,
        related_name='subteclas', # Nombre para acceder a los modificadores desde la tecla padre
        db_column='IDParentTecla',
        help_text="Tecla padre si esta tecla es un modificador (antes Subtecla)"
    )
   
    # --- Fin Nuevos Campos ---

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        # Comunicar cambios a devices
        if is_new:
            comunicar_cambios_devices("insert", "teclas", self.serialize())
        else:
            comunicar_cambios_devices("md", "teclas", self.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener ID antes de borrar
        tecla_id = self.id
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar eliminación a devices
        comunicar_cambios_devices("rm", "teclas", {"id": tecla_id})

    

    # Reemplaza el método serialize que ya tienes en la clase Teclas por este

    def serialize(self, prefetched=False):
        row = {}

        row["ID"] = self.pk

        row["nombre"] = self.nombre
        row["p1"] = float(self.p1)
        row["p2"] = float(self.p2)
        row["IDFamilia"] = self.familia_id
        row["nombreFam"] = self.familia.nombre
        row["tag"] = self.tag
        row["tipo"] = self.tipo
        row["IDParentTecla"] = self.parent_tecla_id
        row["Precio"] = float(self.p1)

        if self.parent_tecla_id:
            parent_nombre = self.parent_tecla.nombre
            row["descripcion_r"] = self.descripcion_r or f"{parent_nombre} {self.nombre}"
            row["descripcion_t"] = self.descripcion_t or parent_nombre
        else:
            row["descripcion_r"] = self.descripcion_r or self.nombre
            row["descripcion_t"] = self.descripcion_t or self.nombre

        if prefetched:
            teclasseccion_list = list(self.teclasecciones.all()) # <-- CAMBIO AQUÍ
            row['RGB'] = teclasseccion_list[0].seccion.rgb if teclasseccion_list else "255,0,0"
            row['IDSeccion'] = teclasseccion_list[0].seccion.id if teclasseccion_list else -1
            
            teclascom_list = list(self.teclascomanda.all())
            row["IDSeccionCom"] = teclascom_list[0].seccion.id if teclascom_list else -1
            row["orden"] = teclascom_list[0].orden if teclascom_list else self.orden
            
            row["hay_existencias"] = 0 if self.agotado_hoy else 1
        else:
            teclasseccion = self.teclasecciones.first() # <-- CAMBIO AQUÍ
            row['RGB'] = teclasseccion.seccion.rgb if teclasseccion else "255,0,0"
            row['IDSeccion'] = teclasseccion.seccion.id if teclasseccion else -1
            
            seccioncom = self.teclascomanda.first()
            row["IDSeccionCom"] = seccioncom.seccion.id if seccioncom else -1
            row["orden"] = seccioncom.orden if seccioncom else self.orden

            hoy_str = date.today().strftime("%Y/%m/%d")
            row["hay_existencias"] = 1 if not TeclasAgotadas.objects.filter(tecla=self, fecha=hoy_str).exists() else 0
    
        return row


    # Pega estos dos métodos dentro de la clase Teclas

   
    @classmethod
    def compare_regs(cls, regs):
        from django.db.models import Exists, OuterRef
        
        result = []
        client_ids = {int(r.get('ID') or r.get('id')) for r in regs if r.get('ID') or r.get('id')}
        
        # --- Consulta Optimizada con el related_name corregido ---
        hoy_str = date.today().strftime("%Y/%m/%d")
        existencias_sq = TeclasAgotadas.objects.filter(tecla_id=OuterRef('pk'), fecha=hoy_str)

        server_records_qs = cls.objects.filter(id__in=client_ids).select_related(
            'familia', 'parent_tecla'
        ).prefetch_related(
            'teclasecciones__seccion',  # <-- CAMBIO AQUÍ
            'teclascomanda__seccion'
        ).annotate(
            agotado_hoy=Exists(existencias_sq)
        )
        
        server_records_dict = {rec.id: rec for rec in server_records_qs}

        # El resto de la lógica no necesita cambios...
        for r in regs:
            client_id = int(r.get('ID') or r.get('id'))
            server_record = server_records_dict.get(client_id)
            if server_record:
                if not cls.is_equals_teclas(r, server_record):
                    logger.debug("Not equal in Teclas")
                    logger.debug(f"Client reg: {r}")
                    logger.debug(f"Server reg: {server_record.serialize(prefetched=True)}")
                    logger.debug(f"Difference found for Tecla ID: {client_id}")
                    result.append({'tb': cls.__name__.lower(), 'op': 'md', 'obj': server_record.serialize(prefetched=True)})
            else:
                logger.debug(f"Server record not found for Tecla ID: {client_id} - marking for removal")

        ids_to_remove = client_ids - set(server_records_dict.keys())
        for record_id in ids_to_remove:
            result.append({'tb': cls.__name__.lower(), 'op': 'rm', 'obj': {'id': record_id}})

        server_only_records = cls.objects.exclude(id__in=client_ids).select_related(
            'familia', 'parent_tecla'
        ).prefetch_related(
            'teclasecciones__seccion',  # <-- CAMBIO AQUÍ
            'teclascomanda__seccion'
        ).annotate(
            agotado_hoy=Exists(existencias_sq)
        )
        
        for rec in server_only_records:
            logger.debug(f"New server record found for insertion - Tecla ID: {rec.id}")
            result.append({'tb': cls.__name__.lower(), 'op': 'insert', 'obj': rec.serialize(prefetched=True)})
            
        return result


    @staticmethod
    def is_equals_teclas(client_reg, server_instance):
        """
        Compara un registro de tecla comparando todas las claves del cliente con el servidor.
        Convierte tanto claves como valores a minúsculas para la comparación.
        """
        # --- NORMALIZACIÓN ---
        # Convertimos todas las claves del cliente a minúsculas para evitar problemas
        client = {k.lower(): v for k, v in client_reg.items()}
        
        # Obtener los datos serializados del servidor para comparar
        server_data = server_instance.serialize(prefetched=True)
        server = {k.lower(): v for k, v in server_data.items()}

        # --- COMPARACIONES DINÁMICAS ---
        # Comparar todas las claves que vienen del cliente
        for key, client_value in client.items():
            # Excluir completamente el campo "precio" de cualquier comparación
            if key == 'precio':
                continue
                
            server_value = server.get(key)
            
            # Comparación especial para precios (p1, p2) - usar 2 decimales
            if key in ['p1', 'p2']:
                try:
                    client_price = round(float(client_value or 0), 2)
                    server_price = round(float(server_value or 0), 2)
                    
                    if client_price != server_price:
                        return False
                except (ValueError, TypeError):
                    return False
            # Comparación especial para IDParentTecla (0 del cliente = None/vacío del servidor)
            elif key == 'idparenttecla':
                try:
                    client_parent = int(client_value or 0)
                    server_parent = int(server_value or 0) if server_value not in [None, '', 'none'] else 0
                    
                    if client_parent != server_parent:
                        return False
                except (ValueError, TypeError):
                    return False
            else:
                # Convertir valores a string y luego a minúsculas para comparación normal
                client_str = str(client_value).lower() if client_value is not None else ''
                server_str = str(server_value).lower() if server_value is not None else ''
                
                if client_str != server_str:
                    return False

        # Si todo ha ido bien, son iguales
        return True

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        # Podrías diferenciar visualmente los modificadores
        if self.parent_tecla:
            return f"{self.parent_tecla.nombre} -> {self.nombre}"
        return self.nombre

    class Meta:
        db_table = 'teclas'
        ordering = ['id'] # Ordenar por familia, luego orden, luego nombre


class Teclascom(BaseModels):
    # Sin cambios aquí, sigue relacionando Teclas con SeccionesCom
    tecla = models.ForeignKey(Teclas,  on_delete=models.SET_NULL, db_column='IDTecla', null=True, related_name="teclascomanda")
    seccion = models.ForeignKey(SeccionesCom,  on_delete=models.CASCADE, db_column='IDSeccion')
    orden = models.IntegerField(db_column='Orden', default=0)

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        # Comunicar cambios de la tecla relacionada (afecta orden/sección)
        if self.tecla:  # Verificar que tecla no sea None
            if is_new:
                comunicar_cambios_devices("md", "teclas", self.tecla.serialize())
            else:
                comunicar_cambios_devices("md", "teclas", self.tecla.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener la tecla antes de borrar (si existe)
        tecla_relacionada = self.tecla
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar cambios de la tecla relacionada (afecta orden/sección)
        if tecla_relacionada:  # Verificar que tecla no sea None
            comunicar_cambios_devices("md", "teclas", tecla_relacionada.serialize())

    class Meta:
        db_table = 'teclascom'
        ordering = ['-orden']

class Teclaseccion(models.Model):
    # Sin cambios aquí, sigue relacionando Teclas con Secciones (asumiendo que Secciones existe)
    seccion = models.ForeignKey('Secciones',  on_delete=models.CASCADE, db_column='IDSeccion')
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla', related_name="teclasecciones")

    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        # Comunicar cambios de la tecla relacionada (afecta sección)
        if is_new:
            comunicar_cambios_devices("md", "teclas", self.tecla.serialize())
        else:
            comunicar_cambios_devices("md", "teclas", self.tecla.serialize())
    
    def delete(self, *args, **kwargs):
        # Obtener la tecla antes de borrar
        tecla_relacionada = self.tecla
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar cambios de la tecla relacionada (afecta sección)
        comunicar_cambios_devices("md", "teclas", tecla_relacionada.serialize())

    class Meta:
        db_table = 'teclaseccion'


class TeclasAgotadas(BaseModels):
    tecla = models.ForeignKey(Teclas, on_delete=models.CASCADE, db_column='IDTecla')
    fecha = models.CharField(max_length=10, db_column='Fecha')
    
    def save(self, *args, **kwargs):
        # Determinar si es una inserción o actualización
        is_new = self.pk is None
        
        # Guardar el objeto
        super().save(*args, **kwargs)
        
        # Comunicar cambios de la tecla relacionada (afecta existencias)
        if is_new:
            comunicar_cambios_devices("md", "teclas", self.tecla.serialize())
        else:
            comunicar_cambios_devices("md", "teclas", self.tecla.serialize())
     
   
    def delete(self, *args, **kwargs):
        # Obtener la tecla antes de borrar
        tecla_relacionada = self.tecla
        
        # Borrar el objeto
        super().delete(*args, **kwargs)
        
        # Comunicar cambios de la tecla relacionada (afecta sección)
        comunicar_cambios_devices("md", "teclas", tecla_relacionada.serialize())

   
    class Meta:
        db_table = 'teclasagotadas'
      
    def __str__(self):
        return f"Tecla {self.tecla.nombre} - {self.fecha}"