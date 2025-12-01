from django.db import transaction
from django.shortcuts import get_object_or_404
from gestion.models.ticket import Ticket, Factura
from gestion.services.verifactu_service import VerifactuService

def crear_factura_desde_ticket(ticket_id, datos_cliente):
    """
    Crea una Factura Completa a partir de un Ticket (Factura Simplificada),
    gestionando la lógica de VeriFactu (encadenamiento, hash, QR) y marcando
    el ticket como facturado.
    """
    
    # 1. Validar Ticket
    ticket = get_object_or_404(Ticket, pk=ticket_id)
    
    if ticket.esta_facturada:
        raise ValueError("Este ticket ya ha sido facturado.")

    # 2. Transacción Atómica
    with transaction.atomic():
        # Bloquear el ticket para evitar concurrencia
        ticket = Ticket.objects.select_for_update().get(pk=ticket_id)
        if ticket.esta_facturada:
             raise ValueError("Este ticket ya ha sido facturado.")

        # 3. Crear instancia de Factura (sin guardar hash aún)
        factura = Factura(
            ticket=ticket,
            nombre_razon=datos_cliente.get('nombre', ''),
            nif=datos_cliente.get('nif', ''),
            direccion=datos_cliente.get('direccion', ''),
            cp=datos_cliente.get('cp', ''),
            poblacion=datos_cliente.get('poblacion', ''),
            provincia=datos_cliente.get('provincia', ''),
            email=datos_cliente.get('email', ''),
            tipo_factura='F2' # Factura Completa
        )
        
        # Guardamos inicialmente para tener ID (aunque VerifactuService lo actualiza)
        factura.save()

        # 4, 5, 6, 7. Delegar en VerifactuService la lógica compleja
        # (Cálculo de número, serie, hash, QR y guardado final)
        VerifactuService.generar_alta_factura_completa(factura, ticket)
        
        # 8. Actualizar Ticket original
        ticket.esta_facturada = True
        ticket.save()
        
        return factura
