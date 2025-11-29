# push_telegram/push_sender.py
# Sistema de envío de notificaciones push a Telegram vía webhook

import logging
import requests
import uuid
from django.db import transaction
from datetime import timedelta
from django.conf import settings
from django.utils import timezone
from .models import TelegramEventType, TelegramSubscription, TelegramNotificationLog
from gestion.tools.config_logs import configurar_logging
import requests
import uuid
from gestion.models.mesasabiertas import Mesasabiertas
from gestion.models.mesas import Mesas

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
    # Crear log pero no intentar guardar todavía
    log_data = {
        'event_type': event_type,
        'telegram_user_id': chat_id,
        'mensaje': mensaje,
        'metadata': metadata,
        'enviado': False,
        'error': None
    }
    
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
            log_data['enviado'] = True
            _guardar_log_seguro(log_data)
            logger.debug(f"Mensaje enviado a {chat_id}")
            return True
        else:
            error_msg = response.json().get('description', 'Error desconocido')
            log_data['error'] = f"HTTP {response.status_code}: {error_msg}"
            _guardar_log_seguro(log_data)
            logger.warning(f"Error enviando a {chat_id}: {error_msg}")
            return False
            
    except Exception as e:
        log_data['error'] = str(e)
        _guardar_log_seguro(log_data)
        logger.error(f"Excepción enviando a {chat_id}: {e}")
        return False


def _guardar_log_seguro(log_data: dict):
    """
    Guardar log de forma segura, manejando errores de charset de MySQL y transacciones.
    """
    # Intentamos limpiar el mensaje de emojis preventivamente si sabemos que puede dar problemas,
    # o confiamos en el manejo de errores.
    
    try:
        # Usamos un bloque atomic para crear un savepoint. 
        # Si falla el save(), solo se revierte este bloque y no la transacción principal.
        with transaction.atomic():
            log = TelegramNotificationLog(
                event_type=log_data['event_type'],
                telegram_user_id=log_data['telegram_user_id'],
                mensaje=log_data['mensaje'],
                metadata=log_data['metadata'],
                enviado=log_data['enviado'],
                error=log_data['error']
            )
            log.save()
    except Exception as e:
        # Si falla (por ejemplo, por charset), la transacción interna se ha revertido.
        # La transacción externa sigue válida.
        
        error_str = str(e)
        # Si es error de charset o similar
        if '1366' in error_str or 'Incorrect string value' in error_str:
            try:
                # Intentamos guardar versión limpia en una nueva sub-transacción
                with transaction.atomic():
                    mensaje_sin_emojis = log_data['mensaje'].encode('ascii', 'ignore').decode('ascii')
                    log = TelegramNotificationLog(
                        event_type=log_data['event_type'],
                        telegram_user_id=log_data['telegram_user_id'],
                        mensaje=mensaje_sin_emojis,
                        metadata=log_data['metadata'],
                        enviado=log_data['enviado'],
                        error=log_data['error']
                    )
                    log.save()
                    logger.warning(f"Log guardado sin emojis debido a charset MySQL")
            except Exception as e2:
                logger.error(f"Error guardando log incluso sin emojis: {e2}")
        else:
            logger.error(f"Error guardando log: {e}")


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
    bot_token = telegram_config.get('TOKEN', '')
    
    # Decidir método de envío: webhook o directo
    use_webhook = bool(webhook_url)
    use_direct = bool(bot_token) and not use_webhook
    
    if not use_webhook and not use_direct:
        logger.error("Ni webhook ni token de bot configurados")
        return 0
    
    # Enviar a cada suscriptor
    enviados = 0
    for subscription in subscriptions:
        # Construir mensaje
        mensaje = f"""
🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

⚠️ El dispositivo está <b>INACTIVO</b> por seguridad.
❓ ¿Deseas activarlo o mantenerlo desactivado?

⏰ Esta autorización expira en 10 minutos.
        """.strip()
        
        if use_direct:
            # ENVÍO DIRECTO - Sin botones por simplicidad
            enviado = _enviar_mensaje_telegram(
                bot_token=bot_token,
                chat_id=subscription.usuario.telegram_user_id,
                mensaje=mensaje,
                event_type=event_type,
                metadata={'uid': uid, 'descripcion': descripcion, 'empresa': empresa}
            )
            if enviado:
                enviados += 1
                logger.info(f"Notificación enviada directamente a {subscription.usuario.nombre}")
                
        elif use_webhook:
            # ENVÍO VÍA WEBHOOK - Con botones y tokens
            token = str(uuid.uuid4())
            expira_en = timezone.now() + timedelta(minutes=10)
            

            
            # Botones inline
            botones = [
                [
                    {'text': '✅ Activar', 'callback_data': f"activate|{token}"},
                    {'text': '🛑 Desactivar', 'callback_data': f"deactivate|{token}"}
                ]
            ]
            
            # Preparar datos del log
            log_data = {
                'event_type': event_type,
                'telegram_user_id': subscription.usuario.telegram_user_id,
                'mensaje': mensaje,
                'metadata': {'uid': uid, 'descripcion': descripcion, 'empresa': empresa},
                'enviado': False,
                'error': None
            }
            
            try:
                # Enviar al webhook
                tpv_api_key = telegram_config.get('TPV_API_KEY', '')
                registro_url = f"{webhook_url}/api/register_notification/"
                headers = {'Authorization': f'Bearer {tpv_api_key}'}
                
                response = requests.post(registro_url, 
                    headers=headers,
                    json={
                        'token': token,
                        'callback_url': f"{base_url}/api/dispositivo/action",
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
                    

                    _guardar_log_seguro(log_data)
                    logger.info(f"Notificación enviada vía webhook para {subscription.usuario.nombre}")
                    enviados += 1
                else:
                    error_msg = response.json().get('error', 'Error desconocido')
                    log_data['error'] = f"Webhook error: {error_msg}"
                    _guardar_log_seguro(log_data)
                    logger.warning(f"Error en webhook: {error_msg}")
                    
            except Exception as e:
                log_data['error'] = str(e)
                _guardar_log_seguro(log_data)
                logger.error(f"Excepción con webhook: {e}")
    
    logger.info(f"Evento 'nuevo_dispositivo': {enviados}/{subscriptions.count()} notificaciones enviadas")
    return enviados


def notificar_cambio_zona(mesa_origen_id: int, mesa_origen_nombre: str, 
                         mesa_destino_id: int, mesa_destino_nombre: str,
                         zona_destino_id: int, zona_destino_nombre: str,
                         camarero_nombre: str, hora_apertura: str, 
                         lineas_pedido: list, infmesa_id: str, tipo_cambio: str = "mesa_completa",
                         lineas_ids: list = None):
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
        lineas_ids: Lista de IDs de líneas (solo para lineas_parciales)
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
    webhook_url = telegram_config.get('WEBHOOK_URL', '')
    tpv_api_key = telegram_config.get('TPV_API_KEY', '')
    base_url = getattr(settings, 'BASE_URL', 'https://tpvtest.valletpv.es')
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    
    if not bot_token and not webhook_url:
        logger.error("Ni Token de Telegram ni Webhook configurados")
        return 0
    
    # Construir resumen de líneas
    resumen_lineas = ""
    total = 0
    for i, linea in enumerate(lineas_pedido[:10], 1):  # Máximo 10 líneas para no saturar
        resumen_lineas += f"{i}. {linea['descripcion']} - {linea['precio']}€\n"
        total += linea['precio']
    
    if len(lineas_pedido) > 10:
        resumen_lineas += f"... y {len(lineas_pedido) - 10} líneas más\n"
    
    # Determinar texto y acciones según el tipo de cambio
    if tipo_cambio == "lineas_parciales":
        tipo_texto = "Líneas Parciales"
        accion_borrar = "borrar_lineas"
        accion_mantener = "mantener_lineas"
        texto_borrar = "🗑️ Borrar Líneas"
        texto_mantener = "✅ Mantener Líneas"
        pregunta = "¿Deseas borrar estas líneas o mantenerlas?"
        
        # Para líneas parciales, necesitamos pasar los IDs en el uid_dispositivo
        # Formato: LINEAS:mesa_id:id1,id2,id3
        if lineas_ids:
            ids_str = ",".join(map(str, lineas_ids))
            uid_auth = f"LINEAS:{mesa_destino_id}:{ids_str}"
            # Truncar si es demasiado largo (max 255 chars)
            if len(uid_auth) > 255:
                logger.warning("Lista de IDs demasiado larga para autorización, truncando...")
                uid_auth = uid_auth[:255]
        else:
            uid_auth = infmesa_id # Fallback
            
    else:
        tipo_texto = "Mesa Completa"
        accion_borrar = "borrar_mesa"
        accion_mantener = "mantener_mesa"
        texto_borrar = "🗑️ Borrar Mesa"
        texto_mantener = "✅ Mantener Mesa"
        pregunta = "¿Deseas borrar esta mesa o mantenerla?"
        uid_auth = infmesa_id
    
    # Enviar a cada suscriptor
    enviados = 0
    for subscription in subscriptions_filtradas:
        # Crear token único para esta notificación
        token = str(uuid.uuid4())
        expira_en = timezone.now() + timedelta(minutes=10)
        
        # Mensaje personalizado según el tipo de cambio
        mensaje = f"""
🔄 <b>Cambio a Zona: {zona_destino_nombre}</b>
📋 <i>Tipo: {tipo_texto}</i>

📍 <b>De:</b> {mesa_origen_nombre} (ID: {mesa_origen_id})
📍 <b>A:</b> {mesa_destino_nombre} (ID: {mesa_destino_id})
🏷️ <b>Zona:</b> {zona_destino_nombre} (ID: {zona_destino_id})
👨‍🍳 <b>Camarero:</b> {camarero_nombre}
🕐 <b>Hora:</b> {hora_apertura}
🏢 <b>Empresa:</b> {empresa}

🍽️ <b>Líneas de pedido ({len(lineas_pedido)}):</b>
{resumen_lineas}
💰 <b>Total:</b> {total:.2f} EUR

❓ {pregunta}

⏰ Esta autorización expira en 10 minutos.
        """.strip()
        
        # Preparar datos del log
        log_data = {
            'event_type': event_type,
            'telegram_user_id': subscription.usuario.telegram_user_id,
            'mensaje': mensaje,
            'metadata': {
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
            },
            'enviado': False,
            'error': None
        }

        # Si hay webhook configurado, usarlo preferentemente
        if webhook_url:
            try:
                # Limpiar token
                token = token.strip()
                
                # Limpiar token
                token = token.strip()
                
                # Botones inline
                
                # Botones inline
                botones = [
                    [
                        {'text': texto_borrar, 'callback_data': f"{accion_borrar}|{token}"},
                        {'text': texto_mantener, 'callback_data': f"{accion_mantener}|{token}"}
                    ]
                ]
                
                # Enviar al webhook
                registro_url = f"{webhook_url}/api/register_notification/"
                headers = {'Authorization': f'Bearer {tpv_api_key}'}
                
                response = requests.post(registro_url, 
                    headers=headers,
                    json={
                        'token': token,
                        'callback_url': f"{base_url}/api/mesas/mesa_action",
                        'telegram_user_id': subscription.usuario.telegram_user_id,
                        'mensaje': mensaje,
                        'botones': botones,
                        'expira_en': expira_en.isoformat(),
                        'empresa': empresa,
                        'uid_dispositivo': uid_auth,
                        'metadata': log_data['metadata']
                    }, 
                    timeout=10
                )
                
                if response.status_code == 200:
                    result = response.json()
                    message_id = result.get('message_id')
                    

                    _guardar_log_seguro(log_data)
                    logger.info(f"Notificación enviada vía webhook para {subscription.usuario.nombre}")
                    enviados += 1
                else:
                    error_msg = response.json().get('error', 'Error desconocido')
                    log_data['error'] = f"Webhook error: {error_msg}"
                    _guardar_log_seguro(log_data)
                    logger.warning(f"Error en webhook: {error_msg}")
                    
            except Exception as e:
                log_data['error'] = str(e)
                _guardar_log_seguro(log_data)
                logger.error(f"Excepción con webhook: {e}")
                
        # Fallback a envío directo si no hay webhook (o si fallara, pero aquí es exclusivo)
        elif bot_token:
            try:
                # Callback data con formato corto
                # Nota: Sin webhook, el callback URL no se registra, así que el bot debe saber qué hacer
                # o esto fallará al hacer click.
                callback_borrar = f"{accion_borrar}|{token}"
                callback_mantener = f"{accion_mantener}|{token}"
                
                # Crear botones inline
                keyboard = {
                    'inline_keyboard': [
                        [
                            {'text': texto_borrar, 'callback_data': callback_borrar},
                            {'text': texto_mantener, 'callback_data': callback_mantener}
                        ]
                    ]
                }
                
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
                    
                    log_data['enviado'] = True
                    _guardar_log_seguro(log_data)
                    
                    logger.info(f"Notificación enviada a {subscription.usuario.nombre} para zona {zona_destino_nombre}")
                    enviados += 1
                else:
                    error_msg = response.json().get('description', 'Error desconocido')
                    log_data['error'] = f"HTTP {response.status_code}: {error_msg}"
                    _guardar_log_seguro(log_data)
                    logger.warning(f"Error enviando a {subscription.usuario.nombre}: {error_msg}")
                    
            except Exception as e:
                log_data['error'] = str(e)
                _guardar_log_seguro(log_data)
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


def editar_mensaje_mesa(telegram_user_id: int, message_id: int, mesa_nombre: str, accion: str):
    """
    Editar el mensaje de cambio de mesa eliminando los botones y mostrando el resultado.
    
    Args:
        telegram_user_id: ID del usuario de Telegram
        message_id: ID del mensaje a editar
        mesa_nombre: Nombre de la mesa
        accion: 'borrada', 'mantenida', 'expirada' o texto libre
    """
    telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
    bot_token = telegram_config.get('TOKEN', '')
    
    if not bot_token:
        logger.error("Token de Telegram no configurado")
        return
    
    # Mensaje según la acción
    if accion == 'borrada':
        nuevo_texto = f"🗑️ Mesa {mesa_nombre} ha sido BORRADA\n\n✅ Acción completada exitosamente."
    elif accion == 'mantenida':
        nuevo_texto = f"✅ Mesa {mesa_nombre} se ha MANTENIDO\n\n✅ La mesa sigue activa."
    elif accion == 'lineas_borradas':
        nuevo_texto = f"🗑️ Líneas de {mesa_nombre} han sido BORRADAS\n\n✅ Acción completada exitosamente."
    elif accion == 'lineas_mantenidas':
        nuevo_texto = f"✅ Líneas de {mesa_nombre} se han MANTENIDO\n\n✅ Las líneas siguen activas."
    else:
        # Si no es una palabra clave, usar como texto directo
        nuevo_texto = accion
    
    try:
        url = f"https://api.telegram.org/bot{bot_token}/editMessageText"
        data = {
            'chat_id': telegram_user_id,
            'message_id': message_id,
            'text': nuevo_texto,
            'parse_mode': 'HTML'
        }
        
        response = requests.post(url, json=data, timeout=10)
        
        if response.status_code == 200:
            logger.info(f"Mensaje de mesa editado correctamente para {telegram_user_id}")
        else:
            logger.warning(f"Error editando mensaje de mesa: {response.json()}")
    except Exception as e:
        logger.error(f"Excepción editando mensaje de mesa: {e}")


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
    
    # Determinar mensaje según la acción
    if accion == 'activado':
        estado = 'ACTIVO'
        verbo = 'activado'
        prefijo = '✅'
    else:  # desactivado
        estado = 'INACTIVO'
        verbo = 'desactivado'
        prefijo = '🛑'
    
    # Construir mensaje editado
    if ya_estaba:
        mensaje = f"""
🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

{prefijo} <b>El dispositivo ya estaba {estado}</b>
        """.strip()
    else:
        mensaje = f"""
🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>

📱 <b>UID:</b> <code>{uid}</code>
📝 <b>Descripción:</b> {descripcion or 'Sin descripción'}
🏢 <b>Empresa:</b> {empresa}

{prefijo} <b>Dispositivo {verbo.upper()}</b>
📊 <b>Estado actual:</b> {estado}
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


def mesa_cambiada(mesa_destino, uid_origen, lineas, tipo_cambio="lineas_parciales"):
    """
    Verificar si la mesa destino tiene zonas vigiladas y enviar notificación.
    Encapsula la lógica de preparación de datos y llamada a notificar_cambio_zona.
    
    Args:
        mesa_destino: Objeto Mesasabiertas destino
        uid_origen: UID de la infmesa origen
        lineas: QuerySet o lista de objetos Lineaspedido
        tipo_cambio: Tipo de cambio ("lineas_parciales" o "mesa_completa")
    """
    try:
        # Verificar si la mesa destino tiene zonas vigiladas
        mesazona_destino = mesa_destino.mesa.mesaszona_set.select_related('zona').first()
        
        if mesazona_destino:
            # Obtener mesa origen
            mesa_origen = Mesasabiertas.objects.filter(infmesa__pk=uid_origen).first()
            mesa_origen_nombre = mesa_origen.mesa.nombre if mesa_origen else "Desconocida"
            mesa_origen_id = mesa_origen.mesa_id if mesa_origen else 0
            
            # Preparar datos de líneas movidas
            lineas_datos = []
            lineas_ids = []
            for linea in lineas:
                precio = float(linea.precio) if hasattr(linea, 'precio') else 0.0
                lineas_datos.append({
                    'descripcion': linea.descripcion_t if hasattr(linea, 'descripcion_t') else 'Sin descripción',
                    'precio': precio
                })
                lineas_ids.append(linea.id)
            
            # Enviar notificación genérica
            notificar_cambio_zona(
                mesa_origen_id=mesa_origen_id,
                mesa_origen_nombre=mesa_origen_nombre,
                mesa_destino_id=mesa_destino.mesa.id,
                mesa_destino_nombre=mesa_destino.mesa.nombre,
                zona_destino_id=mesazona_destino.zona_id,
                zona_destino_nombre=mesazona_destino.zona.nombre,
                camarero_nombre=f"{mesa_destino.infmesa.camarero.nombre} {mesa_destino.infmesa.camarero.apellidos}",
                hora_apertura=mesa_destino.infmesa.hora,
                lineas_pedido=lineas_datos,
                infmesa_id=mesa_destino.infmesa.pk,
                tipo_cambio=tipo_cambio,
                lineas_ids=lineas_ids
            )
    except Exception as e:
        logger.error(f"Error enviando notificación de movimiento de líneas en mesa_cambiada: {e}")
