# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:24:47+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-07T02:13:09+01:00
# @License: Apache License v2.0

from django.forms import ModelForm
from gestion.models import SeccionesCom, Teclascom, Subteclas, Sugerencias

class SeccionesComForm(ModelForm):

    class Meta:
        model = SeccionesCom
        exclude = []


class TeclascomForm(ModelForm):

    class Meta:
        model = Teclascom
        exclude = ['seccion', 'tecla']

class SubteclasForm(ModelForm):

    class Meta:
        model = Subteclas
        exclude = ['tecla', "tecla_child",  "orden"]

class SugerenciasForm(ModelForm):

    class Meta:
        model = Sugerencias
        exclude = ['tecla']
