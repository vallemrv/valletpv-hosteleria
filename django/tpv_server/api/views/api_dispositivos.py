import uuid
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.utils import timezone
from gestion.models.dispositivos import Dispositivo
from push_telegram.models import TelegramAutorizacion
from gestion.tools.config_logs import configurar_logging
from gestion.decorators.log_excepciones import log_excepciones

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
    # La notificación de Telegram se envía automáticamente desde el modelo (método save)
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
    Endpoint para activar un dispositivo validando el token de Telegram.
    Este endpoint es llamado por el webhook tras validar el callback de Telegram.
    """
    if request.method != 'POST' and request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Obtener token del request
    token = request.POST.get('token') or request.GET.get('token')
    
    if not token:
        logger.warning("Intento de activación sin token")
        return JsonResponse({'error': 'Parámetro requerido: token', 'success': False}, status=400)
    
    logger.info(f"Solicitud de activación con token: {token[:8]}...")
    
    # Buscar y validar la autorización
    try:
        autorizacion = TelegramAutorizacion.objects.get(token=token)
    except TelegramAutorizacion.DoesNotExist:
        logger.error(f"Token no encontrado: {token}")
        return JsonResponse({'error': 'Token no válido o expirado', 'success': False}, status=404)
    
    # Validar que la autorización sea válida
    if not autorizacion.is_valida():
        logger.warning(f"Token inválido o expirado: {token}")
        return JsonResponse({'error': 'Token expirado o ya usado', 'success': False}, status=400)
    
    # Validar que la acción sea correcta
    if autorizacion.accion != 'activate_device':
        logger.warning(f"Token con acción incorrecta: {autorizacion.accion}")
        return JsonResponse({'error': 'Token no válido para esta acción', 'success': False}, status=400)
    
    # Buscar el dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=autorizacion.uid_dispositivo)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {autorizacion.uid_dispositivo}")
        return JsonResponse({'error': 'Dispositivo no encontrado', 'success': False}, status=404)
    
    # Marcar token como usado
    autorizacion.usada = True
    autorizacion.usada_en = timezone.now()
    autorizacion.save(update_fields=['usada', 'usada_en'])
    
    # Activar dispositivo
    was_active = dispositivo.activo
    dispositivo.activo = True
    dispositivo.save(update_fields=['activo'])
    
    logger.info(f"✅ Dispositivo activado - UID: {dispositivo.uid}, Was active: {was_active}")
    
    # Editar mensaje de Telegram eliminando botones
    try:
        from push_telegram.push_sender import editar_mensaje_dispositivo
        editar_mensaje_dispositivo(
            telegram_user_id=autorizacion.telegram_user_id,
            message_id=autorizacion.telegram_message_id,
            uid=dispositivo.uid,
            descripcion=dispositivo.descripcion,
            accion='activado',
            ya_estaba=was_active
        )
    except Exception as e:
        logger.warning(f"Error editando mensaje Telegram: {e}")
    
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
    Endpoint para desactivar un dispositivo validando el token de Telegram.
    Este endpoint es llamado por el webhook tras validar el callback de Telegram.
    """
    if request.method != 'POST' and request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Obtener token del request
    token = request.GET.get('token') or request.POST.get('token')
    
    if not token:
        logger.warning("Intento de desactivación sin token")
        return JsonResponse({'error': 'Parámetro requerido: token', 'success': False}, status=400)
    
    logger.info(f"Solicitud de desactivación con token: {token[:8]}...")
    
    # Buscar y validar la autorización
    try:
        autorizacion = TelegramAutorizacion.objects.get(token=token)
    except TelegramAutorizacion.DoesNotExist:
        logger.error(f"Token no encontrado: {token}")
        return JsonResponse({'error': 'Token no válido o expirado', 'success': False}, status=404)
    
    # Validar que la autorización sea válida
    if not autorizacion.is_valida():
        logger.warning(f"Token inválido o expirado: {token}")
        return JsonResponse({'error': 'Token expirado o ya usado', 'success': False}, status=400)
    
    # Validar que la acción sea correcta
    if autorizacion.accion != 'deactivate_device':
        logger.warning(f"Token con acción incorrecta: {autorizacion.accion}")
        return JsonResponse({'error': 'Token no válido para esta acción', 'success': False}, status=400)
    
    # Buscar el dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=autorizacion.uid_dispositivo)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {autorizacion.uid_dispositivo}")
        return JsonResponse({'error': 'Dispositivo no encontrado', 'success': False}, status=404)
    
    # Marcar token como usado
    autorizacion.usada = True
    autorizacion.usada_en = timezone.now()
    autorizacion.save(update_fields=['usada', 'usada_en'])
    
    # Desactivar dispositivo
    was_inactive = not dispositivo.activo
    dispositivo.activo = False
    dispositivo.save(update_fields=['activo'])
    
    logger.info(f"⏸️ Dispositivo desactivado - UID: {dispositivo.uid}, Was inactive: {was_inactive}")
    
    # Editar mensaje de Telegram eliminando botones
    try:
        from push_telegram.push_sender import editar_mensaje_dispositivo
        editar_mensaje_dispositivo(
            telegram_user_id=autorizacion.telegram_user_id,
            message_id=autorizacion.telegram_message_id,
            uid=dispositivo.uid,
            descripcion=dispositivo.descripcion,
            accion='desactivado',
            ya_estaba=was_inactive
        )
    except Exception as e:
        logger.warning(f"Error editando mensaje Telegram: {e}")
    
    return JsonResponse({
        'success': True,
        'message': 'Dispositivo desactivado correctamente' if not was_inactive else 'Dispositivo ya estaba inactivo',
        'uid': dispositivo.uid,
        'descripcion': dispositivo.descripcion,
        'activo': dispositivo.activo,
        'was_inactive': was_inactive
    })


@csrf_exempt
@log_excepciones("api_dispositivos.log")
def dispositivo_action(request):
    """
    Endpoint unificado para acciones de dispositivo (activar/desactivar).
    Recibe token y accion del webhook.
    """
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Obtener parámetros
    token = request.POST.get('token')
    accion = request.POST.get('accion')
    
    if not token:
        logger.warning("Intento de acción sin token")
        return JsonResponse({
            'error': 'Parámetro requerido: token',
            'success': False,
            'mensaje': '❌ Token no recibido'
        }, status=400)
    
    if not accion:
        logger.warning("Intento de acción sin especificar acción")
        return JsonResponse({
            'error': 'Parámetro requerido: accion',
            'success': False,
            'mensaje': '❌ Acción no especificada'
        }, status=400)
    
    logger.info(f"Solicitud de acción '{accion}' con token: {token[:8]}...")
    
    # Buscar y validar autorización
    try:
        autorizacion = TelegramAutorizacion.objects.get(token=token)
    except TelegramAutorizacion.DoesNotExist:
        logger.error(f"Token no encontrado: {token}")
        return JsonResponse({
            'error': 'Token no válido o expirado',
            'success': False,
            'mensaje': '❌ Token no válido o expirado'
        }, status=404)
    
    # Validar que sea válido
    if not autorizacion.is_valida():
        logger.warning(f"Token inválido o expirado: {token}")
        return JsonResponse({
            'error': 'Token expirado o ya usado',
            'success': False,
            'mensaje': '❌ Token expirado o ya utilizado'
        }, status=400)
    
    # Buscar dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=autorizacion.uid_dispositivo)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {autorizacion.uid_dispositivo}")
        return JsonResponse({
            'error': 'Dispositivo no encontrado',
            'success': False,
            'mensaje': '❌ Dispositivo no encontrado'
        }, status=404)
    
    # Marcar token como usado
    autorizacion.usada = True
    autorizacion.usada_en = timezone.now()
    autorizacion.save(update_fields=['usada', 'usada_en'])
    
    # Ejecutar acción (la acción viene del webhook, no de la BD)
    if accion == 'activate':
        was_active = dispositivo.activo
        dispositivo.activo = True
        dispositivo.save(update_fields=['activo'])
        
        # Editar mensaje de Telegram
        try:
            from push_telegram.push_sender import editar_mensaje_dispositivo
            editar_mensaje_dispositivo(
                telegram_user_id=autorizacion.telegram_user_id,
                message_id=autorizacion.telegram_message_id,
                uid=dispositivo.uid,
                descripcion=dispositivo.descripcion,
                accion='activado',
                ya_estaba=was_active
            )
        except Exception as e:
            logger.warning(f"Error editando mensaje: {e}")
        
        logger.info(f"✅ Dispositivo activado - UID: {dispositivo.uid}")
        
        return JsonResponse({
            'success': True,
            'mensaje': '✅ Dispositivo activado' if not was_active else '✅ Ya estaba activo',
            'uid': dispositivo.uid,
            'descripcion': dispositivo.descripcion,
            'activo': True
        })
        
    elif accion == 'deactivate':
        was_inactive = not dispositivo.activo
        dispositivo.activo = False
        dispositivo.save(update_fields=['activo'])
        
        # Editar mensaje de Telegram
        try:
            from push_telegram.push_sender import editar_mensaje_dispositivo
            editar_mensaje_dispositivo(
                telegram_user_id=autorizacion.telegram_user_id,
                message_id=autorizacion.telegram_message_id,
                uid=dispositivo.uid,
                descripcion=dispositivo.descripcion,
                accion='desactivado',
                ya_estaba=was_inactive
            )
        except Exception as e:
            logger.warning(f"Error editando mensaje: {e}")
        
        logger.info(f"⏸️ Dispositivo desactivado - UID: {dispositivo.uid}")
        
        return JsonResponse({
            'success': True,
            'mensaje': '⏸️ Dispositivo desactivado' if not was_inactive else '⏸️ Ya estaba inactivo',
            'uid': dispositivo.uid,
            'descripcion': dispositivo.descripcion,
            'activo': False
        })
        
    else:
        logger.warning(f"Acción desconocida: {accion}")
        return JsonResponse({
            'error': f'Acción desconocida: {accion}',
            'success': False,
            'mensaje': f'❌ Acción desconocida: {accion}'
        }, status=400)



