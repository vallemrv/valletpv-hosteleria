# @Author: Manuel Rodriguez <valle>
# @Date:   2019-01-28T12:09:08+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-05-03T14:58:56+02:00
# @License: Apache License v2.0



from tokenapi.http import JsonResponse, HttpResponse
from gestion.models import Teclas, Subteclas


def get_descripcion_ticket(id, nombre):
    art = Teclas.objects.filter(id=id).first()
    if art and art.descripcion_t != None and art.descripcion_t != "":
        nombre = art.descripcion_t
    elif art:
        nombre = art.nombre
    return nombre

def get_descripcion_pedido(id, nombre):
    art = Teclas.objects.filter(id=id).first()
    sub_tecla = None
    for sub in Subteclas.objects.filter(tecla__id=id):
        if sub.nombre in nombre:
            sub_tecla = sub
            break

    sug = ""
    if sub_tecla and sub_tecla.descripcion_r != None and sub_tecla.descripcion_r != "":
        sug = nombre.replace(art.nombre, "")
        sug = sug.replace(sub_tecla.nombre, "")
        nombre = sub_tecla.descripcion_r + " " + sug
    elif art and art.descripcion_r != None and art.descripcion_r != "":
        sug = nombre.replace(art.nombre, "")
        nombre = art.descripcion_r + " " + sug

    return nombre
