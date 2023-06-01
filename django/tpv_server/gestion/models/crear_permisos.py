from django.contrib.auth.models import Permission
from django.contrib.contenttypes.models import ContentType
from models import Camareros

# Asegúrate de tener el modelo correcto para tu contenido
content_type = ContentType.objects.get_for_model(Camareros)

# Crea tus permisos
PERMISOS_CHOICES = [
    "imprimir_factura", 
    "abrir_cajon", 
    "cobrar_ticket", 
    "borrar_linea", 
    "borrar_mesa"
]

for permiso in PERMISOS_CHOICES:
    Permission.objects.create(
        codename=permiso,
        name=f'Can {permiso.replace("_", " ")}',
        content_type=content_type,
    )
