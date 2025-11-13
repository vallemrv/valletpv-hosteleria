import uuid
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.utils import timezone
from gestion.models.dispositivos import Dispositivo
from gestion.tools.config_logs import configurar_logging
from gestion.decorators.log_excepciones import log_excepciones
from push_telegram.models import TelegramAutorizacion

# Logger personalizado para este módulo
logger = configurar_logging("api_dispositivos")


@csrf_exempt
@log_excepciones("api_dispositivos.log")
def create_uid(request):
    # Generar un UID único usando uuid4
    uid = str(uuid.uuid4())
    alias = request.POST.get("alias", f"")

    logger.info(f"Creando nuevo dispositivo con UID: {uid}")
    
    # Crear el objeto Dispositivo con el UID generado, activo=False y sin descripción
    dispositivo = Dispositivo.objects.create(uid=uid, descripcion=alias, activo=False)

    logger.info(f"Dispositivo creado exitosamente: {uid}")
    
    # Retornar una respuesta en formato JSON con el UID generado
    return JsonResponse({'uid': dispositivo.uid})


@csrf_exempt
@log_excepciones("api_dispositivos.log")
def set_alias(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Obtener parámetros del POST
    uid = request.POST.get('uid')
    alias = request.POST.get('alias')
    
    logger.info(f"Solicitud de establecer alias para dispositivo: {uid}")
    
    # Validar que se hayan proporcionado ambos parámetros
    if not uid:
        logger.warning("Intento de set_alias sin uid")
        return JsonResponse({'error': 'Parámetro uid requerido'}, status=400)
    
    if not alias:
        logger.warning(f"Intento de set_alias sin alias para dispositivo: {uid}")
        return JsonResponse({'error': 'Parámetro alias requerido'}, status=400)
    
    # Buscar el dispositivo por UID
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {uid}")
        return JsonResponse({'error': 'Dispositivo no encontrado'}, status=404)
    
    # Actualizar la descripción con el alias
    dispositivo.descripcion = alias.strip()
    dispositivo.save()
    
    logger.info(f"Alias actualizado para dispositivo {uid}: {alias}")
    
    # Retornar respuesta exitosa
    return JsonResponse({
        'success': True,
        'message': 'Alias actualizado correctamente',
        'uid': dispositivo.uid,
        'alias': dispositivo.descripcion
    })



@csrf_exempt
@log_excepciones("api_dispositivos.log")
def activate_device(request):
    """
    Endpoint para activar un dispositivo por su UID con autorización de Telegram.
    Valida el token contra la BD local.
    """
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    token_autorizacion = request.POST.get('token') or request.GET.get('token')
    
    if not token_autorizacion:
        logger.warning("Intento de activación sin token")
        return JsonResponse({'error': 'Parámetro requerido: token'}, status=400)
    
    # Buscar autorización en BD local solo por token
    try:
        autorizacion = TelegramAutorizacion.objects.get(
            token=token_autorizacion,
            accion='activate_device'
        )
    except TelegramAutorizacion.DoesNotExist:
        logger.error(f"Autorización no encontrada - Token: {token_autorizacion[:8]}...")
        return JsonResponse({'error': 'Autorización no encontrada', 'success': False}, status=403)
    
    uid = autorizacion.uid_dispositivo
    logger.info(f"Solicitud de activación - UID: {uid}, Token encontrado")
    
    # Verificar validez
    if not autorizacion.is_valida():
        logger.error(f"Autorización inválida - UID: {uid}")
        mensaje = 'Autorización ya utilizada' if autorizacion.usada else 'Autorización expirada'
        return JsonResponse({'error': mensaje}, status=403)
    
    # Buscar el dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {uid}")
        return JsonResponse({'error': 'Dispositivo no encontrado'}, status=404)
    
    was_active = dispositivo.activo
    dispositivo.activo = True
    dispositivo.save(update_fields=['activo'])
    
    # Marcar autorización como usada
    autorizacion.usada = True
    autorizacion.usada_en = timezone.now()
    autorizacion.save(update_fields=['usada', 'usada_en'])
    
    logger.info(f"✅ Dispositivo activado - UID: {uid}, Was active: {was_active}")
    
    # Editar mensaje de Telegram
    from push_telegram.push_sender import editar_mensaje_dispositivo
    editar_mensaje_dispositivo(
        telegram_user_id=autorizacion.telegram_user_id,
        message_id=autorizacion.telegram_message_id,
        uid=uid,
        descripcion=dispositivo.descripcion,
        accion='activado',
        ya_estaba=was_active
    )
    
    return JsonResponse({
        'success': True,
        'message': 'Dispositivo activado correctamente' if not was_active else 'Dispositivo ya estaba activo',
        'uid': dispositivo.uid,
        'descripcion': dispositivo.descripcion,
        'activo': dispositivo.activo,
        'was_active': was_active
    })


@csrf_exempt
@log_excepciones("api_dispositivos.log")
def deactivate_device(request):
    """
    Endpoint para desactivar un dispositivo por su UID con autorización de Telegram.
    Valida el token contra la BD local.
    """
    token_autorizacion = request.GET.get('token') or request.POST.get('token')
    
    if not token_autorizacion:
        logger.warning("Intento de desactivación sin token")
        return JsonResponse({'error': 'Parámetro requerido: token', 'success': False}, status=400)
    
    # Buscar autorización en BD local solo por token
    try:
        autorizacion = TelegramAutorizacion.objects.get(
            token=token_autorizacion,
            accion='deactivate_device'
        )
    except TelegramAutorizacion.DoesNotExist:
        logger.error(f"Autorización no encontrada - Token: {token_autorizacion[:8]}...")
        return JsonResponse({'error': 'Autorización no encontrada', 'success': False}, status=403)
    
    uid = autorizacion.uid_dispositivo
    logger.info(f"Solicitud de desactivación - UID: {uid}, Token encontrado")
    
    # Verificar validez
    if not autorizacion.is_valida():
        logger.error(f"Autorización inválida - UID: {uid}")
        mensaje = 'Autorización ya utilizada' if autorizacion.usada else 'Autorización expirada'
        return _respuesta_html('⏱️ Expirado', mensaje, False)
    
    # Buscar el dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {uid}")
        return _respuesta_html('❌ Error', 'Dispositivo no encontrado', False)
    
    was_inactive = not dispositivo.activo
    dispositivo.activo = False
    dispositivo.save(update_fields=['activo'])
    
    # Marcar autorización como usada
    autorizacion.usada = True
    autorizacion.usada_en = timezone.now()
    autorizacion.save(update_fields=['usada', 'usada_en'])
    
    logger.info(f"⏸️ Dispositivo desactivado - UID: {uid}, Was inactive: {was_inactive}")
    
    # Editar mensaje de Telegram
    from push_telegram.push_sender import editar_mensaje_dispositivo
    editar_mensaje_dispositivo(
        telegram_user_id=autorizacion.telegram_user_id,
        message_id=autorizacion.telegram_message_id,
        uid=uid,
        descripcion=dispositivo.descripcion,
        accion='desactivado',
        ya_estaba=was_inactive
    )
    
    mensaje = f"Dispositivo: {dispositivo.descripcion or uid[:20]}"
    if was_inactive:
        return _respuesta_html('ℹ️ Ya Inactivo', mensaje, True, 'El dispositivo ya estaba inactivo')
    else:
        return _respuesta_html('⏸️ Desactivado', mensaje, True)


def _respuesta_html(titulo, mensaje, exito=True, submensaje=None):
    """
    Generar una respuesta HTML amigable para mostrar en el navegador
    """
    color = '#4CAF50' if exito else '#f44336'
    html = f"""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>{titulo}</title>
        <style>
            body {{
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                margin: 0;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }}
            .container {{
                background: white;
                padding: 2rem;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                text-align: center;
                max-width: 400px;
                margin: 1rem;
            }}
            .icon {{
                font-size: 4rem;
                margin-bottom: 1rem;
            }}
            h1 {{
                color: {color};
                margin: 0 0 1rem 0;
                font-size: 1.5rem;
            }}
            p {{
                color: #666;
                margin: 0.5rem 0;
                font-size: 1rem;
            }}
            .sub {{
                color: #999;
                font-size: 0.9rem;
                margin-top: 1rem;
            }}
            .uid {{
                background: #f5f5f5;
                padding: 0.5rem;
                border-radius: 5px;
                font-family: monospace;
                font-size: 0.85rem;
                margin-top: 1rem;
                word-break: break-all;
            }}
        </style>
    </head>
    <body>
        <div class="container">
            <div class="icon">{titulo.split()[0]}</div>
            <h1>{titulo}</h1>
            <p><strong>{mensaje}</strong></p>
            {f'<p class="sub">{submensaje}</p>' if submensaje else ''}
            <p class="sub">Puedes cerrar esta ventana</p>
        </div>
    </body>
    </html>
    """
    return HttpResponse(html)
