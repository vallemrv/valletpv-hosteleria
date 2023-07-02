from django.db import models
from django.forms.models import model_to_dict



class Receptores(models.Model):
    id = models.AutoField(primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(max_length=40)  # Field name made lowercase.
    activo = models.BooleanField( default=True)  # Field name made lowercase.
    descripcion = models.CharField(max_length=200, default="")  # Field name made lowercase.

    def __unicode__(self):
        return self.nombre +'  ' +self.descripcion

    def __str__(self):
        return self.nombre + ' ' +self.descripcion

    class Meta:
       db_table = 'receptores'


class Familias(models.Model):
    id = models.AutoField(primary_key=True)  # Field name made lowercase.
    nombre = models.CharField( max_length=40)  # Field name made lowercase.
    compuesto_por = models.CharField(max_length=150)  # Field name made lowercase.
    cantidad = models.IntegerField(default=0)  # Field name made lowercase.
    receptor = models.ForeignKey('Receptores',  on_delete=models.CASCADE)  # Field name made lowercase.
    color = models.CharField( max_length=11, default="#FFC0CB")  # Field name made lowercase.
    

    def serialize(self):
        data = model_to_dict(self)
        data["compuesto_por"] = self.compuesto_por.split(",")
        return data

    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        db_table = 'familias'
        ordering = ['-id']


class Secciones(models.Model):
    id = models.AutoField(primary_key=True)  # Field name made lowercase.
    nombre = models.CharField(max_length=50)  # Field name made lowercase.
    color = models.CharField( max_length=11, default="#FFC0CB")  # Field name made lowercase.
    orden = models.IntegerField( default=0)  # Field name made lowercase.
    icono = models.FileField(upload_to='iconos_secciones', blank=True, null=True)
    
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        tb_table = 'secciones'
        ordering = ['-orden']



class Sugerencias(models.Model):
    id = models.AutoField( primary_key=True) 
    tecla = models.ForeignKey('Teclas',  on_delete=models.CASCADE)  
    sugerencia = models.CharField(max_length=300) 
    orden = models.IntegerField(default=0, blank=True) 
    
    class Meta:
        tb_name = 'sugerencias'

class Teclas(models.Model):
    id = models.AutoField(primary_key=True)  # Field name made lowercase.
    nombre = models.CharField( max_length=50)  # Field name made lowercase.
    p1 = models.DecimalField( max_digits=6, decimal_places=2)  # Field name made lowercase.
    p2 = models.DecimalField(max_digits=6, decimal_places=2)  # Field name made lowercase.
    p2 = models.DecimalField( max_digits=6, decimal_places=2)  # Field name made lowercase.
    orden = models.IntegerField(default=0, blank=True)  # Field name made lowercase.
    familia = models.ForeignKey(Familias,  on_delete=models.CASCADE)  # Field name made lowercase.
    tag = models.CharField( max_length=100, default='', blank=True)  # Field name made lowercase.
    descripcion_r = models.CharField( max_length=300, null=True, blank=True)
    descripcion_t = models.CharField( max_length=300, null=True, blank=True)
    seccion = models.ForeignKey(Secciones,  on_delete=models.CASCADE, null=True, blank=True)  # Field name made lowercase.
    parent = models.ForeignKey('Teclas',  on_delete=models.CASCADE, null=True, blank=True)  # Field name made lowercase.

    def serialize(self):
        r = self
        row = model_to_dict(r)
        row["p1"] = float(r.p1)
        row["p2"] = float(r.p2)
        row["precio"] = float(r.p1) 
        row['rgb'] = r.familia.rgb 
        row["nombreFam"] = r.familia.nombre
        
        return row
   
    def __unicode__(self):
        return self.nombre

    def __str__(self):
        return self.nombre

    class Meta:
        tb_name = 'teclas'
        ordering = ['-orden']

class ComposicionTeclas(models.Model):
    id = models.AutoField(db_column='ID', primary_key=True)  # Field name made lowercase.
    tecla = models.ForeignKey(Teclas,  on_delete=models.CASCADE, db_column='IDTecla')  # Field name made lowercase.
    compuesto_por = models.CharField(max_length=300)  # Field name made lowercase.
    cantidad = models.IntegerField()  # Field name made lowercase.


    def serialize(self):
        r = self
        aux = model_to_dict(r)
        aux["nombre"] = r.tecla.nombre
        aux["compuesto_por"] = r.compuesto_por.split(",")
        return aux

    
    class Meta:
        tb_name = 'composicion_teclas'
        ordering = ['id']

class LineasCompuestas(models.Model):
    id = models.AutoField(primary_key=True)
    linea_principal = models.ForeignKey('Lineaspedido', models.CASCADE)
    linea_compuesta = models.IntegerField()
    composicion = models.ForeignKey(ComposicionTeclas, models.CASCADE)


    class Meta:
        tb_name = 'lineas_compuestas'
        ordering = ['id']