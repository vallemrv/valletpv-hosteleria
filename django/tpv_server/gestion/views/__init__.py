# @Author: Manuel Rodriguez <valle>
# @Date:   2019-02-03T00:08:21+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-03-05T22:56:56+01:00
# @License: Apache License v2.0


from .camareros import *
from .teclas import *
from .sync import *
from .secciones import *
from .secciones_com import *
from .subteclas import *
from .sugerencias import *
from .zonas import *

from django.shortcuts import render

def inicio(request):
    return render(request, "inicio.html")
