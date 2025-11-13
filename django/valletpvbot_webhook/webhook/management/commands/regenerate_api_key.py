"""
Comando para generar un API key para un TPV.
"""

from django.core.management.base import BaseCommand
from webhook.models import TPVInstance
import secrets


class Command(BaseCommand):
    help = 'Genera un nuevo API Key para un TPV existente'

    def add_arguments(self, parser):
        parser.add_argument('tpv_id', type=str, help='UUID del TPV')

    def handle(self, *args, **options):
        tpv_id = options['tpv_id']
        
        try:
            tpv = TPVInstance.objects.get(id=tpv_id)
        except TPVInstance.DoesNotExist:
            self.stdout.write(
                self.style.ERROR(f'TPV con ID {tpv_id} no encontrado')
            )
            return
        
        # Generar nuevo API Key
        old_key = tpv.api_key
        new_key = secrets.token_urlsafe(32)
        
        tpv.api_key = new_key
        tpv.save(update_fields=['api_key'])
        
        self.stdout.write(
            self.style.SUCCESS(f'API Key regenerado para TPV: {tpv.name}')
        )
        self.stdout.write(f'\nNuevo API Key: {new_key}')
        self.stdout.write(
            self.style.WARNING('\n⚠️  El API Key anterior ha sido invalidado')
        )
        self.stdout.write('Actualiza la configuración del TPV con el nuevo API Key')
