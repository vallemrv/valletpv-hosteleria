"""
Tools de LangChain para gestionar notificaciones push de Telegram
Permite al chatbot gestionar eventos, suscripciones y enviar notificaciones
"""

from langchain_core.tools import tool
from typing import Optional, List
from django.utils import timezone
from push_telegram.models import (
    TelegramEventType, 
    TelegramSubscription, 
    TelegramNotificationLog,
    TelegramAutorizacion
)
from push_telegram.push_sender import enviar_push_telegram


@tool
def listar_eventos_telegram() -> str:
    """
    Lista todos los tipos de eventos de Telegram disponibles y su estado.
    Útil para ver qué eventos existen y cuáles están activos.
    
    Returns:
        str: Lista formateada de eventos con su estado
    """
    eventos = TelegramEventType.objects.all()
    
    if not eventos.exists():
        return "No hay eventos de Telegram configurados."
    
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
def crear_evento_telegram(code: str, nombre: str, descripcion: str = "") -> str:
    """
    Crea un nuevo tipo de evento de Telegram para notificaciones push.
    
    Args:
        code: Código único del evento (ej: 'pedido_completado', 'error_critico')
        nombre: Nombre descriptivo del evento
        descripcion: Descripción opcional del evento
        
    Returns:
        str: Confirmación de creación o mensaje de error
    """
    try:
        # Verificar si ya existe
        if TelegramEventType.objects.filter(code=code).exists():
            return f"❌ Ya existe un evento con el código '{code}'"
        
        evento = TelegramEventType.objects.create(
            code=code,
            nombre=nombre,
            descripcion=descripcion,
            activo=True
        )
        
        return f"✅ Evento creado exitosamente:\n• Código: {evento.code}\n• Nombre: {evento.nombre}"
        
    except Exception as e:
        return f"❌ Error creando evento: {str(e)}"


@tool
def activar_desactivar_evento_telegram(code: str, activar: bool = True) -> str:
    """
    Activa o desactiva un tipo de evento de Telegram.
    Los eventos desactivados no envían notificaciones.
    
    Args:
        code: Código del evento
        activar: True para activar, False para desactivar
        
    Returns:
        str: Confirmación de cambio
    """
    try:
        evento = TelegramEventType.objects.get(code=code)
        evento.activo = activar
        evento.save(update_fields=['activo'])
        
        accion = "activado" if activar else "desactivado"
        return f"✅ Evento '{evento.nombre}' {accion} correctamente"
        
    except TelegramEventType.DoesNotExist:
        return f"❌ No existe un evento con código '{code}'"
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def listar_suscripciones_telegram(telegram_user_id: Optional[int] = None) -> str:
    """
    Lista las suscripciones de Telegram.
    Puede filtrar por usuario específico o mostrar todas.
    
    Args:
        telegram_user_id: ID del usuario de Telegram (opcional)
        
    Returns:
        str: Lista de suscripciones
    """
    try:
        if telegram_user_id:
            suscripciones = TelegramSubscription.objects.filter(
                telegram_user_id=telegram_user_id
            ).select_related('event_type')
            titulo = f"📱 Suscripciones del usuario {telegram_user_id}:\n"
        else:
            suscripciones = TelegramSubscription.objects.all().select_related('event_type')
            titulo = "📱 Todas las suscripciones:\n"
        
        if not suscripciones.exists():
            return "No hay suscripciones configuradas."
        
        resultado = [titulo]
        for sub in suscripciones:
            estado = "✅" if sub.activo else "❌"
            resultado.append(
                f"{estado} {sub.nombre_usuario or sub.telegram_user_id} → {sub.event_type.nombre} ({sub.event_type.code})"
            )
        
        return "\n".join(resultado)
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def suscribir_usuario_telegram(
    telegram_user_id: int,
    event_code: str,
    nombre_usuario: str = ""
) -> str:
    """
    Suscribe un usuario de Telegram a un tipo de evento.
    El usuario recibirá notificaciones cuando ocurra ese evento.
    
    Args:
        telegram_user_id: ID del usuario de Telegram (número)
        event_code: Código del evento al que suscribir
        nombre_usuario: Nombre descriptivo del usuario (opcional)
        
    Returns:
        str: Confirmación de suscripción
    """
    try:
        # Buscar el evento
        try:
            evento = TelegramEventType.objects.get(code=event_code)
        except TelegramEventType.DoesNotExist:
            return f"❌ No existe el evento '{event_code}'. Usa listar_eventos_telegram() para ver eventos disponibles."
        
        # Crear o actualizar suscripción
        suscripcion, created = TelegramSubscription.objects.update_or_create(
            telegram_user_id=telegram_user_id,
            event_type=evento,
            defaults={
                'nombre_usuario': nombre_usuario,
                'activo': True
            }
        )
        
        if created:
            return f"✅ Usuario {telegram_user_id} suscrito a '{evento.nombre}'"
        else:
            return f"✅ Suscripción actualizada para usuario {telegram_user_id} → '{evento.nombre}'"
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def desuscribir_usuario_telegram(telegram_user_id: int, event_code: str) -> str:
    """
    Desuscribe un usuario de Telegram de un tipo de evento.
    El usuario dejará de recibir notificaciones de ese evento.
    
    Args:
        telegram_user_id: ID del usuario de Telegram
        event_code: Código del evento del que desuscribir
        
    Returns:
        str: Confirmación de desuscripción
    """
    try:
        evento = TelegramEventType.objects.get(code=event_code)
        suscripcion = TelegramSubscription.objects.get(
            telegram_user_id=telegram_user_id,
            event_type=evento
        )
        
        suscripcion.activo = False
        suscripcion.save(update_fields=['activo'])
        
        return f"✅ Usuario {telegram_user_id} desuscrito de '{evento.nombre}'"
        
    except TelegramEventType.DoesNotExist:
        return f"❌ No existe el evento '{event_code}'"
    except TelegramSubscription.DoesNotExist:
        return f"❌ El usuario {telegram_user_id} no estaba suscrito a '{event_code}'"
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


@tool
def ver_autorizaciones_pendientes() -> str:
    """
    Muestra las autorizaciones de Telegram pendientes (no usadas y no expiradas).
    Útil para ver qué acciones están esperando confirmación.
    
    Returns:
        str: Lista de autorizaciones pendientes
    """
    try:
        ahora = timezone.now()
        autorizaciones = TelegramAutorizacion.objects.filter(
            usada=False,
            expirada=False,
            expira_en__gt=ahora
        ).order_by('-created_at')[:20]
        
        if not autorizaciones.exists():
            return "✅ No hay autorizaciones pendientes"
        
        resultado = [f"🔓 Autorizaciones pendientes ({len(autorizaciones)}):\n"]
        
        for auth in autorizaciones:
            tiempo_restante = (auth.expira_en - ahora).seconds // 60
            resultado.append(
                f"• {auth.accion} - Dispositivo: {auth.uid_dispositivo[:16]}...\n"
                f"  Usuario: {auth.telegram_user_id} | Empresa: {auth.empresa}\n"
                f"  Expira en: {tiempo_restante} minutos\n"
            )
        
        return "\n".join(resultado)
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


@tool
def limpiar_autorizaciones_expiradas() -> str:
    """
    Marca como expiradas todas las autorizaciones que han pasado su fecha de expiración.
    Esto es útil para mantener limpia la base de datos.
    
    Returns:
        str: Número de autorizaciones marcadas como expiradas
    """
    try:
        ahora = timezone.now()
        autorizaciones = TelegramAutorizacion.objects.filter(
            usada=False,
            expirada=False,
            expira_en__lte=ahora
        )
        
        count = autorizaciones.count()
        autorizaciones.update(expirada=True)
        
        return f"✅ Se marcaron {count} autorización(es) como expiradas"
        
    except Exception as e:
        return f"❌ Error: {str(e)}"


# Lista de todas las herramientas para exportar
telegram_tools = [
    listar_eventos_telegram,
    crear_evento_telegram,
    activar_desactivar_evento_telegram,
    listar_suscripciones_telegram,
    suscribir_usuario_telegram,
    desuscribir_usuario_telegram,
    enviar_notificacion_telegram,
    ver_logs_telegram,
    ver_autorizaciones_pendientes,
    limpiar_autorizaciones_expiradas,
]
