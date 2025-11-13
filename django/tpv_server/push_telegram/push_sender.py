# push_telegram/push_sender.py
# Sistema de envío de notificaciones push a Telegram vía webhook

import logging
import requests
import uuid
from datetime import timedelta
from django.conf import settings
from django.utils import timezone
from .models import TelegramEventType, TelegramSubscription, TelegramNotificationLog
from gestion.tools.config_logs import configurar_logging

logger = configurar_logging("push_telegram")


def enviar_push_telegram(event_code: str, mensaje: str, metadata: dict = None):
    """
    Enviar notificación push a todos los usuarios suscritos a un evento
    
    Args:
        event_code: Código del evento (ej: 'nuevo_dispositivo')
        mensaje: Mensaje a enviar (puede incluir HTML)
        metadata: Datos adicionales del evento (opcional)
    
    Returns:
        int: Número de notificaciones enviadas exitosamente
    """
    try:
        # 1. Buscar el evento
        try:
            event_type = TelegramEventType.objects.get(code=event_code, activo=True)
        except TelegramEventType.DoesNotExist:
            logger.warning(f"Evento '{event_code}' no existe o está inactivo")
            return 0
        
        # 2. Buscar suscriptores activos
        subscriptions = TelegramSubscription.objects.filter(
            event_type=event_type,
            activo=True
        )
        
        if not subscriptions.exists():
            logger.info(f"No hay suscriptores para el evento '{event_code}'")
            return 0
        
        # 3. Obtener token del bot
        telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
        bot_token = telegram_config.get('TOKEN', '')
        
        if not bot_token or bot_token == 'TU_BOT_TOKEN_AQUI':
            logger.error("Token de Telegram no configurado")
            return 0
        
        # 4. Enviar a cada suscriptor
        enviados = 0
        for subscription in subscriptions:
            enviado = _enviar_mensaje_telegram(
                bot_token=bot_token,
                chat_id=subscription.telegram_user_id,
                mensaje=mensaje,
                event_type=event_type,
                metadata=metadata or {}
            )
            if enviado:
                enviados += 1
        
        logger.info(f"Evento '{event_code}': {enviados}/{subscriptions.count()} notificaciones enviadas")
        return enviados
        
    except Exception as e:
        logger.error(f"Error enviando push para evento '{event_code}': {e}")
        return 0


def _enviar_mensaje_telegram(bot_token: str, chat_id: int, mensaje: str, 
                             event_type: TelegramEventType, metadata: dict,
                             reply_markup: dict = None):
    """
    Enviar mensaje a un usuario específico de Telegram y registrar en log
    """
    log = TelegramNotificationLog(
        event_type=event_type,
        telegram_user_id=chat_id,
        mensaje=mensaje,
        metadata=metadata
    )
    
    try:
        # Enviar vía API de Telegram
        url = f"https://api.telegram.org/bot{bot_token}/sendMessage"
        data = {
            'chat_id': chat_id,
            'text': mensaje,
            'parse_mode': 'HTML'
        }
        
        # Agregar botones si se proporcionan
        if reply_markup:
            data['reply_markup'] = reply_markup
        
        response = requests.post(url, json=data, timeout=10)
        
        if response.status_code == 200:
            log.enviado = True
            log.save()
            logger.debug(f"Mensaje enviado a {chat_id}")
            return True
        else:
            error_msg = response.json().get('description', 'Error desconocido')
            log.error = f"HTTP {response.status_code}: {error_msg}"
            log.save()
            logger.warning(f"Error enviando a {chat_id}: {error_msg}")
            return False
            
    except Exception as e:
        log.error = str(e)
        log.save()
        logger.error(f"Excepción enviando a {chat_id}: {e}")
        return False


# Función de conveniencia para el evento de nuevo dispositivo
def notificar_nuevo_dispositivo(uid: str, descripcion: str = None):
    """
    Notificar sobre un nuevo dispositivo detectado con botones de acción.
    Envía directamente a Telegram con URLs en callback_data.
    """
    # Obtener suscriptores del evento
    try:
        event_type = TelegramEventType.objects.get(code='nuevo_dispositivo', activo=True)
    except TelegramEventType.DoesNotExist:
        logger.warning("Evento 'nuevo_dispositivo' no existe")
        return 0
    
    subscriptions = TelegramSubscription.objects.filter(
        event_type=event_type,
        activo=True
    )
    
    if not subscriptions.exists():
        logger.info("No hay suscriptores para nuevo_dispositivo")
        return 0
    
    # Obtener configuración
    telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
    bot_token = telegram_config.get('TOKEN', '')
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    base_url = getattr(settings, 'BASE_URL', 'https://tpvtest.valletpv.es')
    
    if not bot_token:
        logger.error("Token de Telegram no configurado")
        return 0
    
    # Enviar a cada suscriptor
    enviados = 0
    for subscription in subscriptions:
        # Crear tokens de autorización temporal (expira en 10 minutos)
        token_activar = str(uuid.uuid4())
        token_desactivar = str(uuid.uuid4())
        expira_en = timezone.now() + timedelta(minutes=10)
        
        # Mensaje
        mensaje = f"""
🆕 <b>Nuevo Dispositivo Detectado</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

⚠️ El dispositivo está <b>INACTIVO</b> por seguridad.
¿Deseas activarlo o mantenerlo desactivado?

⏱️ Esta autorización expira en 10 minutos.
        """.strip()
        
        # Callback data con formato corto: accion|token (máximo 64 bytes)
        callback_activar = f"activate|{token_activar}"
        callback_desactivar = f"deactivate|{token_desactivar}"
        
        # Crear botones inline con callback_data corto
        keyboard = {
            'inline_keyboard': [
                [
                    {'text': '✅ Activar', 'callback_data': callback_activar},
                    {'text': '⏸️ Desactivar', 'callback_data': callback_desactivar}
                ]
            ]
        }
        
        # Log de la notificación
        log = TelegramNotificationLog(
            event_type=event_type,
            telegram_user_id=subscription.telegram_user_id,
            mensaje=mensaje,
            metadata={'uid': uid, 'descripcion': descripcion, 'empresa': empresa}
        )
        
        try:
            # Enviar mensaje directamente a Telegram
            url = f"https://api.telegram.org/bot{bot_token}/sendMessage"
            data = {
                'chat_id': subscription.telegram_user_id,
                'text': mensaje,
                'parse_mode': 'HTML',
                'reply_markup': keyboard
            }
            
            response = requests.post(url, json=data, timeout=10)
            
            if response.status_code == 200:
                result = response.json()['result']
                message_id = result['message_id']
                
                log.enviado = True
                log.save()
                
                # Guardar autorizaciones en BD local con message_id
                from .models import TelegramAutorizacion
                
                TelegramAutorizacion.objects.create(
                    token=token_activar,
                    uid_dispositivo=uid,
                    telegram_message_id=message_id,
                    telegram_user_id=subscription.telegram_user_id,
                    accion='activate_device',
                    empresa=empresa,
                    expira_en=expira_en
                )
                
                TelegramAutorizacion.objects.create(
                    token=token_desactivar,
                    uid_dispositivo=uid,
                    telegram_message_id=message_id,
                    telegram_user_id=subscription.telegram_user_id,
                    accion='deactivate_device',
                    empresa=empresa,
                    expira_en=expira_en
                )
                
                logger.info(f"Mensaje enviado a {subscription.telegram_user_id}")
                enviados += 1
            else:
                error_msg = response.json().get('description', 'Error desconocido')
                log.error = f"HTTP {response.status_code}: {error_msg}"
                log.save()
                logger.warning(f"Error enviando a {subscription.telegram_user_id}: {error_msg}")
                
        except Exception as e:
            log.error = str(e)
            log.save()
            logger.error(f"Excepción enviando a {subscription.telegram_user_id}: {e}")
    
    logger.info(f"Evento 'nuevo_dispositivo': {enviados}/{subscriptions.count()} notificaciones enviadas")
    return enviados


def editar_mensaje_dispositivo(telegram_user_id: int, message_id: int, uid: str, descripcion: str, accion: str, ya_estaba: bool = False):
    """
    Editar el mensaje original eliminando los botones y mostrando el resultado de la acción.
    Usa la API de Telegram directamente (no necesita pasar por webhook).
    
    Args:
        telegram_user_id: ID del usuario de Telegram
        message_id: ID del mensaje a editar
        uid: UID del dispositivo
        descripcion: Descripción del dispositivo
        accion: 'activado' o 'desactivado'
        ya_estaba: Si el dispositivo ya estaba en ese estado
    """
    # Obtener token del bot
    telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
    bot_token = telegram_config.get('TOKEN', '')
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    
    if not bot_token:
        logger.error("Token de Telegram no configurado para editar mensaje")
        return False
    
    # Determinar emoji y mensaje según la acción
    if accion == 'activado':
        emoji = '✅'
        estado = 'ACTIVO'
        verbo = 'activado'
    else:  # desactivado
        emoji = '⏸️'
        estado = 'INACTIVO'
        verbo = 'desactivado'
    
    # Construir mensaje editado
    if ya_estaba:
        mensaje = f"""
🆕 <b>Nuevo Dispositivo Detectado</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

{emoji} <b>El dispositivo ya estaba {estado}</b>
        """.strip()
    else:
        mensaje = f"""
🆕 <b>Nuevo Dispositivo Detectado</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

{emoji} <b>Dispositivo {verbo.upper()}</b>
🔄 <b>Estado actual:</b> {estado}
        """.strip()
    
    # Editar mensaje eliminando botones
    try:
        url = f"https://api.telegram.org/bot{bot_token}/editMessageText"
        data = {
            'chat_id': telegram_user_id,
            'message_id': message_id,
            'text': mensaje,
            'parse_mode': 'HTML'
        }
        
        response = requests.post(url, json=data, timeout=10)
        
        if response.status_code == 200:
            logger.info(f"Mensaje editado para {telegram_user_id}: dispositivo {verbo}")
            return True
        else:
            error_msg = response.json().get('description', 'Error desconocido')
            logger.warning(f"Error editando mensaje para {telegram_user_id}: {error_msg}")
            return False
            
    except Exception as e:
        logger.error(f"Excepción editando mensaje para {telegram_user_id}: {e}")
        return False
