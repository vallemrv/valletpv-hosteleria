from django.db import models
from django.forms.models import model_to_dict

class Receptores(models.Model):
    id = models.AutoField(primary_key=True) 
    nombre = models.CharField(max_length=40) 
    activo = models.BooleanField( default=True) 
    descripcion = models.CharField(max_length=200, default="") 

    def __unicode__(self):
        return self.nombre +'  ' +self.descripcion

    def __str__(self):
        return self.nombre + ' ' +self.descripcion

    class Meta:
       db_table = 'receptores'


class Familias(models.Model):
    id = models.AutoField(primary_key=True) 
    nombre = models.CharField( max_length=40) 
    compuesto_por = models.CharField(max_length=150) 
    cantidad = models.IntegerField(default=0) 
    receptor = models.ForeignKey('Receptores',  on_delete=models.SET_NULL, null=True) 
    color = models.CharField( max_length=11, default="#FFC0CB") 
    
    @staticmethod
    def update_reg(data):
        familia = Familias.objects.get(pk=data["id"])
        if "nombre" in data:
           familia.nombre = data["nombre"]
        if "compuesto_por" in data:
            familia.compuesto_por = data["compuesto_por"].join(",") 
        if "cantidad" in data:
            familia.cantidad = data["cantidad"]
        if "receptor" in data:
            familia.receptor = Receptores.objects.get(pk=data["receptor"])
        if "color" in data:
            familia.color = data["color"]
        if "receptor_id" in data:
            familia.receptor_id = data["receptor_id"]

        familia.save()
        return familia


    def serialize(self):
        data = model_to_dict(self)
        data["compuesto_por"] = self.compuesto_por.split(",") if self.compuesto_por != "" else []
        return data

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'familias'
        ordering = ['-id']


class Secciones(models.Model):
    id = models.AutoField(primary_key=True) 
    nombre = models.CharField(max_length=50) 
    color = models.CharField( max_length=11, default="#FFC0CB") 
    orden = models.IntegerField( default=0) 
    icono = models.FileField(upload_to='iconos_secciones/', blank=True, null=True)
    
    
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre
    
    def serialize(self):
        data = model_to_dict(self)
        data["icono"] = {
            'url': self.icono.url if self.icono else "",
            'name': self.icono.name.replace("iconos_secciones/", "") if self.icono else "",
            'size': self.icono.size if self.icono else 0,
        }
        return data

    class Meta:
        db_table = 'secciones'
        ordering = ['-orden']



class Sugerencias(models.Model):
    id = models.AutoField( primary_key=True) 
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE)  
    sugerencia = models.CharField(max_length=300) 
    orden = models.IntegerField(default=0, blank=True) 

    def __unicode__(self):
        return self.sugerencia
    
    def __str__(self):
        return self.sugerencia
    
    class Meta:
        db_table = 'sugerencias'

class Teclas(models.Model):
    id = models.AutoField(primary_key=True) 
    nombre = models.CharField( max_length=50) 
    p1 = models.DecimalField( max_digits=6, decimal_places=2) 
    p2 = models.DecimalField(max_digits=6, decimal_places=2) 
    p2 = models.DecimalField( max_digits=6, decimal_places=2) 
    orden = models.IntegerField(default=0, blank=True) 
    familia = models.ForeignKey(Familias,  on_delete=models.SET_NULL, null=True, blank=True) 
    tag = models.CharField( max_length=100, default='', blank=True) 
    descripcion_r = models.CharField( max_length=300, null=True, blank=True)
    descripcion_t = models.CharField( max_length=300, null=True, blank=True)
    seccion = models.ForeignKey(Secciones,  on_delete=models.SET_NULL, null=True, blank=True) 
    parent = models.ForeignKey('Teclas',  on_delete=models.SET_NULL, null=True, blank=True) 

    def serialize(self):
        r = self
        row = model_to_dict(r)
        row["p1"] = float(r.p1)
        row["p2"] = float(r.p2)
        row["precio"] = float(r.p1) 
        row['color'] = r.familia.color  if r.familia else "#FFC0CB" 
        row["nombreFam"] = r.familia.nombre
        
        return row
   
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'teclas'
        ordering = ['-orden']

class ComposicionTeclas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True) 
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla') 
    compuesto_por = models.CharField(max_length=300) 
    cantidad = models.IntegerField() 


    def serialize(self):
        r = self
        aux = model_to_dict(r)
        aux["nombre"] = r.tecla.nombre
        aux["compuesto_por"] = r.compuesto_por.split(",")
        return aux

    
    class Meta:
        db_table = 'composicion_teclas'
        ordering = ['id']

class LineasCompuestas(models.Model):
    id = models.AutoField(primary_key=True)
    linea_principal = models.ForeignKey('Lineaspedido', models.CASCADE)
    linea_compuesta = models.IntegerField()
    composicion = models.ForeignKey(ComposicionTeclas, models.CASCADE)


    class Meta:
        db_table = 'lineas_compuestas'
        ordering = ['id']