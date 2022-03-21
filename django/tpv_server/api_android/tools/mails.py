# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-28T11:59:41+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-30T14:33:08+01:00
# @License: Apache License v2.0


from tokenapi.http import JsonResponse, HttpResponse
from gestion.models import HorarioUsr
from django.contrib.auth.models import User
from django.core.mail import send_mail
from django.utils.html import strip_tags
from django.conf import settings
from datetime import datetime


def getUsuariosMail():
    now = datetime.now().strftime("%H:%M")
    users = User.objects.filter(horariousr__hora_fin__gte=now, horariousr__hora_ini__lte=now)
    if users.count() > 0:
        return users
    else:
        return User.objects.all()

def  send_cierre(user, desglose):
    now = datetime.now()
    titulo = "Cierre " + now.strftime("%d/%m/%Y-%H:%M")
    mensaje = '''
            <html lang="es">
            <head>
            <title>Cierre de {0}</title>
            </head>
            <body>
            <h4>Cierre de caja en {0}</h4>
            <p> Total efectivo: {1:01.2f} € </p>
            <p> Total tarjeta: {2:01.2f} € </p>
            <p> Pagos por caja: {3:01.2f} € </p>
            <p> Total caja del dia: {4:01.2f} € </p>
            <p> Caja real: {5:01.2f} € </p>
            <p> Descuadre: {6:01.2f} € </p>
            <h4>Desglose de gastos </h4>
            '''.format(settings.BRAND_TITLE, desglose["TotalEfectivo"],
                       desglose["TotalTarjeta"], desglose["gastos"],
                       desglose["TotalCaja"], desglose["CajaReal"],
                       float(desglose["Descuadre"]))

    for g in desglose["des_gastos"]:
        mensaje += "<p> {0:50}  {1:>6} € </p>".format(g["Descripcion"], "{0:01.2f}".format(g["Importe"]))

    mensaje +=  "<h4>Desglose de ventas </h4>"

    for v in desglose["des_ventas"]:
        mensaje += "<p> {0:>6}  {1:100}  {2:>7} € </p>".format(v["Can"],
                                                             v["Nombre"],
                                                             "{0:01.2f}".format(v["Total"]))

    mensaje += '</body> </html> '
    hora = now.strftime('%H:%M')
    send_mail(
    titulo,
    strip_tags(mensaje),
    '{2} - {0}  <{1}>'.format(getFranja(hora), settings.MAIL, getDiaSemana(now)),
    [user.email],
    html_message=mensaje
    )


def getFranja(hora):
    if (hora < "06:00" or hora >= "21:30") and hora <= "23:59":
        return "noche"
    if hora >= "19:00" and hora <= "21:29":
        return "tarde"
    if hora >= "15:00" and hora <= "18:59":
        return "medio dia"
    if hora >= "06:01" and hora <= "14:59":
        return "mañana"
    return ""

def getDiaSemana(now):
    dia = now.weekday()
    hora = now.strftime('%H:%M')

    if (hora < "06:00" or hora >= "21:30") and hora <= "23:59":
        dia = dia - 1
    if dia==0:
        return "Lunes"
    if dia==1:
        return "Martes"
    if dia==2:
        return "Miercoles"
    if dia==3:
        return "Jueves"
    if dia==4:
        return "Viernes"
    if dia==5:
        return "Sábado"
    if dia==6:
        return "Domingo"
    else:
        return "Domingo"
