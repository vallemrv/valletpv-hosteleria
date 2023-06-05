from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.contrib.auth.models import Permission, Group
from django.db import models
from django.forms.models import model_to_dict
from comunicacion.tools import comunicar_cambios_devices


class CamareroManager(BaseUserManager):
    def create_camarero(self, nombre, apellidos, password=None,  **extra_fields):
        if not nombre or not apellidos:
            raise ValueError('Los campos Nombre y Apellidos son obligatorios')

        # Generar un user_name único y automático
        user_name = f'{nombre.lower()}_{apellidos.lower()}'
        user_name = user_name.replace(' ', '_')

        # Comprobar la unicidad del user_name
        if Camareros.objects.filter(user_name=user_name).exists():
            raise ValueError('El user_name generado ya existe, por favor, proporcione un Nombre y Apellidos únicos')

        camarero = self.model(nombre=nombre, apellidos=apellidos, user_name=user_name, **extra_fields)

        # Si no se proporciona una contraseña, asignar una contraseña aleatoria
        if password is None:
            camarero.password = "nuevo"
        else:
            camarero.set_password(password)
        camarero.save(using=self._db)
        
        return camarero



class Camareros(AbstractBaseUser, PermissionsMixin):
    id = models.AutoField(primary_key=True, db_column="ID")
    nombre = models.CharField(max_length=100, db_column="Nombre")
    apellidos = models.CharField(max_length=100, db_column="Apellidos")
    user_name = models.CharField(max_length=200)
    activo = models.BooleanField(default=False, db_column="Activo")
    autorizado = models.BooleanField(default=False, db_column="Autorizado")
    fecha_creacion = models.DateTimeField(auto_now_add=True)
    
    groups = models.ManyToManyField(
        Group,
        verbose_name=('groups'),
        blank=True,
        related_name="camarero_groups",
        related_query_name="camarero",
    )
    user_permissions = models.ManyToManyField(
        Permission,
        verbose_name=('user permissions'),
        blank=True,
        related_name="camarero_permissions",
        related_query_name="camarero",
    )


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
    

