# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:24:06+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-01-31T16:25:12+01:00
# @License: Apache License v2.0

from tokenapi.http import JsonError, JsonResponse
from django.db.models import Q
from django.views.decorators.csrf import csrf_exempt
from gestion.models.teclados import Teclas


@csrf_exempt
def art_listado(request):
    offset = request.POST["offset"] if "offset"  in request.POST else 0
    t = request.POST["t"] if 't' in request.POST else 1
    idSec = -1
    articulos = []
    if 'sec' in request.POST:
        idSec = request.POST["sec"]
        articulos = Teclas.objects.raw('''
                                        SELECT * FROM teclas
                                        INNER JOIN teclaseccion ON t.ID=teclaseccion.IDTecla
                                        WHERE teclaseccion.ID={0}
                                        ORDER BY Orden DESC, Nombre
                                        '''.format(idSec))

    if 'str' in request.POST:
        str = request.POST["str"]
        articulos = Teclas.objects.filter(Q(nombre__icontains=str) | Q(tag__icontains=str))


    lstArt = []
    for art in articulos[offset:50]:
        if t == 1:
            precio = art.p1
        else:
            precio = art.p2
        seccion_con = art.teclascom_set.all().first()
        seccion =  art.teclaseccion_set.all().first()
        if seccion:
            seccion = seccion.seccion


        obj = {
            'ID': art.id,
            'Nombre': art.nombre,
            'P1': art.p1,
            'P2': art.p2,
            'Orden': art.orden,
            'IDFamilia': art.familia.pk,
            'Tag': art.tag,
            'TTF': art.ttf,
            'Precio': precio,
            'RGB': art.get_color(),
            'Color': seccion.color if seccion else "gray",
            'IDSeccion': seccion.id if seccion else -1,
            'IDSecCom': seccion_con.pk if seccion_con else -1,
            'NomFamilia': art.familia.nombre,
            }
        lstArt.append(obj)

    return JsonResponse(lstArt)


@csrf_exempt
def ls_todos(request):
    lstArt = []
    teclas = Teclas.objects.all()
    for art in teclas:
        teclasseccion = art.teclaseccion_set.all()
        if teclasseccion.count() > 0:
            obj = {
                'ID': art.id,
                'Nombre': art.nombre,
                'P1': art.p1,
                'P2': art.p2,
                'Orden': art.orden,
                'IDFamilia': art.familia.pk,
                'Tag': art.tag,
                'TTF': art.ttf,
                'Precio': art.p1,
                'RGB': teclasseccion[0].seccion.rgb,
                'IDSeccion': teclasseccion[0].seccion.id,
                "IDSec2": teclasseccion[1].seccion.id if teclasseccion.count() > 1 else -1,
                }
            lstArt.append(obj)

    return JsonResponse(lstArt)
