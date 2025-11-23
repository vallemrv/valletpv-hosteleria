"""
Tools de LangChain para gestionar suscripciones de notificaciones push de Telegram.
Permite al chatbot gestionar suscripciones de usuarios a eventos.

NOTA: Los eventos se gestionan mediante TELEGRAM_HOOKS en local_config.py
      y el comando: python manage_testTPV.py init_telegram_events
"""

from langchain_core.tools import tool
from typing import Optional, List
from django.utils import timezone
from push_telegram.models import (
    TelegramEventType, 
    TelegramSubscription, 
    TelegramNotificationLog,
    TelegramUser,
    TelegramAutorizacion
)
from push_telegram.push_sender import enviar_push_telegram


@tool
def listar_usuarios_telegram() -> str:
    """
    Lista todos los usuarios de Telegram registrados en el sistema.
    Útil para ver qué usuarios están disponibles antes de suscribirlos.
    
    Returns:
        str: Lista de usuarios con sus IDs de Telegram
    """
    usuarios = TelegramUser.objects.filter(activo=True)
    
    if not usuarios.exists():
        return "No hay usuarios de Telegram registrados.\nRegistra usuarios en el admin de Django."
    
    resultado = ["👥 Usuarios de Telegram registrados:\n"]
    for usuario in usuarios:
        subs_count = usuario.subscriptions.filter(activo=True).count()
        desc = f" - {usuario.descripcion}" if usuario.descripcion else ""
        
        # Contar autorizaciones activas
        auth_count = TelegramAutorizacion.objects.filter(
            telegram_user_id=usuario.telegram_user_id,
            usada=False,
            expirada=False
        ).count()
        
        resultado.append(
            f"• {usuario.nombre} (ID: {usuario.telegram_user_id}){desc}\n"
            f"  📋 Suscripciones: {subs_count} | 🔐 Autorizaciones: {auth_count}"
        )
    
    return "\n".join(resultado)


@tool
def registrar_usuario_telegram(nombre: str, telegram_user_id: int, descripcion: str = "") -> str:
    """
    Registra un nuevo usuario de Telegram en el sistema.
    
    Args:
        nombre: Nombre del usuario (ej: Valle, Admin)
        telegram_user_id: ID numérico de Telegram
        descripcion: Descripción opcional del usuario
        
    Returns:
        str: Confirmación de registro
    """
    try:
        usuario, created = TelegramUser.objects.update_or_create(
            nombre=nombre,
            defaults={
                'telegram_user_id': telegram_user_id,
                'descripcion': descripcion,
                'activo': True
            }
        )
        
        if created:
            return f"✅ Usuario '{nombre}' registrado con ID {telegram_user_id}"
        else:
            return f"✅ Usuario '{nombre}' actualizado con ID {telegram_user_id}"
            
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def modificar_usuario_telegram(
    nombre_actual: str,
    nuevo_nombre: Optional[str] = None,
    nuevo_telegram_id: Optional[int] = None,
    nueva_descripcion: Optional[str] = None,
    activo: Optional[bool] = None
) -> str:
    """
    Modifica los datos de un usuario de Telegram existente.
    
    Args:
        nombre_actual: Nombre actual del usuario a modificar
        nuevo_nombre: Nuevo nombre (opcional)
        nuevo_telegram_id: Nuevo ID de Telegram (opcional)
        nueva_descripcion: Nueva descripción (opcional)
        activo: Activar/desactivar usuario (opcional)
        
    Returns:
        str: Confirmación de modificación
    """
    try:
        usuario = TelegramUser.objects.get(nombre__iexact=nombre_actual)
        
        cambios = []
        if nuevo_nombre:
            usuario.nombre = nuevo_nombre
            cambios.append(f"nombre → '{nuevo_nombre}'")
        
        if nuevo_telegram_id:
            usuario.telegram_user_id = nuevo_telegram_id
            cambios.append(f"ID → {nuevo_telegram_id}")
        
        if nueva_descripcion is not None:
            usuario.descripcion = nueva_descripcion
            cambios.append(f"descripción → '{nueva_descripcion}'")
        
        if activo is not None:
            usuario.activo = activo
            estado = "activado" if activo else "desactivado"
            cambios.append(f"estado → {estado}")
        
        if not cambios:
            return "⚠️ No se especificaron cambios"
        
        usuario.save()
        return f"✅ Usuario '{nombre_actual}' modificado:\n• " + "\n• ".join(cambios)
        
    except TelegramUser.DoesNotExist:
        return f"❌ Usuario '{nombre_actual}' no encontrado"
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def eliminar_usuario_telegram(nombre: str) -> str:
    """
    Elimina un usuario de Telegram del sistema.
    También elimina todas sus suscripciones.
    
    Args:
        nombre: Nombre del usuario a eliminar
        
    Returns:
        str: Confirmación de eliminación
    """
    try:
        usuario = TelegramUser.objects.get(nombre__iexact=nombre)
        telegram_id = usuario.telegram_user_id
        
        # Contar suscripciones antes de eliminar (se borrarán automáticamente por CASCADE)
        subs_count = usuario.subscriptions.count()
        
        # Eliminar usuario (borra suscripciones automáticamente)
        usuario.delete()
        
        return f"✅ Usuario '{nombre}' eliminado (ID: {telegram_id})\n• {subs_count} suscripciones eliminadas"
        
    except TelegramUser.DoesNotExist:
        return f"❌ Usuario '{nombre}' no encontrado"
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def listar_eventos_telegram() -> str:
    """
    Lista todos los tipos de eventos de Telegram disponibles y su estado.
    Útil para ver qué eventos existen antes de crear suscripciones.
    
    NOTA: Los eventos se crean mediante internamente
          
    Returns:
        str: Lista formateada de eventos con su estado
    """
    eventos = TelegramEventType.objects.all()
    
    if not eventos.exists():
        return "No hay eventos de Telegram configurados.\nEjecuta: python manage_testTPV.py init_telegram_events"
    
    resultado = ["📋 Eventos de Telegram disponibles:\n"]
    for evento in eventos:
        estado = "✅ Activo" if evento.activo else "❌ Inactivo"
        subs_count = evento.subscriptions.filter(activo=True).count()
        resultado.append(
            f"• {evento.code} - {evento.nombre}\n"
            f"  Estado: {estado}\n"
            f"  Suscriptores: {subs_count}\n"
            f"  Descripción: {evento.descripcion or 'Sin descripción'}\n"
        )
    
    return "\n".join(resultado)


@tool
def listar_zonas_disponibles() -> str:
    """
    Lista todas las zonas disponibles en el sistema.
    Útil para saber qué IDs de zona usar en los filtros de suscripciones.
    
    Returns:
        str: Lista de zonas con sus IDs
    """
    try:
        from gestion.models.mesas import Zonas
        zonas = Zonas.objects.all().order_by('id')
        
        if not zonas.exists():
            return "No hay zonas configuradas en el sistema."
        
        resultado = ["🎯 Zonas disponibles:\n"]
        for zona in zonas:
            resultado.append(f"• ID: {zona.id} - {zona.nombre}")
        
        resultado.append("\n💡 Usa estos IDs para configurar filtros en suscripciones.")
        return "\n".join(resultado)
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def listar_suscripciones_telegram(telegram_user_id: Optional[int] = None) -> str:
    """
    Lista las suscripciones de Telegram con sus filtros aplicados.
    Puede filtrar por usuario específico o mostrar todas.
    
    Args:
        telegram_user_id: ID del usuario de Telegram (opcional)
        
    Returns:
        str: Lista de suscripciones con filtros
    """
    try:
        if telegram_user_id:
            suscripciones = TelegramSubscription.objects.filter(
                usuario__telegram_user_id=telegram_user_id
            ).select_related('event_type', 'usuario')
            titulo = f"📱 Suscripciones del usuario {telegram_user_id}:\n"
        else:
            suscripciones = TelegramSubscription.objects.all().select_related('event_type', 'usuario')
            titulo = "📱 Todas las suscripciones:\n"
        
        if not suscripciones.exists():
            return "No hay suscripciones configuradas."
        
        resultado = [titulo]
        for sub in suscripciones:
            estado = "✅" if sub.activo else "❌"
            nombre = sub.usuario.nombre
            
            # Mostrar filtros si existen
            filtro_info = ""
            if sub.filtros:
                if 'zonas' in sub.filtros:
                    zonas_ids = sub.filtros['zonas']
                    if isinstance(zonas_ids, list):
                        from gestion.models.mesas import Zonas
                        zonas = Zonas.objects.filter(pk__in=zonas_ids).values_list('nombre', flat=True)
                        filtro_info = f" [Zonas: {', '.join(zonas)}]"
                    else:
                        filtro_info = f" [Zona ID: {zonas_ids}]"
                else:
                    # Otros filtros genéricos
                    filtros_str = ', '.join([f"{k}: {v}" for k, v in sub.filtros.items()])
                    filtro_info = f" [{filtros_str}]"
            else:
                filtro_info = " [Sin filtros]"
            
            resultado.append(
                f"{estado} {nombre} → {sub.event_type.nombre} ({sub.event_type.code}){filtro_info}"
            )
        
        return "\n".join(resultado)
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def suscribir_usuario_telegram(
    nombre_o_id: str,
    event_code: str,
    zonas: Optional[List[int]] = None
) -> str:
    """
    Suscribe un usuario de Telegram a un tipo de evento con filtros opcionales.
    El usuario recibirá notificaciones cuando ocurra ese evento.
    
    Args:
        nombre_o_id: Nombre del usuario (ej: 'Valle') o ID numérico de Telegram
        event_code: Código del evento al que suscribir (ej: 'cambio_zona', 'nuevo_dispositivo')
        zonas: Lista de IDs de zonas a vigilar. Si es None o vacío, vigila todas las zonas.
               Ejemplo: [5, 8] para vigilar solo zonas 5 y 8
        
    Returns:
        str: Confirmación de suscripción
        
    Ejemplos:
        - suscribir_usuario_telegram("Valle", "cambio_zona", [5])
          → Vigila solo cambios a zona ID 5
        - suscribir_usuario_telegram("Valle", "cambio_zona", [5, 8])
          → Vigila cambios a zonas 5 y 8
        - suscribir_usuario_telegram("Valle", "cambio_zona")
          → Vigila cambios a todas las zonas
    """
    try:
        # Buscar usuario por nombre o ID
        usuario = None
        
        # Intentar buscar por nombre primero
        try:
            usuario = TelegramUser.objects.get(nombre__iexact=nombre_o_id, activo=True)
        except TelegramUser.DoesNotExist:
            # Intentar convertir a ID numérico
            try:
                telegram_user_id = int(nombre_o_id)
                usuario = TelegramUser.objects.get(telegram_user_id=telegram_user_id, activo=True)
            except (ValueError, TelegramUser.DoesNotExist):
                return f"❌ Usuario '{nombre_o_id}' no encontrado. Usa listar_usuarios_telegram() o registra el usuario primero."
        
        # Buscar el evento
        try:
            evento = TelegramEventType.objects.get(code=event_code)
        except TelegramEventType.DoesNotExist:
            return f"❌ No existe el evento '{event_code}'. Usa listar_eventos_telegram() para ver eventos disponibles."
        
        # Preparar filtros
        filtros = {}
        if zonas:
            # Validar que las zonas existen
            from gestion.models.mesas import Zonas
            zonas_validas = Zonas.objects.filter(pk__in=zonas).values_list('id', flat=True)
            if len(zonas_validas) != len(zonas):
                zonas_invalidas = set(zonas) - set(zonas_validas)
                return f"⚠️ Las siguientes zonas no existen: {zonas_invalidas}. Usa listar_zonas_disponibles()"
            
            filtros['zonas'] = list(zonas_validas)
        
        # Crear o actualizar suscripción
        suscripcion, created = TelegramSubscription.objects.update_or_create(
            usuario=usuario,
            event_type=evento,
            defaults={
                'activo': True,
                'filtros': filtros
            }
        )
        
        # Mensaje informativo sobre filtros
        filtro_msg = ""
        if filtros and 'zonas' in filtros:
            from gestion.models.mesas import Zonas
            zonas_nombres = Zonas.objects.filter(pk__in=filtros['zonas']).values_list('nombre', flat=True)
            filtro_msg = f"\n🎯 Vigilando zonas: {', '.join(zonas_nombres)}"
        else:
            filtro_msg = "\n🎯 Vigilando todas las zonas"
        
        if created:
            return f"✅ Usuario {usuario.nombre} suscrito a '{evento.nombre}'{filtro_msg}"
        else:
            return f"✅ Suscripción actualizada para {usuario.nombre} → '{evento.nombre}'{filtro_msg}"
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def configurar_filtro_zonas(
    nombre_o_id: str,
    event_code: str,
    zonas: Optional[List[int]] = None
) -> str:
    """
    Configura o actualiza los filtros por zonas para una suscripción existente.
    
    Args:
        nombre_o_id: Nombre del usuario (ej: 'Valle') o ID numérico de Telegram
        event_code: Código del evento
        zonas: Lista de IDs de zonas a vigilar. Si es None o vacío, vigilará todas las zonas.
        
    Returns:
        str: Confirmación de actualización
        
    Ejemplos:
        - configurar_filtro_zonas("Valle", "cambio_zona", [5])
          → Solo vigila zona 5 (Barra)
        - configurar_filtro_zonas("Valle", "cambio_zona", [5, 8])
          → Vigila zonas 5 y 8
        - configurar_filtro_zonas("Valle", "cambio_zona", None)
          → Vigila todas las zonas (elimina filtro)
    """
    try:
        # Buscar usuario por nombre o ID
        usuario = None
        try:
            usuario = TelegramUser.objects.get(nombre__iexact=nombre_o_id, activo=True)
        except TelegramUser.DoesNotExist:
            try:
                telegram_user_id = int(nombre_o_id)
                usuario = TelegramUser.objects.get(telegram_user_id=telegram_user_id, activo=True)
            except (ValueError, TelegramUser.DoesNotExist):
                return f"❌ Usuario '{nombre_o_id}' no encontrado."
        
        # Buscar suscripción existente
        evento = TelegramEventType.objects.get(code=event_code)
        suscripcion = TelegramSubscription.objects.get(
            usuario=usuario,
            event_type=evento
        )
        
        # Preparar nuevos filtros
        nuevos_filtros = {}
        if zonas:
            # Validar zonas
            try:
                from gestion.models.mesas import Zonas
                zonas_validas = Zonas.objects.filter(pk__in=zonas).values_list('id', flat=True)
                if len(zonas_validas) != len(zonas):
                    zonas_invalidas = set(zonas) - set(zonas_validas)
                    return f"⚠️ Las siguientes zonas no existen: {zonas_invalidas}. Usa listar_zonas_disponibles()"
                
                nuevos_filtros['zonas'] = list(zonas_validas)
            except Exception as e:
                return f"❌ Error validando zonas: {str(e)}"
        
        # Actualizar filtros
        suscripcion.filtros = nuevos_filtros
        suscripcion.save(update_fields=['filtros'])
        
        # Mensaje informativo
        if nuevos_filtros and 'zonas' in nuevos_filtros:
            from gestion.models.mesas import Zonas
            zonas_nombres = Zonas.objects.filter(pk__in=nuevos_filtros['zonas']).values_list('nombre', flat=True)
            return f"✅ Filtros actualizados. Ahora vigila zonas: {', '.join(zonas_nombres)}"
        else:
            return f"✅ Filtros eliminados. Ahora vigila todas las zonas"
        
    except TelegramEventType.DoesNotExist:
        return f"❌ No existe el evento '{event_code}'"
    except TelegramSubscription.DoesNotExist:
        return f"❌ El usuario '{nombre_o_id}' no está suscrito a '{event_code}'. Usa suscribir_usuario_telegram() primero."
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def desuscribir_usuario_telegram(nombre_o_id: str, event_code: str) -> str:
    """
    Desuscribe un usuario de Telegram de un tipo de evento.
    El usuario dejará de recibir notificaciones de ese evento.
    
    Args:
        nombre_o_id: Nombre del usuario (ej: 'Valle') o ID numérico de Telegram
        event_code: Código del evento del que desuscribir
        
    Returns:
        str: Confirmación de desuscripción
    """
    try:
        # Buscar usuario por nombre o ID
        usuario = None
        try:
            usuario = TelegramUser.objects.get(nombre__iexact=nombre_o_id, activo=True)
        except TelegramUser.DoesNotExist:
            try:
                telegram_user_id = int(nombre_o_id)
                usuario = TelegramUser.objects.get(telegram_user_id=telegram_user_id, activo=True)
            except (ValueError, TelegramUser.DoesNotExist):
                return f"❌ Usuario '{nombre_o_id}' no encontrado."
        
        evento = TelegramEventType.objects.get(code=event_code)
        suscripcion = TelegramSubscription.objects.get(
            usuario=usuario,
            event_type=evento
        )
        
        suscripcion.activo = False
        suscripcion.save(update_fields=['activo'])
        
        return f"✅ Usuario {usuario.nombre} desuscrito de '{evento.nombre}'"
        
    except TelegramEventType.DoesNotExist:
        return f"❌ No existe el evento '{event_code}'"
    except TelegramSubscription.DoesNotExist:
        return f"❌ El usuario '{nombre_o_id}' no estaba suscrito a '{event_code}'"
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def enviar_notificacion_telegram(event_code: str, mensaje: str, metadata: Optional[dict] = None) -> str:
    """
    Envía una notificación push de Telegram a todos los usuarios suscritos a un evento.
    El mensaje puede incluir formato HTML básico.
    
    Args:
        event_code: Código del evento a notificar
        mensaje: Contenido del mensaje (soporta HTML)
        metadata: Datos adicionales del evento (opcional)
        
    Returns:
        str: Resultado del envío
    """
    try:
        if metadata is None:
            metadata = {}
        
        enviados = enviar_push_telegram(
            event_code=event_code,
            mensaje=mensaje,
            metadata=metadata
        )
        
        if enviados > 0:
            return f"✅ Notificación enviada a {enviados} usuario(s)"
        else:
            return f"⚠️ No se pudo enviar la notificación. Verifica que:\n• El evento '{event_code}' existe y está activo\n• Hay usuarios suscritos\n• El token de Telegram está configurado"
        
    except Exception as e:
        return f"❌ Error enviando notificación: {str(e)}"


@tool
def enviar_notificacion_prueba(nombre_o_id: str, mensaje: str = "🧪 Mensaje de prueba del sistema TPV") -> str:
    """
    Envía una notificación de prueba directamente a un usuario específico.
    No requiere suscripciones ni eventos, útil para verificar conectividad.
    
    Args:
        nombre_o_id: Nombre del usuario (ej: 'Valle') o ID numérico de Telegram
        mensaje: Mensaje de prueba a enviar (opcional)
        
    Returns:
        str: Resultado del envío
    """
    try:
        # Buscar usuario
        usuario = None
        try:
            usuario = TelegramUser.objects.get(nombre__iexact=nombre_o_id, activo=True)
        except TelegramUser.DoesNotExist:
            try:
                telegram_user_id = int(nombre_o_id)
                usuario = TelegramUser.objects.get(telegram_user_id=telegram_user_id, activo=True)
            except (ValueError, TelegramUser.DoesNotExist):
                return f"❌ Usuario '{nombre_o_id}' no encontrado"
        
        # Obtener configuración
        from django.conf import settings
        import requests
        
        telegram_config = getattr(settings, 'TELEGRAM_BOT', {})
        bot_token = telegram_config.get('TOKEN', '')
        
        if not bot_token or bot_token == 'TU_BOT_TOKEN_AQUI':
            return "❌ Token de Telegram no configurado en settings"
        
        # Enviar mensaje de prueba
        url = f"https://api.telegram.org/bot{bot_token}/sendMessage"
        data = {
            'chat_id': usuario.telegram_user_id,
            'text': f"{mensaje}\n\n⏰ Enviado: {timezone.now().strftime('%d/%m/%Y %H:%M:%S')}",
            'parse_mode': 'HTML'
        }
        
        response = requests.post(url, json=data, timeout=10)
        
        if response.status_code == 200:
            return f"✅ Mensaje de prueba enviado a {usuario.nombre} (ID: {usuario.telegram_user_id})"
        else:
            error_msg = response.json().get('description', 'Error desconocido')
            return f"❌ Error enviando mensaje: {error_msg}"
            
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def simular_evento_dispositivo(uid: str, descripcion: str = "Dispositivo de prueba") -> str:
    """
    Simula la detección de un nuevo dispositivo para probar las notificaciones.
    Útil para verificar que el sistema de notificaciones funciona correctamente.
    
    Args:
        uid: UID del dispositivo simulado
        descripcion: Descripción del dispositivo (opcional)
        
    Returns:
        str: Resultado de la simulación
    """
    try:
        from push_telegram.push_sender import notificar_nuevo_dispositivo
        
        enviados = notificar_nuevo_dispositivo(
            uid=uid,
            descripcion=descripcion
        )
        
        if enviados > 0:
            return f"✅ Simulación enviada a {enviados} usuario(s) suscrito(s) a 'nuevo_dispositivo'\n📱 UID simulado: {uid}"
        else:
            return "⚠️ No se enviaron notificaciones. Verifica que:\n• Existe el evento 'nuevo_dispositivo'\n• Hay usuarios suscritos a ese evento\n• La configuración de Telegram es correcta"
            
    except Exception as e:
        return f"❌ Error en simulación: {str(e)}"


@tool
def listar_autorizaciones_telegram(nombre_o_id: Optional[str] = None, solo_activas: bool = True) -> str:
    """
    Lista las autorizaciones temporales de Telegram (tokens para botones).
    Útil para verificar qué autorizaciones están pendientes o han expirado.
    
    Args:
        nombre_o_id: Nombre del usuario o ID de Telegram (opcional, muestra todas si no se especifica)
        solo_activas: Si True, solo muestra autorizaciones no usadas y no expiradas
        
    Returns:
        str: Lista de autorizaciones con su estado
    """
    try:
        # Filtrar por usuario si se especifica
        queryset = TelegramAutorizacion.objects.all()
        
        if nombre_o_id:
            # Intentar buscar por nombre primero
            try:
                usuario = TelegramUser.objects.get(nombre__iexact=nombre_o_id, activo=True)
                queryset = queryset.filter(telegram_user_id=usuario.telegram_user_id)
                titulo = f"🔐 Autorizaciones para {usuario.nombre}:\n"
            except TelegramUser.DoesNotExist:
                # Intentar como ID numérico
                try:
                    telegram_user_id = int(nombre_o_id)
                    queryset = queryset.filter(telegram_user_id=telegram_user_id)
                    titulo = f"🔐 Autorizaciones para ID {telegram_user_id}:\n"
                except ValueError:
                    return f"❌ Usuario '{nombre_o_id}' no encontrado"
        else:
            titulo = "🔐 Todas las autorizaciones:\n"
        
        # Filtrar solo activas si se solicita
        if solo_activas:
            queryset = queryset.filter(usada=False, expirada=False)
            titulo += "(Solo activas)\n"
        
        autorizaciones = queryset.order_by('-created_at')[:20]  # Máximo 20
        
        if not autorizaciones.exists():
            return "No hay autorizaciones encontradas."
        
        resultado = [titulo]
        for auth in autorizaciones:
            # Determinar estado
            if auth.usada:
                estado = "✅ Usada"
                fecha_estado = auth.usada_en.strftime("%d/%m %H:%M") if auth.usada_en else "?"
            elif auth.expirada or auth.expira_en < timezone.now():
                estado = "⏰ Expirada"
                fecha_estado = auth.expira_en.strftime("%d/%m %H:%M")
            else:
                estado = "🔓 Activa"
                fecha_estado = auth.expira_en.strftime("%d/%m %H:%M")
            
            # Mostrar información
            uid_corto = auth.uid_dispositivo[:12] + "..." if len(auth.uid_dispositivo) > 15 else auth.uid_dispositivo
            resultado.append(
                f"{estado} | {auth.accion} | {uid_corto}\n"
                f"   👤 User: {auth.telegram_user_id} | ⏰ {fecha_estado}"
            )
        
        return "\n".join(resultado)
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def limpiar_autorizaciones_expiradas() -> str:
    """
    Limpia (marca como expiradas) todas las autorizaciones que han superado su tiempo límite.
    También elimina autorizaciones muy antiguas (más de 24 horas).
    
    Returns:
        str: Resultado de la limpieza
    """
    try:
        from django.utils import timezone
        from datetime import timedelta
        
        ahora = timezone.now()
        hace_24h = ahora - timedelta(hours=24)
        
        # Marcar como expiradas las que han pasado su tiempo
        expiradas = TelegramAutorizacion.objects.filter(
            expira_en__lt=ahora,
            expirada=False,
            usada=False
        ).update(expirada=True)
        
        # Eliminar autorizaciones muy antiguas (más de 24h)
        eliminadas = TelegramAutorizacion.objects.filter(
            created_at__lt=hace_24h
        ).count()
        
        TelegramAutorizacion.objects.filter(
            created_at__lt=hace_24h
        ).delete()
        
        return f"✅ Limpieza completada:\n• {expiradas} autorizaciones marcadas como expiradas\n• {eliminadas} autorizaciones antiguas eliminadas"
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def ver_logs_telegram(limit: int = 10) -> str:
    """
    Muestra los últimos logs de notificaciones de Telegram enviadas.
    Útil para verificar si las notificaciones se están enviando correctamente.
    
    Args:
        limit: Número de logs a mostrar (máximo 50)
        
    Returns:
        str: Últimos logs de notificaciones
    """
    try:
        limit = min(limit, 50)  # Limitar a 50
        logs = TelegramNotificationLog.objects.select_related('event_type').order_by('-created_at')[:limit]
        
        if not logs.exists():
            return "No hay logs de notificaciones."
        
        resultado = [f"📊 Últimos {len(logs)} logs de notificaciones:\n"]
        
        for log in logs:
            estado = "✅" if log.enviado else "❌"
            fecha = log.created_at.strftime("%d/%m/%Y %H:%M")
            resultado.append(
                f"{estado} {fecha} | {log.event_type.code} → User {log.telegram_user_id}"
            )
            if not log.enviado and log.error:
                resultado.append(f"   Error: {log.error[:100]}")
        
        return "\n".join(resultado)
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


# Lista de todas las herramientas para exportar
telegram_tools = [
    listar_usuarios_telegram,
    registrar_usuario_telegram,
    modificar_usuario_telegram,
    eliminar_usuario_telegram,
    listar_eventos_telegram,
    listar_zonas_disponibles,
    listar_suscripciones_telegram,
    suscribir_usuario_telegram,
    configurar_filtro_zonas,
    desuscribir_usuario_telegram,
    enviar_notificacion_telegram,
    enviar_notificacion_prueba,
    simular_evento_dispositivo,
    listar_autorizaciones_telegram,
    limpiar_autorizaciones_expiradas,
    ver_logs_telegram,
]
