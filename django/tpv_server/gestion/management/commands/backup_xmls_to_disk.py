import os
from datetime import datetime
from django.core.management.base import BaseCommand
from django.conf import settings
from gestion.models.ticket import Ticket, Factura

class Command(BaseCommand):
    help = 'Realiza una copia de seguridad de los XML firmados de VeriFactu a una carpeta local.'

    def add_arguments(self, parser):
        parser.add_argument(
            '--target-dir',
            type=str,
            default='/home/valle/backups/verifactu_xmls',
            help='Directorio destino para los backups'
        )
        parser.add_argument(
            '--date',
            type=str,
            help='Fecha a procesar en formato YYYY-MM-DD (por defecto: hoy)',
            required=False
        )

    def handle(self, *args, **options):
        target_dir = options['target_dir']
        date_str = options.get('date')

        if date_str:
            try:
                target_date = datetime.strptime(date_str, '%Y-%m-%d').date()
            except ValueError:
                self.stdout.write(self.style.ERROR('Formato de fecha inválido. Use YYYY-MM-DD'))
                return
        else:
            target_date = datetime.now().date()

        # Crear directorio si no existe
        # Estructura: target_dir/YYYY/MM/DD/
        final_dir = os.path.join(
            target_dir, 
            target_date.strftime('%Y'), 
            target_date.strftime('%m'), 
            target_date.strftime('%d')
        )
        os.makedirs(final_dir, exist_ok=True)

        self.stdout.write(f"Iniciando backup de XMLs para {target_date} en {final_dir}")

        # 1. Procesar Tickets (Facturas Simplificadas)
        # Filtramos por fecha_expedicion (que es DateTimeField)
        tickets = Ticket.objects.filter(
            fecha_expedicion__date=target_date,
            xml_firmado__isnull=False
        ).exclude(xml_firmado="")

        count_tickets = 0
        for ticket in tickets:
            filename = f"TICKET_{ticket.numero_serie}_{ticket.numero_factura}.xml"
            filepath = os.path.join(final_dir, filename)
            
            try:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(ticket.xml_firmado)
                count_tickets += 1
            except Exception as e:
                self.stdout.write(self.style.ERROR(f"Error guardando {filename}: {str(e)}"))

        # 2. Procesar Facturas (Completas)
        facturas = Factura.objects.filter(
            fecha_expedicion__date=target_date,
            xml_firmado__isnull=False
        ).exclude(xml_firmado="")

        count_facturas = 0
        for factura in facturas:
            filename = f"FACTURA_{factura.numero_serie}_{factura.numero_factura}.xml"
            filepath = os.path.join(final_dir, filename)
            
            try:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(factura.xml_firmado)
                count_facturas += 1
            except Exception as e:
                self.stdout.write(self.style.ERROR(f"Error guardando {filename}: {str(e)}"))

        self.stdout.write(self.style.SUCCESS(
            f"Backup completado. Tickets: {count_tickets}, Facturas: {count_facturas}"
        ))
