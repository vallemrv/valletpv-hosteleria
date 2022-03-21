# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:17:33+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-04T00:18:56+01:00
# @License: Apache License v2.0


from django.forms import ModelForm
from gestion.models import Camareros

class CamarerosForm(ModelForm):


    class Meta:
        model = Camareros
        exclude = ["activo", "autorizado", "pass_field", "email"]
