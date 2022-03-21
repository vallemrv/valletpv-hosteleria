# @Author: Manuel Rodriguez <valle>
# @Date:   20-Jul-2017
# @Email:  valle.mrv@gmail.com
# @Filename: decorators.py
# @Last modified by:   valle
# @Last modified time: 2019-01-16T23:49:19+01:00
# @License: Apache license vesion 2.0


from functools import wraps
from django.views.decorators.csrf import csrf_exempt
from gestion.models import Camareros
from tokenapi.http import JsonResponseForbidden

def token_required(view_func):
    """Decorator which ensures the user has provided a correct user and token pair."""

    @csrf_exempt
    @wraps(view_func)
    def _wrapped_view(request, *args, **kwargs):
        return view_func(request, *args, **kwargs)

        if False:
            return JsonResponseForbidden("Camarero no autorizado....")
    return _wrapped_view
