# push_telegram/admin.py

from django.contrib import admin
from .models import TelegramEventType, TelegramSubscription, TelegramNotificationLog, TelegramAutorizacion


@admin.register(TelegramEventType)
class TelegramEventTypeAdmin(admin.ModelAdmin):
    list_display = ('code', 'nombre', 'activo', 'created_at')
    list_filter = ('activo',)
    search_fields = ('code', 'nombre')
    readonly_fields = ('created_at',)


@admin.register(TelegramSubscription)
class TelegramSubscriptionAdmin(admin.ModelAdmin):
    list_display = ('telegram_user_id', 'nombre_usuario', 'event_type', 'activo', 'created_at')
    list_filter = ('activo', 'event_type')
    search_fields = ('telegram_user_id', 'nombre_usuario')
    readonly_fields = ('created_at', 'updated_at')
    
    fieldsets = (
        ('Información del Usuario', {
            'fields': ('telegram_user_id', 'nombre_usuario')
        }),
        ('Suscripción', {
            'fields': ('event_type', 'activo')
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )


@admin.register(TelegramNotificationLog)
class TelegramNotificationLogAdmin(admin.ModelAdmin):
    list_display = ('event_type', 'telegram_user_id', 'enviado', 'created_at')
    list_filter = ('enviado', 'event_type', 'created_at')
    search_fields = ('telegram_user_id', 'mensaje')
    readonly_fields = ('event_type', 'telegram_user_id', 'mensaje', 'enviado', 'error', 'metadata', 'created_at')
    
    def has_add_permission(self, request):
        return False
    
    def has_change_permission(self, request, obj=None):
        return False


@admin.register(TelegramAutorizacion)
class TelegramAutorizacionAdmin(admin.ModelAdmin):
    list_display = ('token_corto', 'accion', 'uid_corto', 'telegram_user_id', 'estado', 'expira_en', 'created_at')
    list_filter = ('usada', 'expirada', 'accion', 'created_at')
    search_fields = ('token', 'uid_dispositivo', 'telegram_user_id')
    readonly_fields = ('token', 'uid_dispositivo', 'telegram_message_id', 'telegram_user_id', 
                      'accion', 'usada', 'expirada', 'usada_en', 'created_at')
    
    def token_corto(self, obj):
        return f"{obj.token[:8]}..."
    token_corto.short_description = 'Token'
    
    def uid_corto(self, obj):
        return f"{obj.uid_dispositivo[:16]}..."
    uid_corto.short_description = 'UID Dispositivo'
    
    def estado(self, obj):
        if obj.usada:
            return "✅ Usada"
        if obj.expirada or not obj.is_valida():
            return "⏰ Expirada"
        return "🔓 Activa"
    estado.short_description = 'Estado'
    
    def has_add_permission(self, request):
        return False
    
    def has_change_permission(self, request, obj=None):
        return False
