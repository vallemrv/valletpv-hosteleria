# -*- coding: utf-8 -*-

# @Author: Manuel Rodriguez <valle>
# @Date:   27-Aug-2017
# @Email:  valle.mrv@gmail.com
# @Filename: admin_extras.py
# @Last modified by:   valle
# @Last modified time: 2019-02-19T08:17:43+01:00
# @License: Apache license vesion 2.0

from django import template
from django.conf import settings
import json
register = template.Library()


@register.filter(name='get_control_vue')
def get_control_vue(field):
    html = field.as_widget(attrs={"class":'form-control',
                                  "ref": field.name}).replace("\n", " ")

    return html

@register.filter(name='is_color')
def is_color(str):
    return 'rgb' in str or 'color' in str


@register.filter(name='addattrs')
def addattrs(field, args):
    attr = {}
    try:
        args_parse = args.replace("'", '"')
        attr = json.loads(args_parse)

    except Exception as error:
        print ("[ERROR  ] %s" % error)
    return field.as_widget(attrs=attr)

@register.filter('klass')
def klass(ob):
    return ob.field.widget.__class__.__name__



@register.filter(name='addcss')
def addcss(field, css):
    return field.as_widget(attrs={"class":css})


@register.simple_tag
def brand(tipo):
    if tipo == "title":
        return settings.BRAND_TITLE
    else:
        return settings.BRAND
