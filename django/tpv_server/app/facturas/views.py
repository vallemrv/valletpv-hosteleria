from token import OP
from django.conf import  settings
from django.db.models import  Sum, Count
from django.http import  HttpResponse
from django.shortcuts import render, redirect
from django.template.loader import get_template
from io import BytesIO as OpenIO
from gestion.models import Ticket
import os

import trml2pdf


def index(request, id, uid):
    ticket = Ticket.objects.filter(id=id, uid=uid).first()
    if ticket:
        if ticket.url_factura == "":
            return render(request, "facturas/datos_cliente.html",
                            {'id':id, 'uid':uid, "empresa":settings.EMPRESA})
        else:
            try:
                f = open(ticket.url_factura, "rb")
                pdfstr = f.read()
                return HttpResponse(pdfstr, content_type='application/pdf')
            except:
               return render(request, "facturas/error_datos_cliente.html", {'empresa':settings.BRAND_TITLE })
 
    else:
        return render(request, "facturas/error_datos_cliente.html", {'empresa':settings.BRAND_TITLE })


def crear_factura(request):
    if request.method=="POST":
        id = request.POST["id"]
        uid = request.POST["uid"]
        ticket = Ticket.objects.filter(id=id, uid=uid).first()
        if not ticket:
            return render(request, "facturas/error_datos_cliente.html", {'empresa':settings.BRAND_TITLE })
        if ticket.url_factura != "":
            return HttpResponse("la factura")

        nombre = request.POST["nombre"]
        direccion = request.POST["direccion"]
        localidad = request.POST["localidad"]
        poblacion = request.POST["provincia"]
        cp = request.POST["cp"]
        nif = request.POST["cif"]

        rows = ticket.ticketlineas_set.values("linea__idart",  
                                          "linea__descripcion_t",
                                          "linea__precio").annotate(can=Count('linea__idart'),
                                                                    total=Sum("linea__precio"))
        lineas = []
        total = 0
        for r in rows:
            total = total + r["total"]
            lineas.append({
                "idart": r["linea__idart"],
                "Nombre": r["linea__descripcion_t"],
                "Precio": "{0:.2f}".format(r["linea__precio"]),
                "Total": "{0:.2f}".format(r["total"]),
                "Can": r["can"]
            })

        fecha_split = ticket.fecha.split("/")
        fecha = fecha_split[2]+'/'+fecha_split[1]+'/'+fecha_split[0]
        iva = settings.IVA
        can_base = (total * 100) / (100 + iva) 
        can_iva = total - can_base
        data = {
                'title': "Factura num %s" % id,
                "fecha": fecha,
                "num_factura": id,
                "productos": lineas,
                "can_base":  "{0:.2f}".format(can_base),
                "can_iva":  "{0:.2f}".format(can_iva),
                "total": "{0:.2f}".format(total),
                "iva": iva,
                "nombre": nombre,
                "DNI": nif,
                "domicilio": direccion,
                "poblacion": localidad,
                "provincia": poblacion,
                "cp": cp,
                "razon_social": settings.RAZON_SOCIAL,
                "emp_nif": settings.NIF,
                "emp_direccion": settings.DIRECCION,
                "emp_telefono": settings.TELEFONO,
                "emp_poblacion": settings.POBLACION,
                "emp_provincia": settings.PROVINCIA,
                "emp_cp": settings.CP,
         }

        template = get_template("facturas/doc/documento_factura.xml")
        xmlstring = template.render(data)
        pdfstr = trml2pdf.parseString(xmlstring.encode("utf-8"))
        pdf_path = settings.MEDIA_ROOT
        url_f = os.path.join(pdf_path, "factura_"+id+".pdf")
        ticket.url_factura = url_f
        ticket.save()
        f = open(url_f, "wb")
        f.write(pdfstr)
        return redirect("/app/facturas/"+id+"/"+uid)
    else:
        return render(request, "facturas/error_datos_cliente.html", {'empresa':settings.BRAND_TITLE })