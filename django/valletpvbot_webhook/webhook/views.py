"""
Webhook Router - Proxy simple para Telegram
Recibe mensajes del TPV → Envía a Telegram → Devuelve callbacks al TPV
"""

import logging
import requests
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods
from django.conf import settings
import json

from .models import TokenCallbackMapping
from .telegram_service import TelegramService

logger = logging.getLogger(__name__)


def validate_api_key(request):
    """
    Valida que la petición incluya el API Key correcto.
    
    El TPV debe enviar: Authorization: Bearer <API_KEY>
    """
    auth_header = request.META.get('HTTP_AUTHORIZATION', '')
    
    if not auth_header.startswith('Bearer '):
        return False
    
    provided_key = auth_header.replace('Bearer ', '').strip()
    expected_key = settings.TPV_API_KEY
    
    if not expected_key:
        logger.warning('TPV_API_KEY no configurado en settings')
        return False
    
    return provided_key == expected_key


@csrf_exempt
@require_http_methods(["POST"])
def telegram_webhook(request):
    """
    Endpoint principal del webhook de Telegram.
    Procesa /start y callbacks de botones inline.
    """
    try:
        payload = json.loads(request.body.decode('utf-8'))
        logger.info(f"Webhook recibido: {payload}")
        
        if 'message' in payload:
            process_message(payload['message'])
        elif 'callback_query' in payload:
            process_callback_query(payload['callback_query'])
        
        return JsonResponse({'status': 'ok'})
        
    except json.JSONDecodeError as e:
        logger.error(f"Error al parsear JSON: {str(e)}")
        return JsonResponse({'error': 'Invalid JSON'}, status=400)
    
    except Exception as e:
        logger.error(f"Error procesando webhook: {str(e)}", exc_info=True)
        return JsonResponse({'error': 'Internal error'}, status=500)


def process_message(message_data):
    """
    Procesa mensajes de Telegram (comando /start).
    """
    from_user = message_data.get('from', {})
    telegram_id = from_user.get('id')
    username = from_user.get('username', '')
    first_name = from_user.get('first_name', '')
    chat_id = message_data.get('chat', {}).get('id')
    text = message_data.get('text', '')
    
    if not telegram_id or not text.startswith('/start'):
        return
    
    logger.info(f"Comando /start de usuario {telegram_id} (@{username})")
    
    # Mensaje de bienvenida con ID para copiar
    mensaje = f"""
👋 <b>¡Bienvenido al Sistema de Avisos de Valle TPV!</b>

📱 Tu ID de Telegram es:
<code>{telegram_id}</code>

ℹ️ <b>Instrucciones:</b>
1. Copia tu ID (presiona sobre el número)
2. Comunícalo al administrador del sistema

🔔 Mantén activadas las notificaciones del bot.
    """.strip()
    
    # Botón para facilitar copiar el ID
    keyboard = {
        'inline_keyboard': [
            [{'text': '📋 Copiar ID', 'copy_text': {'text': str(telegram_id)}}]
        ]
    }
    
    telegram_service = TelegramService()
    telegram_service.send_message(
        chat_id=chat_id,
        text=mensaje,
        reply_markup=keyboard
    )
    
    logger.info(f"Mensaje de bienvenida enviado a {telegram_id}")


def process_callback_query(callback_data):
    """
    PASO 2: Telegram → Webhook → TPV
    Usuario pulsa botón → Busca callback_url → Llama al TPV
    Formato callback_data: "accion|token"
    """
    telegram_id = callback_data.get('from', {}).get('id')
    callback_id = callback_data.get('id')
    data = callback_data.get('data', '')
    
    if not telegram_id or '|' not in data:
        logger.warning(f"Callback inválido: {data}")
        answer_callback_query(callback_id, "❌ Datos inválidos")
        return
    
    logger.info(f"← Callback: {data} (user: {telegram_id})")
    
    try:
        accion, token = data.split('|', 1)
        
        # Buscar mapeo
        try:
            mapping = TokenCallbackMapping.objects.get(token=token)
        except TokenCallbackMapping.DoesNotExist:
            logger.error(f"✗ Token no encontrado: {token[:8]}...")
            answer_callback_query(callback_id, "❌ Token no válido")
            return
        
        # Validar
        if not mapping.is_valida():
            logger.warning(f"✗ Token expirado: {token[:8]}...")
            answer_callback_query(callback_id, "❌ Token expirado")
            return
        
        # Llamar al TPV
        logger.info(f"→ Llamando TPV: {mapping.callback_url}")
        response = requests.post(
            mapping.callback_url,
            data={'token': token, 'accion': accion},
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get('success'):
                mapping.marcar_usada()
                mensaje = result.get('mensaje', '✅ Hecho')
                answer_callback_query(callback_id, mensaje)
                logger.info(f"✓ OK: {accion}")
            else:
                error = result.get('error', 'Error')
                logger.error(f"✗ TPV error: {error}")
                answer_callback_query(callback_id, f"❌ {error}")
        else:
            logger.error(f"✗ HTTP {response.status_code}")
            answer_callback_query(callback_id, "❌ Error en TPV")
            
    except requests.exceptions.Timeout:
        logger.error(f"✗ Timeout: {mapping.callback_url}")
        answer_callback_query(callback_id, "❌ Timeout")
    except Exception as e:
        logger.error(f"✗ Error: {e}", exc_info=True)
        answer_callback_query(callback_id, "❌ Error")


def answer_callback_query(callback_id, text):
    """Responde a un callback query mostrando una alerta al usuario"""
    from django.conf import settings
    
    telegram_service = TelegramService()
    url = f"https://api.telegram.org/bot{settings.TELEGRAM_BOT_TOKEN}/answerCallbackQuery"
    
    try:
        requests.post(url, json={
            'callback_query_id': callback_id,
            'text': text,
            'show_alert': True
        }, timeout=5)
    except Exception as e:
        logger.error(f"Error respondiendo callback query: {e}")


@require_http_methods(["GET"])
def webhook_info(request):
    """
    Endpoint para verificar el estado del webhook.
    """
    telegram_service = TelegramService()
    info = telegram_service.get_webhook_info()
    
    return JsonResponse({
        'status': 'active',
        'webhook_info': info
    })


@csrf_exempt
@require_http_methods(["POST"])
def register_notification(request):
    """
    PASO 1: TPV → Webhook
    Recibe mensaje del TPV y lo envía a Telegram.
    Guarda mapeo token→callback_url para devolver la respuesta después.
    
    POST /api/register_notification/
    Headers: Authorization: Bearer <API_KEY>
    {
        "token": "uuid-unico",
        "callback_url": "https://tpvtest.valletpv.es/api/dispositivo/action",
        "telegram_user_id": 123456789,
        "mensaje": "Texto del mensaje (HTML)",
        "botones": [[{"text": "✅ Activar", "callback_data": "activate|token"}]],
        "expira_en": "2024-01-01T12:00:00Z"
    }
    """
    # Validar API Key
    if not validate_api_key(request):
        logger.warning(f"✗ Intento no autorizado desde {request.META.get('REMOTE_ADDR')}")
        return JsonResponse({'error': 'No autorizado'}, status=401)
    
    try:
        data = json.loads(request.body.decode('utf-8'))
        
        # Validar campos
        required = ['token', 'callback_url', 'telegram_user_id', 'mensaje', 'botones', 'expira_en']
        if missing := [f for f in required if f not in data]:
            return JsonResponse({'error': f'Faltan: {", ".join(missing)}'}, status=400)
        
        # Parsear fecha
        from django.utils.dateparse import parse_datetime
        expira_en = parse_datetime(data['expira_en']) if isinstance(data['expira_en'], str) else data['expira_en']
        if not expira_en:
            return JsonResponse({'error': 'expira_en inválido'}, status=400)
        
        # Guardar mapeo
        mapping = TokenCallbackMapping.objects.create(
            token=data['token'],
            callback_url=data['callback_url'],
            telegram_user_id=data['telegram_user_id'],
            expira_en=expira_en,
            empresa=data.get('empresa', ''),
            uid_dispositivo=data.get('uid_dispositivo', ''),
            metadata=data.get('metadata', {})
        )
        
        logger.info(f"✓ Registrado: {data['token'][:8]}... → {data['callback_url']}")
        
        # Enviar a Telegram
        telegram_service = TelegramService()
        result = telegram_service.send_message(
            chat_id=data['telegram_user_id'],
            text=data['mensaje'],
            reply_markup={'inline_keyboard': data['botones']}
        )
        
        # Guardar message_id
        message_id = result.get('message_id') if result else None
        if message_id:
            mapping.telegram_message_id = message_id
            mapping.save(update_fields=['telegram_message_id'])
            logger.info(f"✓ Telegram message_id: {message_id}")
        
        return JsonResponse({'success': True, 'message_id': message_id})
        
    except Exception as e:
        logger.error(f"✗ Error: {e}", exc_info=True)
        return JsonResponse({'error': str(e)}, status=500)





@csrf_exempt
@require_http_methods(["POST"])
def delete_message(request):
    """
    Permite al TPV borrar un mensaje enviado a Telegram.
    
    POST /api/delete_message/
    Headers: Authorization: Bearer <API_KEY>
    {
        "telegram_user_id": 123456789,
        "message_id": 12345
    }
    """
    # Validar API Key
    if not validate_api_key(request):
        logger.warning(f"✗ Intento no autorizado desde {request.META.get('REMOTE_ADDR')}")
        return JsonResponse({'error': 'No autorizado'}, status=401)
    
    try:
        data = json.loads(request.body.decode('utf-8'))
        
        # Validar campos
        if 'telegram_user_id' not in data or 'message_id' not in data:
            return JsonResponse({'error': 'Faltan campos: telegram_user_id, message_id'}, status=400)
        
        telegram_user_id = data['telegram_user_id']
        message_id = data['message_id']
        
        # Borrar mensaje
        telegram_service = TelegramService()
        success = telegram_service.delete_message(telegram_user_id, message_id)
        
        if success:
            logger.info(f"✓ Mensaje {message_id} borrado")
            return JsonResponse({'success': True})
        else:
            return JsonResponse({'error': 'No se pudo borrar el mensaje'}, status=500)
        
    except Exception as e:
        logger.error(f"✗ Error borrando mensaje: {e}", exc_info=True)
        return JsonResponse({'error': str(e)}, status=500)


@csrf_exempt
@require_http_methods(["POST"])
def edit_message(request):
    """
    Permite al TPV editar un mensaje enviado a Telegram.
    
    POST /api/edit_message/
    Headers: Authorization: Bearer <API_KEY>
    {
        "telegram_user_id": 123456789,
        "message_id": 12345,
        "nuevo_texto": "✅ <b>Dispositivo Activado</b>",
        "botones": []  // Opcional, [] para quitar botones, null para mantener
    }
    """
    # Validar API Key
    if not validate_api_key(request):
        logger.warning(f"✗ Intento no autorizado desde {request.META.get('REMOTE_ADDR')}")
        return JsonResponse({'error': 'No autorizado'}, status=401)
    
    try:
        data = json.loads(request.body.decode('utf-8'))
        
        # Validar campos
        required = ['telegram_user_id', 'message_id', 'nuevo_texto']
        if missing := [f for f in required if f not in data]:
            return JsonResponse({'error': f'Faltan: {", ".join(missing)}'}, status=400)
        
        telegram_user_id = data['telegram_user_id']
        message_id = data['message_id']
        nuevo_texto = data['nuevo_texto']
        botones = data.get('botones')  # None = mantener, [] = quitar, [[...]] = reemplazar
        
        # Preparar reply_markup
        reply_markup = None
        if botones is not None:
            reply_markup = {'inline_keyboard': botones}
        
        # Editar mensaje
        telegram_service = TelegramService()
        success = telegram_service.edit_message(
            chat_id=telegram_user_id,
            message_id=message_id,
            text=nuevo_texto,
            reply_markup=reply_markup
        )
        
        if success:
            logger.info(f"✓ Mensaje {message_id} editado")
            return JsonResponse({'success': True})
        else:
            return JsonResponse({'error': 'No se pudo editar el mensaje'}, status=500)
        
    except Exception as e:
        logger.error(f"✗ Error editando mensaje: {e}", exc_info=True)
        return JsonResponse({'error': str(e)}, status=500)


@require_http_methods(["GET"])
def home(request):
    """
    Vista principal del servidor webhook.
    """
    html = """
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Valle TPV Bot Webhook</title>
        <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                margin: 0;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }
            .container {
                background: white;
                padding: 3rem;
                border-radius: 20px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                text-align: center;
                max-width: 500px;
            }
            h1 {
                color: #667eea;
                margin-bottom: 1rem;
                font-size: 2rem;
            }
            .status {
                display: inline-block;
                padding: 0.5rem 1.5rem;
                background: #10b981;
                color: white;
                border-radius: 25px;
                font-weight: 600;
                margin: 1rem 0;
            }
            .icon {
                font-size: 4rem;
                margin-bottom: 1rem;
            }
            p {
                color: #6b7280;
                line-height: 1.6;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="icon">🤖</div>
            <h1>Valle TPV Bot Webhook</h1>
            <div class="status">✓ Servidor Corriendo</div>
            <p>El servidor webhook está activo y listo para recibir notificaciones de Telegram.</p>
        </div>
    </body>
    </html>
    """
    return HttpResponse(html)



