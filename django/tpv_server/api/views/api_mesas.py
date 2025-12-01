# @Author: Manuel Rodriguez <valle>
# @Date:   2018-12-30T02:09:05+01:00
# @Email:  valle.mrv@gmail.com
# @Last modified by:   valle
# @Last modified time: 2019-10-06T19:08:26+02:00
# @License: Apache License v2.0

import json
from gestion.models.mesas import Mesas, Zonas, Mesasabiertas
from gestion.models.pedidos import Lineaspedido
from push_telegram.models import TelegramUser
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
    infmesa_id = request.POST.get('uid_dispositivo')
    telegram_user_id = request.POST.get('telegram_user_id')
    
    logger.info(f"mesa_action POST: {request.POST}")
    
    # Si no están en POST, intentar leer JSON body
    if not token or not accion:
        try:
            if request.body:
                data = json.loads(request.body)
                token = data.get('token')
                accion = data.get('accion')
                infmesa_id = data.get('uid_dispositivo')
                telegram_user_id = data.get('telegram_user_id')
                
                # Si hay metadata en el JSON body
                if not infmesa_id and 'metadata' in data:
                    meta = data['metadata']
                    if isinstance(meta, str):
                        try:
                            meta = json.loads(meta)
                        except:
                            pass
                    if isinstance(meta, dict):
                        infmesa_id = meta.get('infmesa_id') or meta.get('uid')
        except Exception as e:
            logger.warning(f"Error parseando JSON body: {e}")
            
    # Intentar recuperar infmesa_id de metadata en POST si no vino directo
    if not infmesa_id and request.POST.get('metadata'):
        try:
            meta_str = request.POST.get('metadata')
            meta = json.loads(meta_str)
            infmesa_id = meta.get('infmesa_id') or meta.get('uid')
            logger.info(f"Recuperado infmesa_id de metadata: {infmesa_id}")
        except Exception as e:
            logger.warning(f"Error parseando metadata: {e}")
    
    if not token or not accion:
        return JsonResponse({
            'error': 'Faltan parámetros requeridos',
            'success': False,
            'mensaje': '❌ Datos incompletos'
        }, status=400)
    
    if not infmesa_id:
        # Fallback para compatibilidad si no llega uid_dispositivo
        logger.warning("No se recibió uid_dispositivo en mesa_action")
        return JsonResponse({
            'error': 'Falta uid_dispositivo',
            'success': False,
            'mensaje': '❌ Error de datos'
        }, status=400)

    logger.info(f"Acción '{accion}' para uid '{infmesa_id}' (Token: {token})")
    
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
            camarero = Camareros.objects.filter(permisos__contains="borrar_mesa", activo=1).first()
            if not camarero:
                camarero = Camareros.objects.filter(activo=1).first()
            
            idc = camarero.id if camarero else 0
            nombre_camarero = f"{camarero.nombre} {camarero.apellidos}" if camarero else "Sistema"
            
            # Obtener nombre del usuario de Telegram
            nombre_telegram = f"ID {telegram_user_id}"
            if telegram_user_id:
                telegram_user = TelegramUser.objects.filter(telegram_user_id=telegram_user_id).first()
                if telegram_user:
                    nombre_telegram = telegram_user.nombre
            
            # Borrar la mesa
            Mesasabiertas.borrar_mesa_abierta(
                idm=mesa_id,
                idc=idc,
                motivo=f"Borrado desde Telegram por {nombre_camarero} (Usuario Telegram: {nombre_telegram})"
            )
            
            logger.info(f"🗑️ Mesa borrada - ID: {mesa_id}, Nombre: {mesa_nombre}")
            
            # Texto para actualizar el mensaje en Telegram
            new_text = f"🗑️ Mesa {mesa_nombre} ha sido BORRADA\n\n✅ Acción completada exitosamente."
            
            return JsonResponse({
                'success': True,
                'mensaje': f'🗑️ Mesa {mesa_nombre} borrada correctamente',
                'new_text': new_text,
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
        
        # Intentar obtener nombre para el mensaje
        mesa_nombre = "Mesa"
        mesa_abierta = Mesasabiertas.objects.filter(infmesa_id=infmesa_id).first()
        if mesa_abierta:
            mesa_nombre = mesa_abierta.mesa.nombre
            
        new_text = f"✅ Mesa {mesa_nombre} se ha MANTENIDO\n\n✅ La mesa sigue activa."
        
        return JsonResponse({
            'success': True,
            'mensaje': '✅ Mesa mantenida correctamente',
            'new_text': new_text,
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
            
            # Obtener camarero
            camarero = Camareros.objects.filter(permisos__contains="borrar_linea", activo=1).first()
            if not camarero:
                camarero = Camareros.objects.filter(activo=1).first()
            
            idc = camarero.id if camarero else 0
            nombre_camarero = f"{camarero.nombre} {camarero.apellidos}" if camarero else "Sistema"
            
            # Obtener nombre del usuario de Telegram
            nombre_telegram = f"ID {telegram_user_id}"
            if telegram_user_id:
                telegram_user = TelegramUser.objects.filter(telegram_user_id=telegram_user_id).first()
                if telegram_user:
                    nombre_telegram = telegram_user.nombre
            
            # Borrar líneas
            Lineaspedido.borrar_linea_pedido_by_ids(
                idm=mesa_id,
                ids=ids,
                idc=idc,
                motivo=f"Líneas borradas desde Telegram por {nombre_camarero} (Usuario Telegram: {nombre_telegram})"
            )
            
            logger.info(f"🗑️ Líneas borradas - Mesa ID: {mesa_id}, IDs: {ids}")
            
            new_text = f"🗑️ Líneas de Mesa {mesa_id} han sido BORRADAS\n\n✅ Acción completada exitosamente."
            
            return JsonResponse({
                'success': True,
                'mensaje': '🗑️ Líneas borradas correctamente',
                'new_text': new_text
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
        
        new_text = f"✅ Líneas se han MANTENIDO\n\n✅ Las líneas siguen activas."
        
        return JsonResponse({
            'success': True,
            'mensaje': '✅ Líneas mantenidas correctamente',
            'new_text': new_text
        })
    
    else:
        logger.warning(f"Acción de mesa desconocida: {accion}")
        return JsonResponse({
            'error': f'Acción desconocida: {accion}',
            'success': False,
            'mensaje': f'❌ Acción desconocida: {accion}'
        }, status=400)
