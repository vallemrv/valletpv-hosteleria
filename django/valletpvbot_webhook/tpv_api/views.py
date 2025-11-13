"""
Vistas de la API para TPVs.
"""

import logging
import secrets
from django.utils import timezone
from rest_framework import status
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.response import Response
from rest_framework.permissions import AllowAny, IsAuthenticated

from webhook.models import TPVInstance, Instruction, PushMessage, TelegramUser
from webhook.telegram_service import TelegramService
from webhook.router import TPVRouter
from .authentication import TPVApiKeyAuthentication
from .serializers import (
    TPVInstanceSerializer,
    TPVRegistrationSerializer,
    InstructionSerializer,
    InstructionUpdateSerializer,
    PushMessageCreateSerializer,
    TelegramUserSerializer
)

logger = logging.getLogger(__name__)


@api_view(['POST'])
@permission_classes([AllowAny])
def register_tpv(request):
    """
    Registra una nueva instancia TPV y genera un API Key único.
    
    POST /api/tpv/register/
    {
        "name": "TPV Restaurante Principal",
        "endpoint_url": "http://localhost:8001",
        "max_users": 100,
        "version": "1.0.0",
        "metadata": {}
    }
    """
    serializer = TPVRegistrationSerializer(data=request.data)
    
    if not serializer.is_valid():
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    # Generar API Key único
    api_key = secrets.token_urlsafe(32)
    
    # Crear instancia TPV
    tpv = TPVInstance.objects.create(
        name=serializer.validated_data['name'],
        endpoint_url=serializer.validated_data['endpoint_url'],
        api_key=api_key,
        max_users=serializer.validated_data.get('max_users', 100),
        version=serializer.validated_data.get('version', ''),
        metadata=serializer.validated_data.get('metadata', {}),
        is_active=True
    )
    
    logger.info(f"TPV registrado: {tpv.name} (ID: {tpv.id})")
    
    return Response({
        'id': str(tpv.id),
        'name': tpv.name,
        'api_key': api_key,
        'message': 'TPV registrado correctamente. Guarda el API Key de forma segura.'
    }, status=status.HTTP_201_CREATED)


@api_view(['GET'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def tpv_info(request):
    """
    Obtiene información del TPV autenticado.
    
    GET /api/tpv/info/
    Headers: Authorization: ApiKey <api_key>
    """
    tpv = request.user  # El TPV autenticado
    
    # Contar usuarios activos
    user_count = tpv.users.filter(is_active=True).count()
    
    serializer = TPVInstanceSerializer(tpv)
    data = serializer.data
    data['user_count'] = user_count
    data['is_online'] = tpv.is_online()
    
    return Response(data)


@api_view(['POST'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def heartbeat(request):
    """
    Endpoint de heartbeat para que el TPV notifique que está activo.
    
    POST /api/tpv/heartbeat/
    Headers: Authorization: ApiKey <api_key>
    """
    tpv = request.user
    tpv.update_heartbeat()
    
    return Response({
        'status': 'ok',
        'timestamp': timezone.now().isoformat()
    })


@api_view(['GET'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def get_instructions(request):
    """
    Obtiene instrucciones pendientes para el TPV.
    
    GET /api/tpv/instructions/?status=pending&limit=10
    Headers: Authorization: ApiKey <api_key>
    """
    tpv = request.user
    
    # Parámetros de filtro
    status_filter = request.query_params.get('status', 'pending')
    limit = int(request.query_params.get('limit', 10))
    
    # Obtener instrucciones
    instructions = Instruction.objects.filter(
        tpv_instance=tpv,
        status=status_filter
    ).select_related('telegram_user').order_by('priority', 'created_at')[:limit]
    
    # Marcar como enviadas si están pendientes
    if status_filter == 'pending':
        instruction_ids = [inst.id for inst in instructions]
        Instruction.objects.filter(id__in=instruction_ids).update(
            status='sent',
            sent_at=timezone.now()
        )
    
    serializer = InstructionSerializer(instructions, many=True)
    
    return Response({
        'count': len(serializer.data),
        'instructions': serializer.data
    })


@api_view(['PUT', 'PATCH'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def update_instruction(request, instruction_id):
    """
    Actualiza el estado de una instrucción.
    
    PUT /api/tpv/instructions/<instruction_id>/
    Headers: Authorization: ApiKey <api_key>
    {
        "status": "completed",
        "response": {"result": "success"}
    }
    """
    tpv = request.user
    
    try:
        instruction = Instruction.objects.get(
            id=instruction_id,
            tpv_instance=tpv
        )
    except Instruction.DoesNotExist:
        return Response(
            {'error': 'Instrucción no encontrada'},
            status=status.HTTP_404_NOT_FOUND
        )
    
    serializer = InstructionUpdateSerializer(data=request.data)
    
    if not serializer.is_valid():
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    # Actualizar estado
    new_status = serializer.validated_data['status']
    
    if new_status == 'delivered':
        instruction.mark_as_delivered()
    elif new_status == 'completed':
        response_data = serializer.validated_data.get('response')
        instruction.mark_as_completed(response=response_data)
    elif new_status == 'failed':
        error_msg = serializer.validated_data.get('error_message', '')
        instruction.mark_as_failed(error_message=error_msg)
    elif new_status == 'processing':
        instruction.status = 'processing'
        instruction.save(update_fields=['status'])
    
    logger.info(f"Instrucción {instruction_id} actualizada a estado: {new_status}")
    
    return Response({
        'status': 'ok',
        'instruction_id': str(instruction.id),
        'new_status': instruction.status
    })


@api_view(['POST'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def send_push_message(request):
    """
    Envía un mensaje push a un usuario de Telegram.
    
    POST /api/tpv/push/
    Headers: Authorization: ApiKey <api_key>
    {
        "telegram_user_id": 123456789,
        "message_type": "text",
        "text": "Tu pedido está listo",
        "data": {}
    }
    """
    tpv = request.user
    
    serializer = PushMessageCreateSerializer(data=request.data)
    
    if not serializer.is_valid():
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    telegram_user_id = serializer.validated_data['telegram_user_id']
    
    # Verificar que el usuario exista y esté asignado a este TPV
    try:
        telegram_user = TelegramUser.objects.get(
            telegram_id=telegram_user_id,
            tpv_instance=tpv,
            is_active=True
        )
    except TelegramUser.DoesNotExist:
        return Response(
            {'error': 'Usuario no encontrado o no asignado a este TPV'},
            status=status.HTTP_404_NOT_FOUND
        )
    
    # Crear mensaje push
    push_message = PushMessage.objects.create(
        tpv_instance=tpv,
        telegram_user=telegram_user,
        message_type=serializer.validated_data['message_type'],
        text=serializer.validated_data.get('text', ''),
        data=serializer.validated_data.get('data', {}),
        status='pending'
    )
    
    # Enviar mensaje inmediatamente
    telegram_service = TelegramService()
    message_type = push_message.message_type
    
    try:
        if message_type == 'text':
            result = telegram_service.send_message(
                chat_id=telegram_user.telegram_id,
                text=push_message.text
            )
        elif message_type == 'photo':
            result = telegram_service.send_photo(
                chat_id=telegram_user.telegram_id,
                photo_url=push_message.data.get('photo_url'),
                caption=push_message.text
            )
        elif message_type == 'document':
            result = telegram_service.send_document(
                chat_id=telegram_user.telegram_id,
                document_url=push_message.data.get('document_url'),
                caption=push_message.text
            )
        else:
            result = None
        
        if result:
            telegram_message_id = result.get('message_id')
            push_message.mark_as_sent(telegram_message_id=telegram_message_id)
            
            logger.info(f"Mensaje push enviado: {push_message.id}")
            
            return Response({
                'status': 'sent',
                'push_message_id': str(push_message.id),
                'telegram_message_id': telegram_message_id
            }, status=status.HTTP_201_CREATED)
        else:
            push_message.mark_as_failed(error_message='Error al enviar mensaje a Telegram')
            
            return Response({
                'error': 'Error al enviar mensaje',
                'push_message_id': str(push_message.id)
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
            
    except Exception as e:
        logger.error(f"Error al enviar push message: {str(e)}", exc_info=True)
        push_message.mark_as_failed(error_message=str(e))
        
        return Response({
            'error': 'Error interno al enviar mensaje',
            'details': str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['GET'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def get_users(request):
    """
    Obtiene la lista de usuarios asignados al TPV.
    
    GET /api/tpv/users/?is_active=true
    Headers: Authorization: ApiKey <api_key>
    """
    tpv = request.user
    
    # Filtros
    is_active = request.query_params.get('is_active', 'true').lower() == 'true'
    
    users = TelegramUser.objects.filter(
        tpv_instance=tpv,
        is_active=is_active
    ).order_by('-last_interaction')
    
    serializer = TelegramUserSerializer(users, many=True)
    
    return Response({
        'count': len(serializer.data),
        'users': serializer.data
    })


@api_view(['GET'])
@authentication_classes([TPVApiKeyAuthentication])
@permission_classes([IsAuthenticated])
def stats(request):
    """
    Obtiene estadísticas del TPV.
    
    GET /api/tpv/stats/
    Headers: Authorization: ApiKey <api_key>
    """
    tpv = request.user
    
    # Contar usuarios
    total_users = tpv.users.filter(is_active=True).count()
    
    # Contar instrucciones por estado
    instructions_pending = tpv.instructions.filter(status='pending').count()
    instructions_processing = tpv.instructions.filter(status='processing').count()
    instructions_completed = tpv.instructions.filter(status='completed').count()
    instructions_failed = tpv.instructions.filter(status='failed').count()
    
    # Contar mensajes push
    push_pending = tpv.push_messages.filter(status='pending').count()
    push_sent = tpv.push_messages.filter(status='sent').count()
    push_failed = tpv.push_messages.filter(status='failed').count()
    
    return Response({
        'tpv_id': str(tpv.id),
        'tpv_name': tpv.name,
        'is_online': tpv.is_online(),
        'last_heartbeat': tpv.last_heartbeat,
        'users': {
            'total': total_users,
            'max': tpv.max_users,
            'available_slots': tpv.max_users - total_users
        },
        'instructions': {
            'pending': instructions_pending,
            'processing': instructions_processing,
            'completed': instructions_completed,
            'failed': instructions_failed,
            'total': instructions_pending + instructions_processing + instructions_completed + instructions_failed
        },
        'push_messages': {
            'pending': push_pending,
            'sent': push_sent,
            'failed': push_failed,
            'total': push_pending + push_sent + push_failed
        }
    })
