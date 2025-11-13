"""
Vistas para el webhook de Telegram.
"""

import logging
import requests
from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods
from django.utils import timezone
import json

from .models import TelegramAutorizacion
from .telegram_service import TelegramService

logger = logging.getLogger(__name__)


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
    Procesa callbacks de botones inline (activar/desactivar dispositivos).
    El callback_data contiene: accion|token
    Busca el token en BD para obtener la empresa y construir la URL.
    """
    from_user = callback_data.get('from', {})
    telegram_id = from_user.get('id')
    callback_id = callback_data.get('id')
    data = callback_data.get('data', '')
    
    if not telegram_id:
        logger.warning("Callback query sin telegram_id")
        return
    
    logger.info(f"Callback de {telegram_id}: {data}")
    
    # El callback_data tiene formato: accion|token
    if '|' not in data:
        logger.warning(f"Callback data inválido: {data}")
        answer_callback_query(callback_id, "❌ Datos inválidos")
        return
    
    try:
        accion, token = data.split('|', 1)
        
        # El token contiene el servidor TPV implícito
        # Intentamos con los servidores conocidos en orden
        tpv_urls = [
            'https://tpvtest.valletpv.es',
            'https://v6.valletpv.es',
        ]
        
        endpoint = 'activate' if accion == 'activate' else 'deactivate'
        success = False
        
        # Intentar con cada TPV hasta encontrar el token
        for base_url in tpv_urls:
            url = f"{base_url}/api/dispositivo/{endpoint}?token={token}"
            
            try:
                logger.info(f"Intentando callback en: {url}")
                response = requests.post(url, timeout=5)
                
                if response.status_code == 200:
                    result = response.json()
                    if result.get('success'):
                        accion_texto = 'activado' if accion == 'activate' else 'desactivado'
                        mensaje = f"✅ Dispositivo {accion_texto}"
                        answer_callback_query(callback_id, mensaje)
                        logger.info(f"Dispositivo {accion_texto} correctamente en {base_url}")
                        success = True
                        break
                    elif result.get('message'):
                        # El TPV respondió pero hubo un error (token inválido, etc)
                        answer_callback_query(callback_id, f"⚠️ {result.get('message')}")
                        success = True
                        break
                        
            except requests.exceptions.Timeout:
                logger.warning(f"Timeout en {base_url}")
                continue
            except requests.exceptions.RequestException as e:
                logger.warning(f"Error conectando con {base_url}: {e}")
                continue
        
        if not success:
            logger.error(f"Token no encontrado en ningún TPV: {token}")
            answer_callback_query(callback_id, "❌ Autorización no válida o expirada")
            
    except ValueError:
        logger.error(f"Error parseando callback data: {data}")
        answer_callback_query(callback_id, "❌ Formato inválido")
    except requests.exceptions.RequestException as e:
        logger.error(f"Error conectando con TPV: {e}")
        answer_callback_query(callback_id, "❌ Error de conexión")
    except Exception as e:
        logger.error(f"Error procesando callback: {e}", exc_info=True)
        answer_callback_query(callback_id, "❌ Error interno")


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



