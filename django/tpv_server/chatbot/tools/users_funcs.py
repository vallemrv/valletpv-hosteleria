from typing import List, Dict, Union
from django.contrib.auth.models import User
from django.core.exceptions import ValidationError
from django.contrib.auth.password_validation import validate_password
from langchain_core.tools import tool
from chatbot.utilidades.ws_sender import send_tool_message
from gestion.tools.config_logs import log_debug_chatbot as logger
from django.db.models import Q
from django.core.mail import send_mail
from django.conf import settings
from chatbot.decorators.db_connection_manager import db_connection_handler

@tool
@db_connection_handler
def crear_usuarios(usuarios: List[Dict[str, Union[str, bool]]]) -> List[Dict[str, Union[int, str]]]:
    """
    Crea múltiples usuarios de Django en un solo lote.

    Args:
        usuarios (List[Dict[str, Union[str, bool]]]): Lista de diccionarios, cada uno con los datos de un usuario:
            - username (str): Nombre de usuario (obligatorio).
            - email (str, optional): Correo electrónico.
            - password (str): Contraseña (obligatorio).
            - first_name (str, optional): Nombre.
            - last_name (str, optional): Apellidos.
            - is_superuser (bool, optional): Si es superusuario. Por defecto False.
            - is_staff (bool, optional): Si es staff. Por defecto False.
            - is_active (bool, optional): Si está activo. Por defecto True.

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los resultados de cada operación
        (id, username para usuarios creados; error para fallos).
    """
    results = []
    
    for user_data in usuarios:
        username = user_data.get("username")
        password = user_data.get("password")
        
        if not username or not password:
            results.append({"error": f"Datos incompletos para usuario: username={username}, password={'***' if password else None}"})
            logger.error(f"Datos incompletos para usuario: username={username}")
            continue
        
        try:
            # Verificar si el usuario ya existe
            if User.objects.filter(username=username).exists():
                error_msg = f"Usuario '{username}' ya existe"
                logger.error(error_msg)
                results.append({"error": error_msg})
                continue
            send_tool_message(f"Creando usuario '{username}'...")
            logger.debug(f"Creando usuario '{username}'...")
            
            # Validar la contraseña
            validate_password(password)
            
            # Crear el usuario
            user = User(
                username=username,
                email=user_data.get("email", ""),
                first_name=user_data.get("first_name", ""),
                last_name=user_data.get("last_name", ""),
                is_superuser=user_data.get("is_superuser", False),
                is_staff=user_data.get("is_staff", False),
                is_active=user_data.get("is_active", True)
            )
            user.set_password(password)
            user.save()
            
            results.append({"id": user.id, "username": user.username})
            
        except ValidationError as e:
            error_msg = f"Error de validación para usuario '{username}': {', '.join(e.messages)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
        except Exception as e:
            error_msg = f"Error creando usuario '{username}': {str(e)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
    
    return results


@tool
@db_connection_handler
def listar_usuarios(ids: List[int] = None, solo_superusuarios: bool = False, solo_staff: bool = False, solo_activos: bool = None) -> List[Dict[str, Union[int, str, bool]]]:
    """
    Lista usuarios de Django con filtros opcionales.

    Args:
        ids (List[int], optional): Lista de IDs de usuarios a mostrar. Si es None, muestra según otros filtros.
        solo_superusuarios (bool, optional): Si True, solo muestra superusuarios. Por defecto False.
        solo_staff (bool, optional): Si True, solo muestra usuarios staff. Por defecto False.
        solo_activos (bool, optional): Si True, solo activos; si False, solo inactivos; si None, todos.

    Returns:
        List[Dict[str, Union[int, str, bool]]]: Lista de diccionarios con los datos de los usuarios
        (id, username, email, first_name, last_name, is_superuser, is_staff, is_active, date_joined, last_login).
    """
    try:
        queryset = User.objects.all()
        
        if ids is not None:
            send_tool_message(f"Listando usuarios con IDs: {ids}...")
            logger.debug(f"Listando usuarios con IDs: {ids}...")
            queryset = queryset.filter(id__in=ids)
        else:
            filters = []
            if solo_superusuarios:
                queryset = queryset.filter(is_superuser=True)
                filters.append("superusuarios")
            if solo_staff:
                queryset = queryset.filter(is_staff=True)
                filters.append("staff")
            if solo_activos is not None:
                queryset = queryset.filter(is_active=solo_activos)
                filters.append("activos" if solo_activos else "inactivos")
            
            filter_text = ", ".join(filters) if filters else "todos los usuarios"
            send_tool_message(f"Listando {filter_text}...")
            logger.debug(f"Listando {filter_text}...")
        
        users = []
        for user in queryset:
            users.append({
                "id": user.id,
                "username": user.username,
                "email": user.email,
                "first_name": user.first_name,
                "last_name": user.last_name,
                "is_superuser": user.is_superuser,
                "is_staff": user.is_staff,
                "is_active": user.is_active,
                "date_joined": user.date_joined.isoformat() if user.date_joined else None,
                "last_login": user.last_login.isoformat() if user.last_login else None
            })
        
        return users
        
    except Exception as e:
        error_msg = f"Error listando usuarios: {str(e)}"
        logger.error(error_msg)
        return [{"error": error_msg}]


@tool
@db_connection_handler
def modificar_usuarios(modificaciones: List[Dict[str, Union[int, str, bool]]]) -> List[Dict[str, Union[int, str]]]:
    """
    Modifica múltiples usuarios de Django existentes en un solo lote.

    Args:
        modificaciones (List[Dict[str, Union[int, str, bool]]]): Lista de diccionarios, cada uno con:
            - user_id (int): ID del usuario a modificar (obligatorio).
            - username (str, optional): Nuevo nombre de usuario.
            - email (str, optional): Nuevo correo electrónico.
            - password (str, optional): Nueva contraseña.
            - first_name (str, optional): Nuevo nombre.
            - last_name (str, optional): Nuevos apellidos.
            - is_superuser (bool, optional): Cambiar estado de superusuario.
            - is_staff (bool, optional): Cambiar estado de staff.
            - is_active (bool, optional): Cambiar estado activo.

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los resultados de cada operación
        (id, username para usuarios modificados; error para fallos).
    """
    results = []
    
    for mod_data in modificaciones:
        user_id = mod_data.get("user_id")
        
        if not user_id:
            results.append({"error": "ID de usuario no proporcionado"})
            logger.error("ID de usuario no proporcionado")
            continue
        
        try:
            user = User.objects.get(id=user_id)
            send_tool_message(f"Modificando usuario '{user.username}'...")
            logger.debug(f"Modificando usuario '{user.username}'...")
            
            # Verificar si se intenta cambiar username y ya existe
            new_username = mod_data.get("username")
            if new_username and new_username != user.username:
                if User.objects.filter(username=new_username).exists():
                    error_msg = f"El nombre de usuario '{new_username}' ya existe"
                    logger.error(error_msg)
                    results.append({"error": error_msg})
                    continue
                user.username = new_username
            
            # Actualizar otros campos
            if "email" in mod_data:
                user.email = mod_data["email"] or ""
            if "first_name" in mod_data:
                user.first_name = mod_data["first_name"] or ""
            if "last_name" in mod_data:
                user.last_name = mod_data["last_name"] or ""
            if "is_superuser" in mod_data:
                user.is_superuser = mod_data["is_superuser"]
            if "is_staff" in mod_data:
                user.is_staff = mod_data["is_staff"]
            if "is_active" in mod_data:
                user.is_active = mod_data["is_active"]
            
            # Cambiar contraseña si se proporciona
            new_password = mod_data.get("password")
            if new_password:
                validate_password(new_password)
                user.set_password(new_password)
                
            user.save()
            results.append({"id": user.id, "username": user.username})
            
        except User.DoesNotExist:
            error_msg = f"Usuario con ID {user_id} no encontrado"
            logger.error(error_msg)
            results.append({"error": error_msg})
        except ValidationError as e:
            error_msg = f"Error de validación para usuario con ID {user_id}: {', '.join(e.messages)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
        except Exception as e:
            error_msg = f"Error modificando usuario con ID {user_id}: {str(e)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
    
    return results


@tool
@db_connection_handler
def eliminar_usuarios(user_ids: List[int]) -> List[Dict[str, str]]:
    """
    Elimina múltiples usuarios de Django por sus IDs en un solo lote.

    Args:
        user_ids (List[int]): Lista de IDs de los usuarios a eliminar.

    Returns:
        List[Dict[str, str]]: Lista de diccionarios con los resultados de cada operación
        (mensaje de éxito o error).
    """
    results = []
    
    for user_id in user_ids:
        try:
            user = User.objects.get(id=user_id)
            username = user.username
            
            send_tool_message(f"Eliminando usuario '{username}'...")
            logger.debug(f"Eliminando usuario '{username}'...")
            
            user.delete()
            results.append({"success": f"Usuario '{username}' eliminado correctamente"})
            
        except User.DoesNotExist:
            error_msg = f"Usuario con ID {user_id} no encontrado"
            logger.error(error_msg)
            results.append({"error": error_msg})
        except Exception as e:
            error_msg = f"Error eliminando usuario con ID {user_id}: {str(e)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
    
    return results


@tool
@db_connection_handler
def promover_a_superusuario(user_ids: List[int]) -> List[Dict[str, Union[int, str]]]:
    """
    Promueve usuarios a superusuarios (is_superuser=True, is_staff=True).

    Args:
        user_ids (List[int]): Lista de IDs de usuarios a promover.

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los resultados de cada operación
        (id, username para usuarios promovidos; error para fallos).
    """
    results = []
    
    for user_id in user_ids:
        try:
            user = User.objects.get(id=user_id)
            send_tool_message(f"Promoviendo usuario '{user.username}' a superusuario...")
            logger.debug(f"Promoviendo usuario '{user.username}' a superusuario...")
            
            user.is_superuser = True
            user.is_staff = True
            user.save()
            
            results.append({"id": user.id, "username": user.username, "success": "Promovido a superusuario"})
            
        except User.DoesNotExist:
            error_msg = f"Usuario con ID {user_id} no encontrado"
            logger.error(error_msg)
            results.append({"error": error_msg})
        except Exception as e:
            error_msg = f"Error promoviendo usuario con ID {user_id}: {str(e)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
    
    return results


@tool
@db_connection_handler
def quitar_permisos_superusuario(user_ids: List[int]) -> List[Dict[str, Union[int, str]]]:
    """
    Quita permisos de superusuario a usuarios (is_superuser=False, is_staff=False).

    Args:
        user_ids (List[int]): Lista de IDs de usuarios a los que quitar permisos.

    Returns:
        List[Dict[str, Union[int, str]]]: Lista de diccionarios con los resultados de cada operación
        (id, username para usuarios modificados; error para fallos).
    """
    results = []
    
    for user_id in user_ids:
        try:
            user = User.objects.get(id=user_id)
            send_tool_message(f"Quitando permisos de superusuario a '{user.username}'...")
            logger.debug(f"Quitando permisos de superusuario a '{user.username}'...")
            
            user.is_superuser = False
            user.is_staff = False
            user.save()
            
            results.append({"id": user.id, "username": user.username, "success": "Permisos de superusuario quitados"})
            
        except User.DoesNotExist:
            error_msg = f"Usuario con ID {user_id} no encontrado"
            logger.error(error_msg)
            results.append({"error": error_msg})
        except Exception as e:
            error_msg = f"Error quitando permisos a usuario con ID {user_id}: {str(e)}"
            logger.error(error_msg)
            results.append({"error": error_msg})
    
    return results


@tool
@db_connection_handler
def buscar_usuarios_por_nombre(filtro: str) -> List[Dict[str, Union[int, str, bool]]]:
    """
    Busca usuarios por nombre de usuario, nombre, apellidos o email.

    Args:
        filtro (str): Texto a buscar en username, first_name, last_name o email.

    Returns:
        List[Dict[str, Union[int, str, bool]]]: Lista de diccionarios con los datos de los usuarios encontrados.
    """
    try:
        send_tool_message(f"Buscando usuarios con filtro '{filtro}'...")
        logger.debug(f"Buscando usuarios con filtro '{filtro}'...")
        
        users = User.objects.filter(
            Q(username__icontains=filtro) |
            Q(first_name__icontains=filtro) |
            Q(last_name__icontains=filtro) |
            Q(email__icontains=filtro)
        ).distinct()
        
        results = []
        for user in users:
            results.append({
                "id": user.id,
                "username": user.username,
                "email": user.email,
                "first_name": user.first_name,
                "last_name": user.last_name,
                "is_superuser": user.is_superuser,
                "is_staff": user.is_staff,
                "is_active": user.is_active,
                "date_joined": user.date_joined.isoformat() if user.date_joined else None,
                "last_login": user.last_login.isoformat() if user.last_login else None
            })
        
        return results
        
    except Exception as e:
        error_msg = f"Error buscando usuarios: {str(e)}"
        logger.error(error_msg)
        return [{"error": error_msg}]

# 1. Crea una función interna con la lógica, SIN el decorador @tool
#    La renombramos con un guion bajo por convención.
@db_connection_handler
def _enviar_email_destinatarios_logic(destinatarios: List[str], asunto: str, mensaje: str, es_html: bool = False) -> List[Dict[str, Union[str, bool]]]:
    """
    Lógica interna para enviar correos electrónicos a una lista de direcciones.
    Esta función NO es una herramienta para el agente, es un helper interno.
    """
    results = []
    
    if not asunto or not mensaje:
        error_msg = "Asunto y mensaje son obligatorios"
        logger.error(error_msg)
        return [{"error": error_msg}]
    
    if not destinatarios:
        error_msg = "La lista de destinatarios no puede estar vacía"
        logger.error(error_msg)
        return [{"error": error_msg}]
    
    mail_address = getattr(settings, 'MAIL', "noreply@valletpv.es")
    brand_title = getattr(settings, 'BRAND_TITLE', "ValletPV")
    from_email = f"{brand_title} <{mail_address}>"
    
    for email_destino in destinatarios:
        try:
            if "@" not in email_destino or "." not in email_destino:
                error_msg = f"Dirección de correo inválida: {email_destino}"
                logger.warning(error_msg)
                results.append({"email": email_destino, "error": error_msg})
                continue
            
            send_tool_message(f"Enviando correo a '{email_destino}'...")
            logger.debug(f"Enviando correo a '{email_destino}'...")
            
            if es_html:
                from django.core.mail import EmailMessage
                email = EmailMessage(subject=asunto, body=mensaje, from_email=from_email, to=[email_destino])
                email.content_subtype = "html"
                email.send()
            else:
                send_mail(subject=asunto, message=mensaje, from_email=from_email, recipient_list=[email_destino], fail_silently=False)
            
            results.append({"email": email_destino, "success": "Correo enviado correctamente"})
            
        except Exception as e:
            error_msg = f"Error enviando correo a {email_destino}: {str(e)}"
            logger.error(error_msg)
            results.append({"email": email_destino, "error": error_msg})
    
    return results


@tool
@db_connection_handler
def enviar_email_usuarios(user_ids: List[int], asunto: str, mensaje: str, es_html: bool = False) -> List[Dict[str, Union[int, str]]]:
    """
    Envía correos electrónicos a múltiples usuarios especificados por sus IDs.
    """
    # ... (toda la lógica para obtener los emails de los usuarios es la misma) ...
    results = []
    
    if not asunto or not mensaje:
        error_msg = "Asunto y mensaje son obligatorios"
        logger.error(error_msg)
        return [{"error": error_msg}]
    
    emails_validos = []
    usuarios_info = {}
    
    for user_id in user_ids:
        try:
            user = User.objects.get(id=user_id)
            if not user.email:
                error_msg = f"Usuario '{user.username}' no tiene dirección de correo configurada"
                logger.warning(error_msg)
                results.append({"id": user.id, "username": user.username, "email": "", "error": error_msg})
                continue
            emails_validos.append(user.email)
            usuarios_info[user.email] = {"id": user.id, "username": user.username, "email": user.email}
        except User.DoesNotExist:
            error_msg = f"Usuario con ID {user_id} no encontrado"
            logger.error(error_msg)
            results.append({"error": error_msg})
    
    if emails_validos:
        # 2. Llama a la función de lógica interna, no a la herramienta
        email_results = _enviar_email_destinatarios_logic(emails_validos, asunto, mensaje, es_html)
        
        # ... (el resto del mapeo de resultados es igual) ...
        for email_result in email_results:
            email = email_result.get("email", "")
            if email in usuarios_info:
                user_info = usuarios_info[email]
                if "success" in email_result:
                    results.append({"id": user_info["id"], "username": user_info["username"], "email": user_info["email"], "success": email_result["success"]})
                elif "error" in email_result:
                    results.append({"id": user_info["id"], "username": user_info["username"], "email": user_info["email"], "error": email_result["error"]})
    
    return results


@tool
def enviar_email_destinatarios(destinatarios: List[str], asunto: str, mensaje: str, es_html: bool = False) -> List[Dict[str, Union[str, bool]]]:
    """
    Envía correos electrónicos a una lista de direcciones de correo especificadas directamente.
    """
    # 3. Esta herramienta ahora simplemente llama a la función de lógica interna
    return _enviar_email_destinatarios_logic(destinatarios, asunto, mensaje, es_html)

# Lista de herramientas disponibles para usuarios
tools: List = [
    crear_usuarios,
    listar_usuarios,
    modificar_usuarios,
    eliminar_usuarios,
    promover_a_superusuario,
    quitar_permisos_superusuario,
    buscar_usuarios_por_nombre,
    enviar_email_usuarios,
    enviar_email_destinatarios,
]
