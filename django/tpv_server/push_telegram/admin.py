# push_telegram/admin.py

from django.contrib import admin
from .models import TelegramEventType, TelegramSubscription, TelegramNotificationLog, TelegramUser


@admin.register(TelegramUser)
class TelegramUserAdmin(admin.ModelAdmin):
    list_display = ('nombre', 'telegram_user_id', 'descripcion', 'activo', 'created_at')
    list_filter = ('activo',)
    search_fields = ('nombre', 'telegram_user_id', 'descripcion')
    readonly_fields = ('created_at', 'updated_at')


@admin.register(TelegramEventType)
class TelegramEventTypeAdmin(admin.ModelAdmin):
    list_display = ('code', 'nombre', 'activo', 'created_at')
    list_filter = ('activo',)
    search_fields = ('code', 'nombre')
    readonly_fields = ('created_at',)


@admin.register(TelegramSubscription)
class TelegramSubscriptionAdmin(admin.ModelAdmin):
    list_display = ('usuario', 'event_type', 'filtros_display', 'activo', 'created_at')
    list_filter = ('activo', 'event_type', 'usuario')
    search_fields = ('usuario__nombre', 'usuario__telegram_user_id')
    readonly_fields = ('created_at', 'updated_at')
    
    fieldsets = (
        ('Información del Usuario', {
            'fields': ('usuario',)
        }),
        ('Suscripción', {
            'fields': ('event_type', 'activo')
        }),
        ('Filtros de Vigilancia', {
            'fields': ('filtros',),
            'description': 'Configure qué zonas vigilar. Ejemplo: {"zonas": [1, 2, 3]} para vigilar zonas con ID 1, 2 y 3. Dejar vacío para vigilar todas.'
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def filtros_display(self, obj):
        """Muestra los filtros de forma legible"""
        if not obj.filtros:
            return "Todas las zonas"
        if 'zonas' in obj.filtros:
            zonas_ids = obj.filtros['zonas']
            if isinstance(zonas_ids, list):
                from gestion.models import Zonas
                zonas = Zonas.objects.filter(pk__in=zonas_ids).values_list('nombre', flat=True)
                return f"Zonas: {', '.join(zonas)}"
            else:
                from gestion.models import Zonas
                zona = Zonas.objects.filter(pk=zonas_ids).first()
                return f"Zona: {zona.nombre if zona else zonas_ids}"
        return str(obj.filtros)
    filtros_display.short_description = 'Vigilando'


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
