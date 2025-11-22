# push_telegram/management/commands/init_telegram_events.py
# Comando para inicializar eventos y suscripciones de Telegram

from django.core.management.base import BaseCommand
from push_telegram.models import TelegramEventType, TelegramSubscription


class Command(BaseCommand):
    help = 'Inicializa eventos de Telegram predefinidos'

    def handle(self, *args, **options):
        """
        Crea los eventos de Telegram predefinidos y permite crear suscripciones
        """
        self.stdout.write("Inicializando eventos de Telegram...")
        
        # Eventos predefinidos del sistema
        eventos_predefinidos = [
            {
                'code': 'nuevo_dispositivo',
                'nombre': 'Nuevo Dispositivo',
                'descripcion': 'Se notifica cuando se detecta un nuevo dispositivo intentando conectarse al TPV'
            },
            {
                'code': 'cambio_zona',
                'nombre': 'Cambio de Zona',
                'descripcion': 'Se notifica cuando se cambia una mesa a una zona vigilada'
            }
        ]
        
        # Crear eventos predefinidos
        eventos_creados = 0
        for evento_config in eventos_predefinidos:
            event, created = TelegramEventType.objects.get_or_create(
                code=evento_config['code'],
                defaults={
                    'nombre': evento_config['nombre'],
                    'descripcion': evento_config['descripcion'],
                    'activo': True
                }
            )
            
            if created:
                self.stdout.write(self.style.SUCCESS(
                    f"✓ Evento creado: {event.code} - {event.nombre}"
                ))
                eventos_creados += 1
            else:
                self.stdout.write(f"  Evento ya existe: {event.code}")
        
        self.stdout.write("")
        self.stdout.write(self.style.SUCCESS(
            f"✓ {eventos_creados} eventos creados"
        ))
        self.stdout.write("")
        self.stdout.write("Para crear suscripciones:")
        self.stdout.write("  1. Ve al admin de Django: /admin/")
        self.stdout.write("  2. Entra en 'Suscripciones Push'")
        self.stdout.write("  3. Crea suscripciones para cada usuario")
        self.stdout.write("")
        self.stdout.write(self.style.WARNING(
            "IMPORTANTE: Puedes configurar filtros para las suscripciones:"
        ))
        self.stdout.write("  - Ejemplo para 'cambio_zona': {\"zonas\": [1, 2, 3]}")
        self.stdout.write("  - Sin filtros: {} (aplica a todos los casos)")

