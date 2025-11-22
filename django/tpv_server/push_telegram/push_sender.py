# push_telegram/push_sender.py
# Sistema de envío de notificaciones push a Telegram vía webhook

import logging
import requests
import uuid
from datetime import timedelta
from django.conf import settings
from django.utils import timezone
from .models import TelegramEventType, TelegramSubscription, TelegramNotificationLog, TelegramAutorizacion
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
        ).select_related('usuario')
        
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
                chat_id=subscription.usuario.telegram_user_id,
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
    Envía mensaje y tokens al webhook que actúa como router.
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
    ).select_related('usuario')
    
    if not subscriptions.exists():
        logger.info("No hay suscriptores para nuevo_dispositivo")
        return 0
    
    # Obtener configuración
    telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
    webhook_url = telegram_config.get('WEBHOOK_URL', '')
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    base_url = getattr(settings, 'BASE_URL', 'https://tpvtest.valletpv.es')
    
    if not webhook_url:
        logger.error("Webhook URL no configurada")
        return 0
    
    # Enviar a cada suscriptor
    enviados = 0
    for subscription in subscriptions:
        # Crear UN SOLO token (la acción viene en el callback_data del botón)
        token = str(uuid.uuid4())
        expira_en = timezone.now() + timedelta(minutes=10)
        
        # Guardar UNA autorización local (sin acción específica, se valida después)
        try:
            TelegramAutorizacion.objects.create(
                token=token,
                uid_dispositivo=uid,
                telegram_user_id=subscription.usuario.telegram_user_id,
                telegram_message_id=0,
                accion='dispositivo_action',  # Acción genérica
                empresa=empresa,
                expira_en=expira_en
            )
            logger.info(f"Token creado localmente para UID {uid}")
        except Exception as e:
            logger.error(f"Error creando token local: {e}")
            continue
        
        # Construir mensaje
        mensaje = f"""
🆕 <b>Nuevo Dispositivo Detectado</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

⚠️ El dispositivo está <b>INACTIVO</b> por seguridad.
¿Deseas activarlo o mantenerlo desactivado?

⏱️ Esta autorización expira en 10 minutos.
        """.strip()
        
        # Botones inline con callback_data: accion|token (mismo token, diferente acción)
        botones = [
            [
                {'text': '✅ Activar', 'callback_data': f"activate|{token}"},
                {'text': '⏸️ Desactivar', 'callback_data': f"deactivate|{token}"}
            ]
        ]
        
        # Log local
        log = TelegramNotificationLog(
            event_type=event_type,
            telegram_user_id=subscription.usuario.telegram_user_id,
            mensaje=mensaje,
            metadata={'uid': uid, 'descripcion': descripcion, 'empresa': empresa}
        )
        
        # Obtener API Key para autenticar con el webhook
        telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
        tpv_api_key = telegram_config.get('TPV_API_KEY', '')
        
        logger.info(f"Preparando envío al webhook: {webhook_url}/api/register_notification/")
        
        try:
            # Enviar al webhook (router simple)
            # El webhook recibe accion|token en callback_data y llama al callback_url con esos datos
            registro_url = f"{webhook_url}/api/register_notification/"
            headers = {'Authorization': f'Bearer {tpv_api_key}'}
            
            logger.info(f"Enviando POST a webhook con token {token[:8]}...")
            
            response = requests.post(registro_url, 
                headers=headers,
                json={
                    'token': token,
                    'callback_url': f"{base_url}/api/dispositivo/action",  # URL genérica que recibe accion
                    'telegram_user_id': subscription.usuario.telegram_user_id,
                    'mensaje': mensaje,
                    'botones': botones,
                    'expira_en': expira_en.isoformat(),
                    'empresa': empresa,
                    'uid_dispositivo': uid,
                    'metadata': {'descripcion': descripcion}
                }, 
                timeout=10
            )
            
            if response.status_code == 200:
                result = response.json()
                message_id = result.get('message_id')
                
                # Actualizar message_id en ambas autorizaciones locales (mismo token)
                if message_id:
                    TelegramAutorizacion.objects.filter(
                        token=token,
                        uid_dispositivo=uid
                    ).update(telegram_message_id=message_id)
                
                log.enviado = True
                log.save()
                logger.info(f"Notificación enviada vía webhook para {subscription.usuario.nombre}")
                enviados += 1
            else:
                error_msg = response.json().get('error', 'Error desconocido')
                log.error = f"Webhook error: {error_msg}"
                log.save()
                logger.warning(f"Error en webhook: {error_msg}")
                
        except Exception as e:
            log.error = str(e)
            log.save()
            logger.error(f"Excepción con webhook: {e}")
    
    logger.info(f"Evento 'nuevo_dispositivo': {enviados}/{subscriptions.count()} notificaciones enviadas")
    return enviados


def notificar_cambio_zona(mesa_origen_id: int, mesa_origen_nombre: str, 
                         mesa_destino_id: int, mesa_destino_nombre: str,
                         zona_destino_id: int, zona_destino_nombre: str,
                         camarero_nombre: str, hora_apertura: str, 
                         lineas_pedido: list, infmesa_id: str, tipo_cambio: str = "mesa_completa"):
    """
    Notificar cuando se cambia una mesa o líneas a una zona vigilada por un usuario.
    Cada usuario puede configurar qué zonas vigilar mediante filtros en su suscripción.
    
    Args:
        mesa_origen_id: ID de la mesa origen
        mesa_origen_nombre: Nombre de la mesa origen
        mesa_destino_id: ID de la mesa destino
        mesa_destino_nombre: Nombre de la mesa destino
        zona_destino_id: ID de la zona destino
        zona_destino_nombre: Nombre de la zona destino
        camarero_nombre: Nombre del camarero
        hora_apertura: Hora de apertura de la mesa
        lineas_pedido: Lista de líneas de pedido
        infmesa_id: ID de infmesa
        tipo_cambio: "mesa_completa" o "lineas_parciales"
    """
    try:
        event_type = TelegramEventType.objects.get(code='cambio_zona', activo=True)
    except TelegramEventType.DoesNotExist:
        logger.warning("Evento 'cambio_zona' no existe")
        return 0
    
    # Obtener todas las suscripciones activas
    subscriptions = TelegramSubscription.objects.filter(
        event_type=event_type,
        activo=True
    ).select_related('usuario')
    
    # Filtrar suscripciones que vigilen esta zona específica
    subscriptions_filtradas = [
        sub for sub in subscriptions 
        if sub.match_filtros(zona_id=zona_destino_id)
    ]
    
    if not subscriptions_filtradas:
        logger.info(f"No hay suscriptores vigilando la zona '{zona_destino_nombre}' (ID: {zona_destino_id})")
        return 0
    
    # Obtener configuración
    telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
    bot_token = telegram_config.get('TOKEN', '')
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    
    if not bot_token:
        logger.error("Token de Telegram no configurado")
        return 0
    
    # Construir resumen de líneas
    resumen_lineas = ""
    total = 0
    for i, linea in enumerate(lineas_pedido[:10], 1):  # Máximo 10 líneas para no saturar
        resumen_lineas += f"{i}. {linea['descripcion']} - {linea['precio']}€\n"
        total += linea['precio']
    
    if len(lineas_pedido) > 10:
        resumen_lineas += f"... y {len(lineas_pedido) - 10} líneas más\n"
    
    # Determinar emoji y texto según el tipo de cambio
    tipo_emoji = "🔄" if tipo_cambio == "mesa_completa" else "📦"
    tipo_texto = "Mesa Completa" if tipo_cambio == "mesa_completa" else "Líneas Parciales"
    
    # Enviar a cada suscriptor
    enviados = 0
    for subscription in subscriptions:
        # Crear tokens de autorización temporal (expira en 10 minutos)
        token_borrar = str(uuid.uuid4())
        token_mantener = str(uuid.uuid4())
        expira_en = timezone.now() + timedelta(minutes=10)
        
        # Mensaje personalizado según el tipo de cambio
        mensaje = f"""
{tipo_emoji} <b>Cambio a Zona: {zona_destino_nombre}</b>
<i>Tipo: {tipo_texto}</i>

📍 <b>De:</b> {mesa_origen_nombre} (ID: {mesa_origen_id})
📍 <b>A:</b> {mesa_destino_nombre} (ID: {mesa_destino_id})
🎯 <b>Zona:</b> {zona_destino_nombre} (ID: {zona_destino_id})
👤 <b>Camarero:</b> {camarero_nombre}
⏰ <b>Hora:</b> {hora_apertura}
🏢 <b>Empresa:</b> {empresa}

📋 <b>Líneas de pedido ({len(lineas_pedido)}):</b>
{resumen_lineas}
💰 <b>Total:</b> {total:.2f}€

❓ ¿Deseas borrar esta mesa o mantenerla?

⏱️ Esta autorización expira en 10 minutos.
        """.strip()
        
        # Callback data con formato corto
        callback_borrar = f"borrar_mesa|{token_borrar}"
        callback_mantener = f"mantener_mesa|{token_mantener}"
        
        # Crear botones inline
        keyboard = {
            'inline_keyboard': [
                [
                    {'text': '🗑️ Borrar Mesa', 'callback_data': callback_borrar},
                    {'text': '✅ Mantener Mesa', 'callback_data': callback_mantener}
                ]
            ]
        }
        
        # Log de la notificación
        log = TelegramNotificationLog(
            event_type=event_type,
            telegram_user_id=subscription.usuario.telegram_user_id,
            mensaje=mensaje,
            metadata={
                'mesa_origen_id': mesa_origen_id,
                'mesa_origen_nombre': mesa_origen_nombre,
                'mesa_destino_id': mesa_destino_id,
                'mesa_destino_nombre': mesa_destino_nombre,
                'zona_destino_id': zona_destino_id,
                'zona_destino_nombre': zona_destino_nombre,
                'tipo_cambio': tipo_cambio,
                'camarero': camarero_nombre,
                'empresa': empresa,
                'infmesa_id': infmesa_id,
                'total_lineas': len(lineas_pedido),
                'total': total
            }
        )
        
        try:
            # Enviar mensaje a Telegram
            url = f"https://api.telegram.org/bot{bot_token}/sendMessage"
            data = {
                'chat_id': subscription.usuario.telegram_user_id,
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
                
                # Guardar autorizaciones localmente en la BD
                try:
                    # Registrar token de borrar
                    TelegramAutorizacion.objects.create(
                        token=token_borrar,
                        uid_dispositivo=infmesa_id,
                        telegram_user_id=subscription.usuario.telegram_user_id,
                        telegram_message_id=message_id,
                        accion='borrar_mesa',
                        empresa=empresa,
                        expira_en=expira_en
                    )
                    
                    # Registrar token de mantener
                    TelegramAutorizacion.objects.create(
                        token=token_mantener,
                        uid_dispositivo=infmesa_id,
                        telegram_user_id=subscription.usuario.telegram_user_id,
                        telegram_message_id=message_id,
                        accion='mantener_mesa',
                        empresa=empresa,
                        expira_en=expira_en
                    )
                    
                    logger.info(f"Autorizaciones creadas localmente para mesa {mesa_destino_nombre}")
                except Exception as e:
                    logger.error(f"Error creando autorizaciones: {e}")
                
                logger.info(f"Notificación enviada a {subscription.usuario.nombre} para zona {zona_destino_nombre}")
                enviados += 1
            else:
                error_msg = response.json().get('description', 'Error desconocido')
                log.error = f"HTTP {response.status_code}: {error_msg}"
                log.save()
                logger.warning(f"Error enviando a {subscription.usuario.nombre}: {error_msg}")
                
        except Exception as e:
            log.error = str(e)
            log.save()
            logger.error(f"Excepción enviando a {subscription.usuario.nombre}: {e}")
    
    logger.info(f"Evento 'cambio_zona' ({zona_destino_nombre}): {enviados}/{len(subscriptions_filtradas)} notificaciones enviadas")
    return enviados


# Alias para mantener compatibilidad con código antiguo
def notificar_cambio_mesa_a_barra(mesa_origen_id: int, mesa_origen_nombre: str, 
                                  mesa_destino_id: int, mesa_destino_nombre: str,
                                  camarero_nombre: str, hora_apertura: str, 
                                  lineas_pedido: list, infmesa_id: str):
    """
    DEPRECATED: Usar notificar_cambio_zona en su lugar.
    Esta función existe solo para compatibilidad con código antiguo.
    """
    # Obtener la zona destino
    from gestion.models.mesas import Mesas
    try:
        mesa = Mesas.objects.get(pk=mesa_destino_id)
        mesazona = mesa.mesaszona_set.select_related('zona').first()
        if mesazona:
            return notificar_cambio_zona(
                mesa_origen_id=mesa_origen_id,
                mesa_origen_nombre=mesa_origen_nombre,
                mesa_destino_id=mesa_destino_id,
                mesa_destino_nombre=mesa_destino_nombre,
                zona_destino_id=mesazona.zona_id,
                zona_destino_nombre=mesazona.zona.nombre,
                camarero_nombre=camarero_nombre,
                hora_apertura=hora_apertura,
                lineas_pedido=lineas_pedido,
                infmesa_id=infmesa_id,
                tipo_cambio="mesa_completa"
            )
    except Exception as e:
        logger.error(f"Error en notificar_cambio_mesa_a_barra (deprecated): {e}")
    return 0


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
