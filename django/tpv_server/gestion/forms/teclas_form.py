# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:24:47+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-04-17T10:54:00+02:00
# @License: Apache License v2.0

from django.forms import ModelForm, Form, ChoiceField
from gestion.models import Familias, Teclas, Secciones
from app.custom_widget import ColorPicket

class FamiliasForm(ModelForm):

    class Meta:
        model = Familias
        exclude = ["numtapas"]

class SeccionesForm(ModelForm):

    class Meta:
        model = Secciones
        exclude = ["color"]
        widgets = {
            'rgb': ColorPicket(),
        }


class TeclasForm(ModelForm):

    class Meta:
        model = Teclas
        exclude = []

class TeclasFormOrden(ModelForm):
    
    class Meta:
        model = Teclas
        fields = ["orden"]

class TeclaSeccionForm(Form):
    try:
        seccion_principal = ChoiceField(label="Seccion principal", choices=
                                        [('-1', '----')] +
                                        [(choice.pk, choice) for choice in Secciones.objects.all()], required=True)
        seccion_secundaria = ChoiceField(label="Favoritos", choices=
                                        [('-1' ,'----')] +
                                        [(choice.pk, choice) for choice in Secciones.objects.all()] )
    except:
        pass
