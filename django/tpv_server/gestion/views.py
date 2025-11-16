# @Author: Manuel Rodriguez <valle>
# @Date:   2025-01-14
# @Email:  valle.mrv@gmail.com
# @License: Apache License v2.0

from django.http import JsonResponse, FileResponse, Http404
from django.shortcuts import render, get_object_or_404
from django.views.decorators.csrf import csrf_exempt
from gestion.models.ticket import Ticket
from gestion.models.camareros import Camareros
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from decimal import Decimal
import json
import os
from django.conf import settings
from datetime import datetime


def calcular_iva(total_con_iva, tipo_iva=None):
    """Calcula la base imponible y el IVA desde el total con IVA incluido"""
    if tipo_iva is None:
        tipo_iva = getattr(settings, 'IVA', 21)
    
    total = Decimal(str(total_con_iva))
    # Fórmula: subtotal = total * 100 / (100 + IVA)
    base = total * Decimal('100') / (Decimal('100') + Decimal(str(tipo_iva)))
    iva = total - base
    
    return {
        'base': float(base),
        'iva': float(iva),
        'total': float(total),
        'tipo_iva': tipo_iva
    }


def generar_pdf_factura(ticket, datos_cliente, total_con_iva):
    """Genera un PDF de factura profesional"""
    
    # Crear directorio si no existe
    facturas_dir = os.path.join(settings.BASE_DIR, 'static', 'media_testTPV', 'facturas')
    os.makedirs(facturas_dir, exist_ok=True)
    
    # Nombre del archivo
    filename = f"factura_{ticket.id}_{ticket.uid}.pdf"
    filepath = os.path.join(facturas_dir, filename)
    
    # Crear el PDF
    c = canvas.Canvas(filepath, pagesize=A4)
    width, height = A4
    
    # Título
    c.setFont("Helvetica-Bold", 20)
    c.drawString(50, height - 50, "FACTURA")
    
    # Número de factura
    c.setFont("Helvetica", 12)
    c.drawString(50, height - 75, f"Nº Factura: {ticket.id}")
    c.drawString(50, height - 90, f"Fecha: {ticket.fecha}")
    c.drawString(50, height - 105, f"Hora: {ticket.hora}")
    
    # Datos del establecimiento desde settings
    c.setFont("Helvetica-Bold", 12)
    c.drawString(350, height - 75, getattr(settings, 'RAZON_SOCIAL', 'Valle TPV'))
    c.setFont("Helvetica", 10)
    c.drawString(350, height - 90, f"NIF: {getattr(settings, 'NIF', '')}")
    
    y_empresa = height - 105
    if getattr(settings, 'DIRECCION', ''):
        c.drawString(350, y_empresa, getattr(settings, 'DIRECCION', ''))
        y_empresa -= 15
    
    # CP, Población y Provincia
    cp_poblacion_provincia = []
    if getattr(settings, 'CP', ''):
        cp_poblacion_provincia.append(getattr(settings, 'CP', ''))
    if getattr(settings, 'POBLACION', ''):
        cp_poblacion_provincia.append(getattr(settings, 'POBLACION', ''))
    if getattr(settings, 'PROVINCIA', ''):
        cp_poblacion_provincia.append(f"({getattr(settings, 'PROVINCIA', '')})")
    
    if cp_poblacion_provincia:
        c.drawString(350, y_empresa, ' '.join(cp_poblacion_provincia))
        y_empresa -= 15
    
    if getattr(settings, 'TELEFONO', ''):
        c.drawString(350, y_empresa, f"Tel: {getattr(settings, 'TELEFONO', '')}")
    
    # Línea separadora
    c.setStrokeColor(colors.grey)
    c.line(50, height - 140, width - 50, height - 140)
    
    # Datos del cliente
    c.setFont("Helvetica-Bold", 12)
    c.drawString(50, height - 165, "DATOS DEL CLIENTE")
    c.setFont("Helvetica", 10)
    c.drawString(50, height - 185, f"Nombre: {datos_cliente['nombre']}")
    c.drawString(50, height - 200, f"NIF/CIF: {datos_cliente['nif']}")
    c.drawString(50, height - 215, f"Dirección: {datos_cliente['direccion']}")
    c.drawString(50, height - 230, f"CP: {datos_cliente['cp']} - {datos_cliente['poblacion']}")
    if datos_cliente.get('email'):
        c.drawString(50, height - 245, f"Email: {datos_cliente['email']}")
    
    # Línea separadora
    c.line(50, height - 265, width - 50, height - 265)
    
    # Cabecera de la tabla
    y_position = height - 295
    c.setFont("Helvetica-Bold", 10)
    c.drawString(50, y_position, "Descripción")
    c.drawString(320, y_position, "Cant.")
    c.drawString(380, y_position, "Precio")
    c.drawString(450, y_position, "Total")
    
    # Línea bajo cabecera
    c.line(50, y_position - 5, width - 50, y_position - 5)
    
    # Líneas del ticket
    c.setFont("Helvetica", 9)
    y_position -= 25
    
    lineas = ticket.ticketlineas_set.all().values(
        'linea__descripcion_t',
        'linea__precio'
    )
    
    # Agrupar líneas iguales
    lineas_agrupadas = {}
    for linea in lineas:
        desc = linea['linea__descripcion_t']
        precio = float(linea['linea__precio'])
        if precio > 0:  # Solo líneas con precio
            if desc in lineas_agrupadas:
                lineas_agrupadas[desc]['cantidad'] += 1
                lineas_agrupadas[desc]['total'] += precio
            else:
                lineas_agrupadas[desc] = {
                    'cantidad': 1,
                    'precio': precio,
                    'total': precio
                }
    
    # Imprimir líneas
    for desc, datos in lineas_agrupadas.items():
        if y_position < 150:  # Nueva página si es necesario
            c.showPage()
            y_position = height - 50
            c.setFont("Helvetica", 9)
        
        c.drawString(50, y_position, desc[:40])  # Limitar longitud
        c.drawString(330, y_position, str(datos['cantidad']))
        c.drawString(380, y_position, f"{datos['precio']:.2f}€")
        c.drawString(450, y_position, f"{datos['total']:.2f}€")
        y_position -= 20
    
    # Totales
    y_position -= 20
    c.line(50, y_position, width - 50, y_position)
    
    calculos = calcular_iva(total_con_iva)
    
    y_position -= 25
    c.setFont("Helvetica-Bold", 11)
    c.drawString(350, y_position, "BASE IMPONIBLE:")
    c.drawString(480, y_position, f"{calculos['base']:.2f}€")
    
    y_position -= 20
    c.drawString(350, y_position, f"IVA ({calculos['tipo_iva']}%):")
    c.drawString(480, y_position, f"{calculos['iva']:.2f}€")
    
    y_position -= 25
    c.setFont("Helvetica-Bold", 14)
    c.drawString(350, y_position, "TOTAL:")
    c.drawString(470, y_position, f"{calculos['total']:.2f}€")
    
    # Pie de página
    c.setFont("Helvetica", 8)
    c.drawString(50, 50, "Gracias por su visita")
    c.drawString(50, 35, f"Mesa: {ticket.mesa}")
    
    camarero = Camareros.objects.filter(pk=ticket.camarero_id).first()
    if camarero:
        c.drawString(50, 20, f"Camarero: {camarero.nombre} {camarero.apellidos}")
    
    c.save()
    
    return filename


@csrf_exempt
def factura_view(request, ticket_id, uid):
    """Vista para mostrar formulario o PDF de factura"""
    
    
    # Verificar que el ticket existe y el UID coincide
    ticket = get_object_or_404(Ticket, id=ticket_id, uid=uid)
    
    # Si ya tiene factura, mostrar el PDF
    if ticket.url_factura:
        filepath = os.path.join(
            settings.BASE_DIR, 
            'static', 
            'media_testTPV', 
            'facturas',
            ticket.url_factura
        )
        
        if os.path.exists(filepath):
            return FileResponse(
                open(filepath, 'rb'),
                content_type='application/pdf',
                as_attachment=False,
                filename=ticket.url_factura
            )
        else:
            raise Http404("Factura no encontrada")
    
    # Si no tiene factura y es GET, mostrar formulario
    if request.method == 'GET':
        # Calcular total
        total = 0
        for linea in ticket.ticketlineas_set.all():
            if linea.linea.precio > 0:
                total += float(linea.linea.precio)
        
        context = {
            'ticket': ticket,
            'total': f"{total:.2f}"
        }
        return render(request, 'formulario_factura.html', context)
    
    # Si es POST, generar la factura
    elif request.method == 'POST':
        try:
            datos_cliente = json.loads(request.body)
            
            # Validar campos requeridos
            campos_requeridos = ['nombre', 'nif', 'direccion', 'cp', 'poblacion']
            for campo in campos_requeridos:
                if not datos_cliente.get(campo):
                    return JsonResponse({'error': f'El campo {campo} es requerido'}, status=400)
            
            # Calcular total con IVA
            total_con_iva = 0
            for linea in ticket.ticketlineas_set.all():
                if linea.linea.precio > 0:
                    total_con_iva += float(linea.linea.precio)
            
            # Generar PDF
            filename = generar_pdf_factura(ticket, datos_cliente, total_con_iva)
            
            # Guardar nombre del archivo en el ticket
            ticket.url_factura = filename
            ticket.save()
            
            return JsonResponse({'success': True, 'filename': filename})
            
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Datos JSON inválidos'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
    
    return JsonResponse({'error': 'Método no permitido'}, status=405)
