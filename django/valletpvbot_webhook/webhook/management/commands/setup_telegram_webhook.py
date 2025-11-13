"""
Comandos de gestión personalizados para el webhook.
"""

from django.core.management.base import BaseCommand
from webhook.telegram_service import TelegramService
from django.conf import settings


class Command(BaseCommand):
    help = 'Configura el webhook de Telegram'

    def handle(self, *args, **options):
        webhook_url = settings.TELEGRAM_WEBHOOK_URL
        
        if not webhook_url:
            self.stdout.write(
                self.style.ERROR('TELEGRAM_WEBHOOK_URL no configurado en settings')
            )
            return
        
        telegram_service = TelegramService()
        
        self.stdout.write('Configurando webhook...')
        success = telegram_service.set_webhook(webhook_url)
        
        if success:
            self.stdout.write(
                self.style.SUCCESS(f'Webhook configurado correctamente: {webhook_url}')
            )
            
            # Mostrar información del webhook
            info = telegram_service.get_webhook_info()
            if info:
                self.stdout.write('\nInformación del webhook:')
                self.stdout.write(f"  URL: {info.get('url')}")
                self.stdout.write(f"  Pending updates: {info.get('pending_update_count', 0)}")
                self.stdout.write(f"  Max connections: {info.get('max_connections', 0)}")
        else:
            self.stdout.write(
                self.style.ERROR('Error al configurar el webhook')
            )
