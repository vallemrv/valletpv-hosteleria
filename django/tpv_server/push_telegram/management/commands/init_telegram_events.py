# push_telegram/management/commands/init_telegram_events.py
# Comando para inicializar eventos y suscripciones de Telegram

from django.core.management.base import BaseCommand
from push_telegram.models import TelegramEventType, TelegramSubscription


class Command(BaseCommand):
    help = 'Inicializa eventos de Telegram y suscripciones desde configuración'

    def handle(self, *args, **options):
        """
        Crea los eventos y suscripciones definidos en local_config.py
        """
        self.stdout.write("Inicializando eventos de Telegram...")
        
        # Importar configuración local
        try:
            from template_tpv_app import local_config
            hooks = getattr(local_config, 'TELEGRAM_HOOKS', [])
            admin_ids = getattr(local_config, 'TELEGRAM_ADMIN_IDS', [])
        except ImportError:
            self.stdout.write(self.style.ERROR(
                'No se pudo importar local_config. '
                'Asegúrate de que template_tpv_app/local_config.py existe y tiene TELEGRAM_HOOKS y TELEGRAM_ADMIN_IDS'
            ))
            return
        
        if not hooks:
            self.stdout.write(self.style.WARNING(
                'No hay hooks definidos en TELEGRAM_HOOKS en local_config.py'
            ))
            return
        
        # Crear eventos (hooks)
        eventos_creados = 0
        for hook_config in hooks:
            event, created = TelegramEventType.objects.get_or_create(
                code=hook_config['code'],
                defaults={
                    'name': hook_config['name'],
                    'description': hook_config['description'],
                    'activo': True
                }
            )
            
            if created:
                self.stdout.write(self.style.SUCCESS(
                    f"✓ Evento creado: {event.code} - {event.name}"
                ))
                eventos_creados += 1
            else:
                self.stdout.write(f"  Evento ya existe: {event.code}")
        
        # Crear suscripciones para los admin_ids
        if not admin_ids:
            self.stdout.write(self.style.WARNING(
                'No hay IDs de admin definidos en TELEGRAM_ADMIN_IDS en local_config.py'
            ))
        else:
            suscripciones_creadas = 0
            for admin_id in admin_ids:
                for hook_config in hooks:
                    try:
                        event = TelegramEventType.objects.get(code=hook_config['code'])
                        subscription, created = TelegramSubscription.objects.get_or_create(
                            telegram_user_id=admin_id,
                            event_type=event,
                            defaults={'activo': True}
                        )
                        
                        if created:
                            self.stdout.write(self.style.SUCCESS(
                                f"✓ Suscripción creada: user_id={admin_id} → {event.code}"
                            ))
                            suscripciones_creadas += 1
                        else:
                            self.stdout.write(
                                f"  Suscripción ya existe: user_id={admin_id} → {event.code}"
                            )
                    except TelegramEventType.DoesNotExist:
                        self.stdout.write(self.style.ERROR(
                            f"✗ Evento no encontrado: {hook_config['code']}"
                        ))
            
            self.stdout.write("")
            self.stdout.write(self.style.SUCCESS(
                f"Resumen: {eventos_creados} eventos y {suscripciones_creadas} suscripciones creadas"
            ))

