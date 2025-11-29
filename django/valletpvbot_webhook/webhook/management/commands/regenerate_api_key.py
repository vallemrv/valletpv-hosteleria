"""
Comando para regenerar el TPV_API_KEY en el archivo .env
"""

from django.core.management.base import BaseCommand
from django.conf import settings
import secrets
import os
import re

class Command(BaseCommand):
    help = 'Regenera el TPV_API_KEY en el archivo .env'

    def handle(self, *args, **options):
        # 1. Generar nueva clave
        new_key = secrets.token_urlsafe(32)
        
        # 2. Localizar archivo .env
        env_path = os.path.join(settings.BASE_DIR, '.env')
        
        if not os.path.exists(env_path):
            self.stdout.write(self.style.ERROR(f'No se encontró el archivo .env en: {env_path}'))
            return

        # 3. Leer y reemplazar contenido
        try:
            with open(env_path, 'r') as f:
                content = f.read()
            
            # Buscar si existe la variable
            if 'TPV_API_KEY=' in content:
                # Reemplazar valor existente usando regex para capturar hasta el final de línea
                new_content = re.sub(
                    r'^TPV_API_KEY=.*$', 
                    f'TPV_API_KEY={new_key}', 
                    content, 
                    flags=re.MULTILINE
                )
            else:
                # Añadir al final si no existe
                new_content = content + f'\nTPV_API_KEY={new_key}\n'
            
            # 4. Guardar cambios
            with open(env_path, 'w') as f:
                f.write(new_content)
                
            self.stdout.write(self.style.SUCCESS(f'✅ TPV_API_KEY actualizada correctamente en .env'))
            self.stdout.write(f'\nNueva Clave: {new_key}')
            self.stdout.write(self.style.WARNING('\n⚠️  IMPORTANTE: Debes reiniciar el servidor para que los cambios surtan efecto.'))
            self.stdout.write('⚠️  Asegúrate de actualizar esta clave en todas tus instancias de TPV Server.')
            
        except Exception as e:
            self.stdout.write(self.style.ERROR(f'Error actualizando .env: {str(e)}'))
