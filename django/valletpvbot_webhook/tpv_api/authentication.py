"""
Autenticación personalizada para TPVs usando API Keys.
"""

from rest_framework import authentication
from rest_framework import exceptions
from webhook.models import TPVInstance


class TPVApiKeyAuthentication(authentication.BaseAuthentication):
    """
    Autenticación basada en API Key para TPVs.
    El API Key debe enviarse en el header: Authorization: ApiKey <api_key>
    """
    
    def authenticate(self, request):
        auth_header = request.META.get('HTTP_AUTHORIZATION', '')
        
        if not auth_header.startswith('ApiKey '):
            return None
        
        try:
            api_key = auth_header.split('ApiKey ')[1].strip()
        except IndexError:
            raise exceptions.AuthenticationFailed('API Key inválido')
        
        try:
            tpv = TPVInstance.objects.get(api_key=api_key, is_active=True)
        except TPVInstance.DoesNotExist:
            raise exceptions.AuthenticationFailed('API Key no válido o TPV inactivo')
        
        # Actualizar heartbeat
        tpv.update_heartbeat()
        
        return (tpv, None)
    
    def authenticate_header(self, request):
        return 'ApiKey'
