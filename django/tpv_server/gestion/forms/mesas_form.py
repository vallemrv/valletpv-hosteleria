# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:24:47+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-06T10:40:15+01:00
# @License: Apache License v2.0

from django.forms import ModelForm, Form, ChoiceField
from gestion.models import Zonas, Mesas
from app.custom_widget import ColorPicket

class ZonasForm(ModelForm):

    class Meta:
        model = Zonas
        exclude = ["color"]
        widgets = {
            'rgb': ColorPicket(),
        }

class MesasForm(ModelForm):

    class Meta:
        model = Mesas
        exclude = []
