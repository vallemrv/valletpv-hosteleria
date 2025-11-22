from django.contrib import admin
from .models import TokenCallbackMapping


@admin.register(TokenCallbackMapping)
class TokenCallbackMappingAdmin(admin.ModelAdmin):
    list_display = ('token_short', 'uid_short', 'accion', 'empresa', 'usada', 'expira_en', 'created_at')
    list_filter = ('accion', 'empresa', 'usada', 'created_at')
    search_fields = ('token', 'uid_dispositivo', 'empresa', 'callback_url')
    readonly_fields = ('token', 'created_at', 'usada_en', 'telegram_message_id')
    
    fieldsets = [
        ('Token y Callback', {
            'fields': ['token', 'callback_url']
        }),
        ('Información', {
            'fields': ['empresa', 'accion', 'uid_dispositivo', 'telegram_user_id']
        }),
        ('Estado', {
            'fields': ['usada', 'usada_en', 'expira_en']
        }),
        ('Telegram', {
            'fields': ['telegram_message_id']
        }),
        ('Metadatos', {
            'fields': ['metadata'],
            'classes': ['collapse']
        }),
        ('Auditoría', {
            'fields': ['created_at'],
            'classes': ['collapse']
        }),
    ]
    
    def token_short(self, obj):
        return f"{obj.token[:8]}..."
    token_short.short_description = 'Token'
    
    def uid_short(self, obj):
        if obj.uid_dispositivo:
            return f"{obj.uid_dispositivo[:16]}..."
        return '-'
    uid_short.short_description = 'UID'
