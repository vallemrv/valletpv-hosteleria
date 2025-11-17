# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T01:20:27+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-02-01T14:33:04+01:00
# @License: Apache License v2.0

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
def health_check(request):
    """Endpoint simple para verificar conectividad con el servidor"""
    return JsonResponse({'success': True, 'status': 'ok'})

