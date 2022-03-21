# -*- coding: utf-8 -*-

# @Author: Manuel Rodriguez <valle>
# @Date:   27-Aug-2017
# @Email:  valle.mrv@gmail.com
# @Filename: admin_extras.py
# @Last modified by:   valle
# @Last modified time: 2019-03-06T18:41:04+01:00
# @License: Apache license vesion 2.0

import json
from django import template
from django.utils.safestring import SafeString

register = template.Library()

@register.filter(name='icon_pass')
def icon_pass(c):
    if c.pass_field == "":
        return 'fa-unlock-alt'
    else:
        return 'fa-lock'

@register.filter(name='get_total_precio')
def get_total_precio(l):
    return l.tecla.p1 + l.incremento

@register.filter(name='icon_activo')
def icon_activo(c):
    if c.autorizado == 1:
        return 'fa-user-check'
    else:
        return 'fa-user'

@register.filter(name='rgb_seccion')
def rgb_seccion(l):
    sec = l.teclaseccion_set.all().first()
    if sec:
        return sec.seccion.rgb
    else:
        return '214,219,223'

@register.filter(name='get_color')
def get_color(l, hex=False):
    return l.get_color(hex)

@register.filter(name='is_max')
def is_max(l, max):
    return "true" if len(l) >= max else "false"

@register.filter(name='secciones')
def secciones(l):
    secciones = []
    for s in l.teclaseccion_set.all():
        secciones.append(str(s.seccion))

    return ", ".join(secciones)

@register.filter(name='zona')
def zona(l):
    nombres = l.mesaszona_set.all().values_list("zona__nombre", flat=True)
    return ", ".join(nombres)
    
            
@register.filter(name='nombre')
def nombre(l):
    if l.tecla_child != None:
        return l.tecla_child.nombre
    else:
        return ""

@register.filter(name='get_items_edit')
def get_items_edit(l, orden=None):
    return SafeString(l.get_items_edit(orden=orden))

@register.filter(name='get_items_edit_with')
def get_items_edit_with(l):
    return SafeString(l.get_items_edit(show_secciones=True))

@register.filter(name='get_items_add')
def get_items_add(l):
    return SafeString(l.get_items_add())

