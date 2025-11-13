from django.contrib import admin
from django.core.cache import cache
from .models import TelegramAutorizacion, TPVInstance, TelegramUser, Instruction, PushMessage, AllowedOrigin


@admin.register(AllowedOrigin)
class AllowedOriginAdmin(admin.ModelAdmin):
    list_display = ('origin', 'description', 'is_active', 'created_at')
    list_filter = ('is_active', 'created_at')
    search_fields = ('origin', 'description')
    readonly_fields = ('created_at', 'updated_at')
    
    def save_model(self, request, obj, form, change):
        """Limpiar cache al guardar"""
        super().save_model(request, obj, form, change)
        cache.delete('allowed_cors_origins')
    
    def delete_model(self, request, obj):
        """Limpiar cache al eliminar"""
        super().delete_model(request, obj)
        cache.delete('allowed_cors_origins')


@admin.register(TelegramAutorizacion)
class TelegramAutorizacionAdmin(admin.ModelAdmin):
    list_display = ('token_short', 'uid_short', 'accion', 'empresa', 'usada', 'expirada', 'created_at')
    list_filter = ('accion', 'empresa', 'usada', 'expirada')
    search_fields = ('token', 'uid_dispositivo', 'empresa')
    readonly_fields = ('token', 'created_at', 'usada_en')
    
    def token_short(self, obj):
        return f"{obj.token[:8]}..."
    token_short.short_description = 'Token'
    
    def uid_short(self, obj):
        return f"{obj.uid_dispositivo[:16]}..."
    uid_short.short_description = 'UID'


@admin.register(TPVInstance)
class TPVInstanceAdmin(admin.ModelAdmin):
    list_display = ['name', 'is_active', 'is_online', 'user_count', 'last_heartbeat', 'created_at']
    list_filter = ['is_active', 'created_at']
    search_fields = ['name', 'api_key']
    readonly_fields = ['id', 'created_at', 'updated_at', 'last_heartbeat']
    
    fieldsets = [
        ('Información Básica', {
            'fields': ['id', 'name', 'api_key', 'endpoint_url']
        }),
        ('Configuración', {
            'fields': ['is_active', 'max_users', 'version']
        }),
        ('Estado', {
            'fields': ['last_heartbeat', 'created_at', 'updated_at']
        }),
        ('Metadatos', {
            'fields': ['metadata'],
            'classes': ['collapse']
        }),
    ]
    
    def user_count(self, obj):
        return obj.users.count()
    user_count.short_description = 'Usuarios'
    
    def is_online(self, obj):
        return obj.is_online()
    is_online.boolean = True
    is_online.short_description = 'Online'


@admin.register(TelegramUser)
class TelegramUserAdmin(admin.ModelAdmin):
    list_display = ['telegram_id', 'username', 'full_name', 'tpv_instance', 'is_active', 'last_interaction']
    list_filter = ['is_active', 'tpv_instance', 'created_at']
    search_fields = ['telegram_id', 'username', 'first_name', 'last_name']
    readonly_fields = ['created_at', 'updated_at', 'last_interaction']
    
    def full_name(self, obj):
        return f"{obj.first_name} {obj.last_name}".strip() or '-'
    full_name.short_description = 'Nombre'


@admin.register(Instruction)
class InstructionAdmin(admin.ModelAdmin):
    list_display = ['command', 'tpv_instance', 'telegram_user', 'status', 'priority', 'created_at']
    list_filter = ['status', 'tpv_instance', 'created_at']
    search_fields = ['command', 'telegram_user__username']
    readonly_fields = ['id', 'created_at', 'sent_at', 'delivered_at', 'completed_at']
    
    fieldsets = [
        ('Información', {
            'fields': ['id', 'tpv_instance', 'telegram_user', 'command', 'payload']
        }),
        ('Estado', {
            'fields': ['status', 'priority', 'expires_at']
        }),
        ('Tiempos', {
            'fields': ['created_at', 'sent_at', 'delivered_at', 'completed_at']
        }),
        ('Respuesta', {
            'fields': ['response', 'error_message'],
            'classes': ['collapse']
        }),
        ('Reintentos', {
            'fields': ['retry_count', 'max_retries'],
            'classes': ['collapse']
        }),
    ]


@admin.register(PushMessage)
class PushMessageAdmin(admin.ModelAdmin):
    list_display = ['message_type', 'telegram_user', 'tpv_instance', 'status', 'created_at', 'sent_at']
    list_filter = ['status', 'message_type', 'tpv_instance', 'created_at']
    search_fields = ['telegram_user__username', 'text']
    readonly_fields = ['id', 'created_at', 'sent_at', 'telegram_message_id']
    
    fieldsets = [
        ('Información', {
            'fields': ['id', 'tpv_instance', 'telegram_user', 'message_type']
        }),
        ('Contenido', {
            'fields': ['text', 'data']
        }),
        ('Estado', {
            'fields': ['status', 'created_at', 'sent_at', 'telegram_message_id']
        }),
        ('Error', {
            'fields': ['error_message', 'retry_count', 'max_retries'],
            'classes': ['collapse']
        }),
    ]
