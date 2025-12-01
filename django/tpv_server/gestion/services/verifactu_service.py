import hashlib
import qrcode
from io import BytesIO
from django.core.files.base import ContentFile
from django.conf import settings
from django.db import transaction
from django.utils import timezone
from gestion.models.ticket import Ticket
from decimal import Decimal

import threading

class VerifactuService:
    _lock = threading.Lock()

    @staticmethod
    def generar_alta_factura(ticket, importe_total):
        """
        Realiza el proceso de 'Alta de Factura' para VeriFactu:
        1. Obtiene el último ticket para encadenamiento.
        2. Genera número de factura y serie.
        3. Calcula la huella (hash) actual.
        4. Genera el QR.
        5. Guarda los datos en el ticket.
        """
        
        # Usamos un Lock de Python para proteger contra hilos en el mismo proceso (útil para tests y servidores threaded)
        # Y transaction.atomic + select_for_update para proteger contra múltiples procesos (MySQL/Postgres)
        with VerifactuService._lock:
            with transaction.atomic():
                # BLOQUEO GLOBAL (MUTEX):
                # Bloqueamos TODAS las filas de User para crear un bloqueo de tabla efectivo
                # Esto serializa completamente la generación de números de factura
                from django.contrib.auth.models import User
                try:
                    # Bloqueamos todas las filas de usuarios para forzar serialización
                    list(User.objects.select_for_update().all())
                except:
                    pass # Si no hay usuarios, continuamos (raro en producción)

                # 1. Obtener datos de la empresa (NIF)
                nif_emisor = getattr(settings, 'NIF', '00000000T')
            
                # 2. Determinar serie y número
                # Por defecto usamos el año actual como serie si no hay otra lógica
                serie_actual = timezone.now().strftime('%Y')
                
                # Buscamos el último ticket DE LA MISMA SERIE que YA TENGA numero_factura asignado
                # Excluimos los que están en proceso (numero_factura vacío)
                last_ticket = Ticket.objects.filter(
                    numero_serie=serie_actual
                ).exclude(
                    numero_factura=""
                ).select_for_update().order_by('-id').first()
            
            if last_ticket:
                # Si existe anterior, incrementamos número y cogemos su hash
                try:
                    last_num = int(last_ticket.numero_factura)
                except ValueError:
                    last_num = 0
                
                nuevo_numero = str(last_num + 1)
                prev_hash = last_ticket.signature
            else:
                # Primer ticket de la serie
                nuevo_numero = "1"
                # Hash inicial (cadena de ceros según especificaciones técnicas habituales)
                prev_hash = "0" * 64

            # 3. Preparar datos para el Hash
            # Formato fecha: dd-mm-yyyy (según borrador VeriFactu)
            fecha_expedicion_str = ticket.fecha_expedicion.strftime('%d-%m-%Y')
            
            # Importe total con 2 decimales
            importe_total_str = f"{importe_total:.2f}"
            
            # Cadena de encadenamiento:
            # NIF + NumSerie + NumFactura + Fecha + Importe + HuellaAnterior
            cadena_a_hashear = (
                f"IDEmisor={nif_emisor}&"
                f"NumSerie={serie_actual}&"
                f"NumFactura={nuevo_numero}&"
                f"FechaExpedicion={fecha_expedicion_str}&"
                f"Monto={importe_total_str}&"
                f"HuellaAnterior={prev_hash}"
            )
            
            # Calcular SHA-256
            signature = hashlib.sha256(cadena_a_hashear.encode('utf-8')).hexdigest().upper()
            
            # 4. Generar QR
            # URL Producción AEAT (VeriFactu)
            base_url = "https://www2.agenciatributaria.gob.es/vl/ES/II/SU/H"
            qr_data = f"{base_url}?{cadena_a_hashear}&Signature={signature}"
            
            # 5. Actualizar Ticket
            ticket.numero_serie = serie_actual
            ticket.numero_factura = nuevo_numero
            ticket.prev_hash = prev_hash
            ticket.signature = signature
            ticket.qr_code = qr_data
            
            # 6. Generar y guardar XML firmado
            # IMPORTANTE: Llamamos a generate_verifactu_xml AHORA que el ticket tiene todos los datos (hash, etc.)
            xml_content = VerifactuService.generate_verifactu_xml(ticket)
            ticket.xml_firmado = xml_content
            
            ticket.save()
            
            return ticket

    @staticmethod
    def get_last_hash():
        """
        Obtiene el hash del último documento generado (Ticket o Factura)
        para mantener la cadena global.
        """
        from gestion.models.ticket import Ticket, Factura
        
        # Buscamos el último ticket y la última factura
        last_ticket = Ticket.objects.order_by('-fecha_expedicion').first()
        last_factura = Factura.objects.order_by('-fecha_expedicion').first()
        
        # Comparamos fechas para ver cuál es el verdaderamente último
        # Si no hay ninguno, hash ceros
        if not last_ticket and not last_factura:
            return "0" * 64
            
        if last_ticket and not last_factura:
            return last_ticket.signature
            
        if not last_ticket and last_factura:
            return last_factura.signature
            
        # Ambos existen, comparamos timestamp
        if last_ticket.fecha_expedicion > last_factura.fecha_expedicion:
            return last_ticket.signature
        else:
            return last_factura.signature

    @staticmethod
    def generar_alta_factura_completa(factura, ticket_origen):
        """
        Genera los datos VeriFactu para una Factura Completa que sustituye a un Ticket.
        """
        with VerifactuService._lock:
            with transaction.atomic():
                # BLOQUEO GLOBAL (MUTEX)
                # Bloqueamos TODAS las filas de User para crear un bloqueo de tabla efectivo
                from django.contrib.auth.models import User
                try:
                    list(User.objects.select_for_update().all())
                except:
                    pass

                # 1. Datos Config
                nif_emisor = getattr(settings, 'NIF', '00000000T')
                serie_actual = timezone.now().strftime('%Y') # Serie anual para facturas completas
                
                # 2. Consecutivo (Solo miramos Facturas para el número, pero el Hash es global)
                from gestion.models.ticket import Factura
                last_factura = Factura.objects.filter(
                    numero_serie=serie_actual
                ).exclude(
                    numero_factura=""
                ).select_for_update().order_by('-id').first()
                
                if last_factura:
                    try:
                        last_num = int(last_factura.numero_factura)
                    except ValueError:
                        last_num = 0
                    nuevo_numero = str(last_num + 1)
                else:
                    nuevo_numero = "1"
                
                # 3. Obtener Hash Anterior (Global)
                prev_hash = VerifactuService.get_last_hash()
                
                # 4. Calcular Hash
                fecha_expedicion_str = factura.fecha_expedicion.strftime('%d-%m-%Y')
                importe_total = ticket_origen.entrega # Asumimos mismo importe
                importe_total_str = f"{importe_total:.2f}"
                
                cadena_a_hashear = (
                    f"IDEmisor={nif_emisor}&"
                    f"NumSerie={serie_actual}&"
                    f"NumFactura={nuevo_numero}&"
                    f"FechaExpedicion={fecha_expedicion_str}&"
                    f"Monto={importe_total_str}&"
                    f"HuellaAnterior={prev_hash}"
                )
                
                signature = hashlib.sha256(cadena_a_hashear.encode('utf-8')).hexdigest().upper()
                
                # 5. Generar QR
                base_url = "https://www2.agenciatributaria.gob.es/vl/ES/II/SU/H"
                qr_data = f"{base_url}?{cadena_a_hashear}&Signature={signature}"
                
                # 6. Guardar
                factura.numero_serie = serie_actual
                factura.numero_factura = nuevo_numero
                factura.prev_hash = prev_hash
                factura.signature = signature
                factura.qr_code = qr_data
                
                # 7. Generar y guardar XML firmado
                xml_content = VerifactuService.generate_verifactu_xml(factura)
                factura.xml_firmado = xml_content
                
                factura.save()
                
                return factura

    @staticmethod
    def generate_verifactu_xml(documento):
        """
        Genera el XML de Alta de Factura.
        Soporta tanto Ticket (Factura Simplificada) como Factura (Completa).
        """
        import xml.etree.ElementTree as ET
        from gestion.models.ticket import Ticket, Factura
        
        is_factura_completa = isinstance(documento, Factura)
        
        # Datos de configuración
        nif_emisor = getattr(settings, 'NIF', '00000000T')
        razon_social = getattr(settings, 'RAZON_SOCIAL', 'EMPRESA DEMO')
        iva_pct = getattr(settings, 'IVA', 21)
        
        # Calcular desglose
        if is_factura_completa:
            total = Decimal(str(documento.ticket.entrega))
        else:
            total = Decimal(str(documento.entrega))
            
        base = total * Decimal('100') / (Decimal('100') + Decimal(str(iva_pct)))
        cuota = total - base
        
        # Root
        root = ET.Element("AltaFactu")
        
        # Cabecera
        cabecera = ET.SubElement(root, "Cabecera")
        ET.SubElement(cabecera, "IDVersion").text = "1.0"
        titular = ET.SubElement(cabecera, "Titular")
        ET.SubElement(titular, "NombreRazon").text = razon_social
        ET.SubElement(titular, "NIF").text = nif_emisor
        
        # RegistroFactura
        registro = ET.SubElement(root, "RegistroFactura")
        
        # DatosFactura
        datos_factura = ET.SubElement(registro, "DatosFactura")
        ET.SubElement(datos_factura, "NumSerieFactura").text = documento.numero_serie
        ET.SubElement(datos_factura, "NumFactura").text = documento.numero_factura
        ET.SubElement(datos_factura, "FechaExpedicionFactura").text = documento.fecha_expedicion.strftime('%d-%m-%Y')
        ET.SubElement(datos_factura, "HoraExpedicionFactura").text = documento.fecha_expedicion.strftime('%H:%M:%S')
        ET.SubElement(datos_factura, "TipoFactura").text = documento.tipo_factura
        ET.SubElement(datos_factura, "DescripcionOperacion").text = "Servicios de Hostelería"
        
        # Si es Factura Completa (Sustitución), añadir referencia
        if is_factura_completa:
            ET.SubElement(datos_factura, "TipoRectificativa").text = "S" # S = Sustitución
            referencias = ET.SubElement(datos_factura, "FacturasSustituidas")
            ref = ET.SubElement(referencias, "IDFacturaSustituida")
            ET.SubElement(ref, "NumSerieFactura").text = documento.ticket.numero_serie
            ET.SubElement(ref, "NumFactura").text = documento.ticket.numero_factura
            ET.SubElement(ref, "FechaExpedicionFactura").text = documento.ticket.fecha_expedicion.strftime('%d-%m-%Y')
        
        # DatosDesglose
        datos_desglose = ET.SubElement(registro, "DatosDesglose")
        detalle = ET.SubElement(datos_desglose, "DetalleDesglose")
        ET.SubElement(detalle, "Impuesto").text = "01" # 01 = IVA
        ET.SubElement(detalle, "TipoImpositivo").text = f"{iva_pct:.2f}"
        ET.SubElement(detalle, "BaseImponible").text = f"{base:.2f}"
        ET.SubElement(detalle, "CuotaRepercutida").text = f"{cuota:.2f}"
        
        # Importes Totales
        ET.SubElement(registro, "ImporteTotal").text = f"{total:.2f}"
        
        # DatosControl (Huella)
        datos_control = ET.SubElement(registro, "DatosControl")
        ET.SubElement(datos_control, "Huella").text = documento.signature
        ET.SubElement(datos_control, "HuellaAnterior").text = documento.prev_hash
        ET.SubElement(datos_control, "TipoHash").text = "SHA-256"
        
        # Generar String
        return ET.tostring(root, encoding='utf-8', method='xml').decode('utf-8')
