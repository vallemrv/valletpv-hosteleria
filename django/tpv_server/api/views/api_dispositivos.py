import uuid
from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.http import HttpResponse
from django.utils import timezone
from gestion.models.dispositivos import Dispositivo
from django.conf import settings
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
    Endpoint para activar un dispositivo.
    """
    if request.method != 'POST' and request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Obtener token del request (para logging) y uid
    token = request.POST.get('token') or request.GET.get('token')
    uid = request.POST.get('uid_dispositivo') or request.GET.get('uid')
    
    if not uid:
        logger.warning("Intento de activación sin uid")
        return JsonResponse({'error': 'Parámetro requerido: uid_dispositivo', 'success': False}, status=400)
    
    logger.info(f"Solicitud de activación para UID: {uid}")
    
    # Buscar el dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {uid}")
        return JsonResponse({'error': 'Dispositivo no encontrado', 'success': False}, status=404)
    
    # Activar dispositivo
    was_active = dispositivo.activo
    dispositivo.activo = True
    dispositivo.save(update_fields=['activo'])
    
    logger.info(f"✅ Dispositivo activado - UID: {dispositivo.uid}, Was active: {was_active}")
    
    # Construir texto para actualizar mensaje
    estado = 'ACTIVO'
    prefijo = '✅'
    verbo = 'activado'
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    
    if was_active:
        new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>El dispositivo ya estaba {estado}</b>"
    else:
        new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>Dispositivo {verbo.upper()}</b>\n📊 <b>Estado actual:</b> {estado}"

    return JsonResponse({
        'success': True,
        'message': 'Dispositivo activado correctamente' if not was_active else 'Dispositivo ya estaba activo',
        'uid': dispositivo.uid,
        'descripcion': dispositivo.descripcion,
        'activo': dispositivo.activo,
        'was_active': was_active,
        'new_text': new_text
    })


@csrf_exempt
@log_excepciones("api_dispositivos.log")
def deactivate_device(request):
    """
    Endpoint para desactivar un dispositivo.
    """
    if request.method != 'POST' and request.method != 'GET':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Obtener token del request (para logging) y uid
    token = request.GET.get('token') or request.POST.get('token')
    uid = request.POST.get('uid_dispositivo') or request.GET.get('uid')
    
    if not uid:
        logger.warning("Intento de desactivación sin uid")
        return JsonResponse({'error': 'Parámetro requerido: uid_dispositivo', 'success': False}, status=400)
    
    logger.info(f"Solicitud de desactivación para UID: {uid}")
    
    # Buscar el dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {uid}")
        return JsonResponse({'error': 'Dispositivo no encontrado', 'success': False}, status=404)
    
    # Desactivar dispositivo
    was_inactive = not dispositivo.activo
    dispositivo.activo = False
    dispositivo.save(update_fields=['activo'])
    
    logger.info(f"⏸️ Dispositivo desactivado - UID: {dispositivo.uid}, Was inactive: {was_inactive}")
    
    # Construir texto para actualizar mensaje
    estado = 'INACTIVO'
    prefijo = '🛑'
    verbo = 'desactivado'
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    
    if was_inactive:
        new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>El dispositivo ya estaba {estado}</b>"
    else:
        new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>Dispositivo {verbo.upper()}</b>\n📊 <b>Estado actual:</b> {estado}"
    
    return JsonResponse({
        'success': True,
        'message': 'Dispositivo desactivado correctamente' if not was_inactive else 'Dispositivo ya estaba inactivo',
        'uid': dispositivo.uid,
        'descripcion': dispositivo.descripcion,
        'activo': dispositivo.activo,
        'was_inactive': was_inactive,
        'new_text': new_text
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
    uid = request.POST.get('uid_dispositivo')
    
    # Si no están en POST, intentar leer JSON body
    if not token or not accion:
        try:
            if request.body:
                import json
                data = json.loads(request.body)
                token = data.get('token')
                accion = data.get('accion')
                uid = data.get('uid_dispositivo')
        except Exception as e:
            logger.warning(f"Error parseando JSON body: {e}")

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
        
    if not uid:
        logger.warning("Intento de acción sin uid_dispositivo")
        return JsonResponse({
            'error': 'Parámetro requerido: uid_dispositivo',
            'success': False,
            'mensaje': '❌ UID no recibido'
        }, status=400)
    
    logger.info(f"Solicitud de acción '{accion}' para UID: {uid}")
    
    # Buscar dispositivo
    try:
        dispositivo = Dispositivo.objects.get(uid=uid)
    except Dispositivo.DoesNotExist:
        logger.error(f"Dispositivo no encontrado: {uid}")
        return JsonResponse({
            'error': 'Dispositivo no encontrado',
            'success': False,
            'mensaje': '❌ Dispositivo no encontrado'
        }, status=404)
    
    empresa = getattr(settings, 'EMPRESA', 'testTPV')
    
    # Ejecutar acción
    if accion == 'activate':
        was_active = dispositivo.activo
        dispositivo.activo = True
        dispositivo.save(update_fields=['activo'])
        
        logger.info(f"✅ Dispositivo activado - UID: {dispositivo.uid}")
        
        estado = 'ACTIVO'
        prefijo = '✅'
        verbo = 'activado'
        
        if was_active:
            new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>El dispositivo ya estaba {estado}</b>"
        else:
            new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>Dispositivo {verbo.upper()}</b>\n📊 <b>Estado actual:</b> {estado}"
        
        return JsonResponse({
            'success': True,
            'mensaje': '✅ Dispositivo activado' if not was_active else '✅ Ya estaba activo',
            'uid': dispositivo.uid,
            'descripcion': dispositivo.descripcion,
            'activo': True,
            'new_text': new_text
        })
        
    elif accion == 'deactivate':
        was_inactive = not dispositivo.activo
        dispositivo.activo = False
        dispositivo.save(update_fields=['activo'])
        
        logger.info(f"⏸️ Dispositivo desactivado - UID: {dispositivo.uid}")
        
        estado = 'INACTIVO'
        prefijo = '🛑'
        verbo = 'desactivado'
        
        if was_inactive:
            new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>El dispositivo ya estaba {estado}</b>"
        else:
            new_text = f"🆕 <b>[NUEVO DISPOSITIVO DETECTADO]</b>\n\n📱 <b>UID:</b> <code>{uid}</code>\n📝 <b>Descripción:</b> {dispositivo.descripcion}\n🏢 <b>Empresa:</b> {empresa}\n\n{prefijo} <b>Dispositivo {verbo.upper()}</b>\n📊 <b>Estado actual:</b> {estado}"
        
        return JsonResponse({
            'success': True,
            'mensaje': '⏸️ Dispositivo desactivado' if not was_inactive else '⏸️ Ya estaba inactivo',
            'uid': dispositivo.uid,
            'descripcion': dispositivo.descripcion,
            'activo': False,
            'new_text': new_text
        })
        
    else:
        logger.warning(f"Acción desconocida: {accion}")
        return JsonResponse({
            'error': f'Acción desconocida: {accion}',
            'success': False,
            'mensaje': f'❌ Acción desconocida: {accion}'
        }, status=400)



