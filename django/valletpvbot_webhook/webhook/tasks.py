"""
Tareas asíncronas de Celery para el webhook.
"""

from celery import shared_task
from django.utils import timezone
import logging

logger = logging.getLogger(__name__)


@shared_task
def process_pending_push_messages():
    """
    Procesa mensajes push pendientes.
    Se ejecuta periódicamente para enviar mensajes que fallaron.
    """
    from webhook.models import PushMessage
    from webhook.telegram_service import TelegramService
    
    telegram_service = TelegramService()
    
    # Obtener mensajes pendientes que no hayan excedido el límite de reintentos
    messages = PushMessage.objects.filter(
        status='pending'
    ).filter(
        retry_count__lt=models.F('max_retries')
    )[:50]
    
    processed = 0
    
    for message in messages:
        try:
            if message.message_type == 'text':
                result = telegram_service.send_message(
                    chat_id=message.telegram_user.telegram_id,
                    text=message.text
                )
            elif message.message_type == 'photo':
                result = telegram_service.send_photo(
                    chat_id=message.telegram_user.telegram_id,
                    photo_url=message.data.get('photo_url'),
                    caption=message.text
                )
            elif message.message_type == 'document':
                result = telegram_service.send_document(
                    chat_id=message.telegram_user.telegram_id,
                    document_url=message.data.get('document_url'),
                    caption=message.text
                )
            else:
                result = None
            
            if result:
                message.mark_as_sent(telegram_message_id=result.get('message_id'))
                processed += 1
            else:
                message.mark_as_failed('Error al enviar')
                
        except Exception as e:
            logger.error(f"Error procesando mensaje push {message.id}: {str(e)}")
            message.mark_as_failed(str(e))
    
    logger.info(f"Procesados {processed} mensajes push pendientes")
    return processed


@shared_task
def check_tpv_health():
    """
    Verifica el estado de salud de los TPVs.
    Marca como inactivos los que no han enviado heartbeat recientemente.
    """
    from webhook.models import TPVInstance
    from datetime import timedelta
    
    threshold = timezone.now() - timedelta(minutes=5)
    
    # TPVs que deberían estar online pero no han enviado heartbeat
    inactive_tpvs = TPVInstance.objects.filter(
        is_active=True,
        last_heartbeat__lt=threshold
    )
    
    count = inactive_tpvs.count()
    
    for tpv in inactive_tpvs:
        logger.warning(f"TPV {tpv.name} sin heartbeat desde {tpv.last_heartbeat}")
        # Opcionalmente, reasignar usuarios o marcar como inactivo
    
    return count


@shared_task
def cleanup_old_instructions():
    """
    Limpia instrucciones antiguas completadas o fallidas.
    """
    from webhook.models import Instruction
    from datetime import timedelta
    
    threshold = timezone.now() - timedelta(days=7)
    
    deleted_count, _ = Instruction.objects.filter(
        status__in=['completed', 'failed'],
        created_at__lt=threshold
    ).delete()
    
    logger.info(f"Eliminadas {deleted_count} instrucciones antiguas")
    return deleted_count


# Importar models aquí para evitar circular imports
from django.db import models
