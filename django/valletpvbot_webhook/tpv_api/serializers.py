"""
Serializers para la API de TPVs.
"""

from rest_framework import serializers
from webhook.models import TPVInstance, Instruction, PushMessage, TelegramUser


class TPVInstanceSerializer(serializers.ModelSerializer):
    """Serializer para instancias TPV."""
    
    user_count = serializers.IntegerField(read_only=True)
    is_online = serializers.BooleanField(read_only=True)
    
    class Meta:
        model = TPVInstance
        fields = [
            'id', 'name', 'endpoint_url', 'is_active', 'max_users',
            'version', 'metadata', 'created_at', 'last_heartbeat',
            'user_count', 'is_online'
        ]
        read_only_fields = ['id', 'created_at', 'last_heartbeat']


class TPVRegistrationSerializer(serializers.Serializer):
    """Serializer para registrar un nuevo TPV."""
    
    name = serializers.CharField(max_length=200)
    endpoint_url = serializers.URLField()
    max_users = serializers.IntegerField(default=100, min_value=1)
    version = serializers.CharField(max_length=50, required=False, allow_blank=True)
    metadata = serializers.JSONField(required=False, default=dict)


class InstructionSerializer(serializers.ModelSerializer):
    """Serializer para instrucciones."""
    
    telegram_user_id = serializers.IntegerField(source='telegram_user.telegram_id', read_only=True)
    telegram_username = serializers.CharField(source='telegram_user.username', read_only=True)
    
    class Meta:
        model = Instruction
        fields = [
            'id', 'command', 'payload', 'status', 'priority',
            'telegram_user_id', 'telegram_username',
            'created_at', 'sent_at', 'delivered_at', 'completed_at',
            'expires_at', 'response', 'error_message', 'retry_count'
        ]
        read_only_fields = [
            'id', 'created_at', 'sent_at', 'delivered_at', 
            'completed_at', 'retry_count'
        ]


class InstructionUpdateSerializer(serializers.Serializer):
    """Serializer para actualizar el estado de una instrucción."""
    
    status = serializers.ChoiceField(
        choices=['delivered', 'processing', 'completed', 'failed']
    )
    response = serializers.JSONField(required=False)
    error_message = serializers.CharField(required=False, allow_blank=True)


class PushMessageSerializer(serializers.ModelSerializer):
    """Serializer para mensajes push."""
    
    class Meta:
        model = PushMessage
        fields = [
            'id', 'message_type', 'text', 'data', 'status',
            'created_at', 'sent_at', 'telegram_message_id', 'error_message'
        ]
        read_only_fields = [
            'id', 'status', 'created_at', 'sent_at', 
            'telegram_message_id', 'error_message'
        ]


class PushMessageCreateSerializer(serializers.Serializer):
    """Serializer para crear mensajes push desde el TPV."""
    
    telegram_user_id = serializers.IntegerField(
        help_text="ID de Telegram del usuario destinatario"
    )
    message_type = serializers.ChoiceField(
        choices=['text', 'photo', 'document', 'location'],
        default='text'
    )
    text = serializers.CharField(
        required=False, 
        allow_blank=True,
        help_text="Texto del mensaje o caption"
    )
    data = serializers.JSONField(
        required=False, 
        default=dict,
        help_text="Datos adicionales (photo_url, document_url, etc)"
    )
    
    def validate(self, attrs):
        """Valida que los datos sean consistentes con el tipo de mensaje."""
        message_type = attrs.get('message_type')
        text = attrs.get('text', '')
        data = attrs.get('data', {})
        
        if message_type == 'text' and not text:
            raise serializers.ValidationError("El texto es requerido para mensajes de tipo 'text'")
        
        if message_type == 'photo' and 'photo_url' not in data:
            raise serializers.ValidationError("Se requiere 'photo_url' en data para mensajes de tipo 'photo'")
        
        if message_type == 'document' and 'document_url' not in data:
            raise serializers.ValidationError("Se requiere 'document_url' en data para mensajes de tipo 'document'")
        
        return attrs


class TelegramUserSerializer(serializers.ModelSerializer):
    """Serializer para usuarios de Telegram."""
    
    class Meta:
        model = TelegramUser
        fields = [
            'telegram_id', 'username', 'first_name', 'last_name',
            'is_active', 'language_code', 'created_at', 'last_interaction'
        ]
        read_only_fields = fields
