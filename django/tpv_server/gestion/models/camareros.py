from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.contrib.auth.models import Permission
from django.db import models
from django.forms.models import model_to_dict
from comunicacion.tools import comunicar_cambios_devices
from gestion.models import Mesas
import json


class CamareroManager(BaseUserManager):
    def create_camarero(self, nombre, apellidos, password=None, **extra_fields):
        if not nombre or not apellidos:
            raise ValueError('Los campos nombre y apellidos son obligatorios')

        user_name = f'{nombre.lower()}_{apellidos.lower()}'
        user_name = user_name.replace(' ', '_')
        camarero = self.model(user_name=user_name, nombre=nombre, apellidos=apellidos, **extra_fields)
        
        camarero.set_password(password)
        camarero.save(using=self._db)
        
        return camarero



class Camareros(AbstractBaseUser, PermissionsMixin):
    id = models.AutoField(primary_key=True, db_column="ID")
    nombre = models.CharField(max_length=100, db_column="Nombre")
    apellidos = models.CharField(max_length=100, db_column="Apellidos")
    user_name = models.EmailField(unique=True)
    activo = models.BooleanField(default=False, db_column="Activo")
    autorizado = models.BooleanField(default=False, db_column="Autorizado")
    fecha_creacion = models.DateTimeField(auto_now_add=True)

    objects = CamareroManager()

    USERNAME_FIELD = 'user_name'
    REQUIRED_FIELDS = ['nombre', 'apellidos']

    class Meta:
        verbose_name = 'camarero'
        verbose_name_plural = 'camareros'
        db_table = 'camareros'
        ordering = ["apellidos"]

    @staticmethod
    def update_from_device(row):
        c = Camareros.objects.filter(id=row["ID"]).first()
        if c:
            c.autorizado = int(row["autorizado"])
            c.pass_field = row["pass_field"] if "pass_field" in row else row["Pass"]
            c.permisos = row["permisos"]
            c.save()
            comunicar_cambios_devices("md", "camareros", c.serialize())
       

    @staticmethod
    def update_for_devices():
        rows = Camareros.objects.all()
        tb = []
        for r in rows:
            tb.append(r.serialize())
        return tb


    @property
    def permisos_list(self):
        return [p.codename for p in self.user_permissions.all()] + [p.codename for p in Permission.objects.filter(group__user=self)]

    def asignar_permiso(self, permiso_codename):
        permiso_obj = Permission.objects.get(codename=permiso_codename)
        self.user_permissions.add(permiso_obj)

    def eliminar_permiso(self, permiso_codename):
        permiso_obj = Permission.objects.get(codename=permiso_codename)
        self.user_permissions.remove(permiso_obj)

    def serialize(self):
        data = model_to_dict(self)
        data['permisos'] = self.permisos_list
        return data
    

class PeticionesAutoria(models.Model):
    idautorizado = models.ForeignKey("Camareros", on_delete=models.CASCADE)
    accion = models.CharField(max_length=150)
    instrucciones = models.CharField(max_length=300)


    def serialize(self):
        inst = json.loads(self.instrucciones)
        if self.accion == "borrar_mesa":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            motivo = inst["motivo"]
            return {"tipo": "peticion", "idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje":camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar la mesa completa "
            + mesa.nombre +" por el motivo:  "+ motivo}
        elif self.accion == "borrar_linea":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            nombre = inst["Descripcion"]
            can = inst["can"]
            motivo = inst["motivo"]
            return {"tipo": "peticion", "idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje":camarero.nombre+ " "
            + camarero.apellidos+" solicita permiso para borrar " + can +" "+nombre + " de la mesa "
            + mesa.nombre +" por el motivo:  "+ motivo}
        elif self.accion == "informacion":
            return {"tipo": "informacion", "idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje": "Mensaje de "+inst["autor"]+": \n "+ inst["mensaje"]}
        elif self.accion == "cobrar_ticket":
            camarero = Camareros.objects.get(id=inst["idc"])
            mesa = Mesas.objects.get(id=inst["idm"])
            return {"tipo":"peticion","idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje": camarero.nombre + " "
            + camarero.apellidos+" solicita permiso para cobrar "+ mesa.nombre}
        elif self.accion == "abrir_cajon":
            camarero = Camareros.objects.get(id=inst["idc"])
            return {"tipo":"peticion","idautorizado":self.idautorizado.pk, "idpeticion": self.pk, "mensaje": camarero.nombre + " "
            + camarero.apellidos+" solicita permiso para abrir cajon" }


    class Meta:
        ordering = ['-id']

    