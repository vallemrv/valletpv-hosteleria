# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:09:05+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-06T19:08:26+02:00
# @License: Apache License v2.0

import json
from gestion.models.mesas import Mesas, Zonas, Mesasabiertas
from gestion.models.pedidos import Lineaspedido
from push_telegram.models import TelegramAutorizacion, TelegramUser
from gestion.models.camareros import Camareros
from push_telegram.push_sender import editar_mensaje_mesa
from django.utils import timezone
from gestion.tools.config_logs import configurar_logging
from gestion.decorators.log_excepciones import log_excepciones

from tokenapi.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

# Logger personalizado para este módulo
logger = configurar_logging("api_mesas")

@csrf_exempt
def ls_zonas(request):
    lsZonas = []
    for z in Zonas.objects.all():
        obj = {
            "ID": z.id,
            "Nombre": z.nombre,
            "Tarifa": z.tarifa,
            "Color": "",
            "RGB": z.rgb,
        }
        lsZonas.append(obj)
    return JsonResponse(lsZonas)

@csrf_exempt
def lstodaslasmesas(request):
    return JsonResponse([])

@csrf_exempt
def lsmesasabiertas(request):
    lstObj = []
    if 'idz' in request.POST:
        idz = request.POST["idz"]
        mesas = Mesasabiertas.objects.filter(mesa__mesaszona__zona__pk=idz)
    else:
        mesas = Mesasabiertas.objects.all()

    for m in mesas:
        obj = {
            "ID": m.id,
            "UID": m.infmesa.pk,
            "IDMesa": m.mesa_id,
            "num": m.infmesa.numcopias,
            "Hora": m.infmesa.hora,
            "NomMesa": m.mesa.nombre,
            "RGB": m.mesa.mesaszona_set.all().first().zona.rgb

        }
        lstObj.append(obj)

    return JsonResponse(lstObj)

@csrf_exempt
@log_excepciones("api_mesas.log")
def mesa_action(request):
    """
    Endpoint unificado para acciones de mesa (borrar/mantener).
    Recibe token y accion del webhook de Telegram.
    """
    if request.method != 'POST':
        return JsonResponse({'error': 'Método no permitido'}, status=405)
    
    # Intentar obtener parámetros de POST o JSON body
    token = request.POST.get('token')
    accion = request.POST.get('accion')
    
    # Si no están en POST, intentar leer JSON body
    if not token or not accion:
        try:
            if request.body:
                data = json.loads(request.body)
                token = data.get('token')
                accion = data.get('accion')
        except Exception as e:
            logger.warning(f"Error parseando JSON body: {e}")
    
    if not token:
        logger.warning("Intento de acción de mesa sin token")
        return JsonResponse({
            'error': 'Parámetro requerido: token',
            'success': False,
            'mensaje': '❌ Token no recibido'
        }, status=400)
    
    if not accion:
        logger.warning("Intento de acción de mesa sin especificar acción")
        return JsonResponse({
            'error': 'Parámetro requerido: accion',
            'success': False,
            'mensaje': '❌ Acción no especificada'
        }, status=400)
    
    # Limpiar token
    token = token.strip()
    
    logger.info(f"Solicitud de acción de mesa '{accion}' con token: '{token}'")
    
    # Buscar y validar autorización
    try:
        autorizacion = TelegramAutorizacion.objects.get(token=token)
    except TelegramAutorizacion.DoesNotExist:
        logger.error(f"Token no encontrado en BD: '{token}'")
        # Debug: listar últimos tokens creados para ver si hay alguno parecido
        last_tokens = TelegramAutorizacion.objects.all().order_by('-created_at')[:5]
        logger.info(f"Últimos 5 tokens en BD: {[t.token for t in last_tokens]}")
        
        # Devolver 200 con success=False para evitar 404 en el cliente
        return JsonResponse({
            'error': 'Token no válido o expirado',
            'success': False,
            'mensaje': '❌ Token no válido o expirado'
        }, status=200)
    
    # Validar que sea válido
    if not autorizacion.is_valida():
        logger.warning(f"Token inválido o expirado: {token}")
        
        # Intentar editar el mensaje para indicar que expiró
        try:
            editar_mensaje_mesa(
                telegram_user_id=autorizacion.telegram_user_id,
                message_id=autorizacion.telegram_message_id,
                mesa_nombre="Desconocida",
                accion='expirada'
            )
        except Exception as e:
            logger.warning(f"No se pudo editar mensaje expirado: {e}")
            
        return JsonResponse({
            'error': 'Token expirado o ya usado',
            'success': False,
            'mensaje': '❌ Token expirado o ya utilizado'
        }, status=200)
    
    # Obtener infmesa_id del dispositivo (en este caso es el ID de la mesa)
    infmesa_id = autorizacion.uid_dispositivo
    
    # Marcar token como usado
    autorizacion.usada = True
    autorizacion.usada_en = timezone.now()
    autorizacion.save(update_fields=['usada', 'usada_en'])
    
    # Ejecutar acción
    if accion in ['borrar', 'borrar_mesa']:
        try:
            # Buscar la mesa abierta por infmesa_id
            mesa_abierta = Mesasabiertas.objects.filter(infmesa_id=infmesa_id).first()
            
            if not mesa_abierta:
                logger.warning(f"Mesa no encontrada con infmesa_id: {infmesa_id}")
                return JsonResponse({
                    'error': 'Mesa no encontrada',
                    'success': False,
                    'mensaje': '❌ Mesa no encontrada o ya cerrada'
                }, status=404)
            
            mesa_nombre = mesa_abierta.mesa.nombre
            mesa_id = mesa_abierta.mesa_id
            
            # Obtener camarero adecuado para la acción
            
            # 1. Buscar camarero con permiso de borrar mesa
            camarero = Camareros.objects.filter(permisos__contains="borrar_mesa", activo=1).first()
            
            # 2. Si no hay, usar el primer camarero activo
            if not camarero:
                camarero = Camareros.objects.filter(activo=1).first()
            
            # ID del camarero a usar
            idc = camarero.id if camarero else 0
            nombre_camarero = f"{camarero.nombre} {camarero.apellidos}" if camarero else "Sistema"
            
            # Obtener nombre del usuario de Telegram
            telegram_user = TelegramUser.objects.filter(telegram_user_id=autorizacion.telegram_user_id).first()
            nombre_telegram = telegram_user.nombre if telegram_user else f"ID {autorizacion.telegram_user_id}"
            
            # Borrar la mesa usando el método existente
            Mesasabiertas.borrar_mesa_abierta(
                idm=mesa_id,
                idc=idc,
                motivo=f"Borrado desde Telegram por {nombre_camarero} (Usuario Telegram: {nombre_telegram})"
            )
            
            logger.info(f"🗑️ Mesa borrada - ID: {mesa_id}, Nombre: {mesa_nombre}")
            
            # Editar mensaje de Telegram
            try:
                editar_mensaje_mesa(
                    telegram_user_id=autorizacion.telegram_user_id,
                    message_id=autorizacion.telegram_message_id,
                    mesa_nombre=mesa_nombre,
                    accion='borrada'
                )
            except Exception as e:
                logger.warning(f"Error editando mensaje: {e}")
            
            return JsonResponse({
                'success': True,
                'mensaje': f'🗑️ Mesa {mesa_nombre} borrada correctamente',
                'mesa_id': mesa_id,
                'mesa_nombre': mesa_nombre
            })
            
        except Exception as e:
            logger.error(f"Error borrando mesa: {e}")
            return JsonResponse({
                'error': str(e),
                'success': False,
                'mensaje': f'❌ Error borrando mesa: {str(e)}'
            }, status=500)
    
    elif accion in ['mantener', 'mantener_mesa']:
        logger.info(f"✅ Mesa mantenida - infmesa_id: {infmesa_id}")
        
        # Editar mensaje de Telegram
        try:
            mesa_abierta = Mesasabiertas.objects.filter(infmesa_id=infmesa_id).first()
            mesa_nombre = mesa_abierta.mesa.nombre if mesa_abierta else "Desconocida"
            
            editar_mensaje_mesa(
                telegram_user_id=autorizacion.telegram_user_id,
                message_id=autorizacion.telegram_message_id,
                mesa_nombre=mesa_nombre,
                accion='mantenida'
            )
        except Exception as e:
            logger.warning(f"Error editando mensaje: {e}")
        
        return JsonResponse({
            'success': True,
            'mensaje': '✅ Mesa mantenida correctamente',
            'infmesa_id': infmesa_id
        })

    elif accion == 'borrar_lineas':
        try:
            # Formato esperado: LINEAS:idm:id1,id2,id3
            if not infmesa_id.startswith("LINEAS:"):
                return JsonResponse({'error': 'Formato de datos inválido', 'success': False}, status=400)
            
            parts = infmesa_id.split(":")
            if len(parts) < 3:
                return JsonResponse({'error': 'Datos incompletos', 'success': False}, status=400)
            
            mesa_id = int(parts[1])
            ids_str = parts[2]
            ids = [int(x) for x in ids_str.split(",")]
            
            # Obtener camarero adecuado para la acción
            
            # 1. Buscar camarero con permiso de borrar línea
            camarero = Camareros.objects.filter(permisos__contains="borrar_linea", activo=1).first()
            
            # 2. Si no hay, usar el primer camarero activo
            if not camarero:
                camarero = Camareros.objects.filter(activo=1).first()
            
            # ID del camarero a usar (o 0 si no hay ninguno, aunque debería haber)
            idc = camarero.id if camarero else 0
            nombre_camarero = f"{camarero.nombre} {camarero.apellidos}" if camarero else "Sistema"
            
            # Obtener nombre del usuario de Telegram
            telegram_user = TelegramUser.objects.filter(telegram_user_id=autorizacion.telegram_user_id).first()
            nombre_telegram = telegram_user.nombre if telegram_user else f"ID {autorizacion.telegram_user_id}"
            
            # Borrar líneas
            Lineaspedido.borrar_linea_pedido_by_ids(
                idm=mesa_id,
                ids=ids,
                idc=idc,
                motivo=f"Líneas borradas desde Telegram por {nombre_camarero} (Usuario Telegram: {nombre_telegram})"
            )
            
            logger.info(f"🗑️ Líneas borradas - Mesa ID: {mesa_id}, IDs: {ids}")
            
            # Editar mensaje de Telegram
            try:
                editar_mensaje_mesa(
                    telegram_user_id=autorizacion.telegram_user_id,
                    message_id=autorizacion.telegram_message_id,
                    mesa_nombre=f"Mesa {mesa_id}",
                    accion='lineas_borradas'
                )
            except Exception as e:
                logger.warning(f"Error editando mensaje: {e}")
            
            return JsonResponse({
                'success': True,
                'mensaje': '🗑️ Líneas borradas correctamente'
            })
            
        except Exception as e:
            logger.error(f"Error borrando líneas: {e}")
            return JsonResponse({
                'error': str(e),
                'success': False,
                'mensaje': f'❌ Error borrando líneas: {str(e)}'
            }, status=500)

    elif accion == 'mantener_lineas':
        logger.info(f"✅ Líneas mantenidas")
        
        # Editar mensaje de Telegram
        try:
            editar_mensaje_mesa(
                telegram_user_id=autorizacion.telegram_user_id,
                message_id=autorizacion.telegram_message_id,
                mesa_nombre="Mesa",
                accion='lineas_mantenidas'
            )
        except Exception as e:
            logger.warning(f"Error editando mensaje: {e}")
        
        return JsonResponse({
            'success': True,
            'mensaje': '✅ Líneas mantenidas correctamente'
        })
    
    else:
        logger.warning(f"Acción de mesa desconocida: {accion}")
        return JsonResponse({
            'error': f'Acción desconocida: {accion}',
            'success': False,
            'mensaje': f'❌ Acción desconocida: {accion}'
        }, status=400)
