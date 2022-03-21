# @Author: Manuel Rodriguez <valle>
# @Date:   27-Jun-2018
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 27-Jun-2018
# @License: Apache license vesion 2.0

from tokenapi.http import JsonResponse
from .controlimpresion import *


def index(request):
    return JsonResponse("Error no tine permisos para esta urls")
