# chatbot/views/saved_messages_views.py

from django.db.models import Q
from django.utils import timezone
from datetime import timedelta
from django.core.paginator import Paginator
import json

from tokenapi.http import JsonResponse, JsonError
from tokenapi.decorators import token_required

from chatbot.models.save_message import SavedMessage, Category


def format_saved_message(message):
    """
    Formatear un mensaje para la respuesta JSON
    """
    return {
        'id': message.id,
        'titulo': message.titulo,
        'texto_html_raw': message.texto_html_raw,
        'category': {
            'id': message.category.id,
            'name': message.category.name
        } if message.category else None,
        'created_at': message.created_at.isoformat()
    }


def format_category(category):
    """
    Formatear una categoría para la respuesta JSON
    """
    return {
        'id': category.id,
        'name': category.name,
        'created_at': category.created_at.isoformat(),
        'message_count': category.savedmessage_set.count()
    }


def validate_create_data(post_data, user):
    """
    Validar datos para crear un mensaje
    """
    errors = {}
    cleaned_data = {}
    
    # Campo requerido
    texto_html_raw = post_data.get('texto_html_raw', '').strip()
    if not texto_html_raw:
        errors['texto_html_raw'] = 'Este campo es requerido'
    else:
        cleaned_data['texto_html_raw'] = texto_html_raw
    
    # Campos opcionales
    titulo = post_data.get('titulo', '').strip()
    if titulo:
        cleaned_data['titulo'] = titulo
    
    # Categoría (debe pertenecer al usuario)
    category_id = post_data.get('category_id')
    if category_id:
        try:
            category = Category.objects.get(id=category_id, user=user)
            cleaned_data['category'] = category
        except Category.DoesNotExist:
            errors['category_id'] = 'Categoría no encontrada o no pertenece al usuario'
    
    cleaned_data['user'] = user
    
    return cleaned_data, errors


def validate_update_data(post_data, user):
    """
    Validar datos para actualizar un mensaje
    """
    errors = {}
    cleaned_data = {}
    
    # Todos los campos son opcionales en actualización
    titulo = post_data.get('titulo', '').strip()
    if titulo:
        cleaned_data['titulo'] = titulo
    
    texto_html_raw = post_data.get('texto_html_raw', '').strip()
    if texto_html_raw:
        cleaned_data['texto_html_raw'] = texto_html_raw
    
    # Categoría (debe pertenecer al usuario)
    category_id = post_data.get('category_id')
    if category_id:
        try:
            category = Category.objects.get(id=category_id, user=user)
            cleaned_data['category'] = category
        except Category.DoesNotExist:
            errors['category_id'] = 'Categoría no encontrada o no pertenece al usuario'
    
    return cleaned_data, errors


def get_queryset_for_user(user, request_params=None):
    """
    Filtrar mensajes guardados por usuario
    """
    if request_params is None:
        request_params = {}
        
    queryset = SavedMessage.objects.filter(user=user)
    
    # Filtrar por categoría
    category_id = request_params.get('category_id')
    if category_id:
        queryset = queryset.filter(category_id=category_id)
    
    # Búsqueda en texto
    search = request_params.get('search')
    if search:
        queryset = queryset.filter(
            Q(titulo__icontains=search) | 
            Q(texto_html_raw__icontains=search)
        )
    
    return queryset.order_by('-created_at')


def paginate_queryset(queryset, page_num=1, page_size=20):
    """
    Paginar un queryset
    """
    paginator = Paginator(queryset, page_size)
    try:
        page = paginator.page(page_num)
    except:
        page = paginator.page(1)
    
    return {
        'results': [format_saved_message(obj) for obj in page],
        'count': paginator.count,
        'num_pages': paginator.num_pages,
        'current_page': page.number,
        'has_next': page.has_next(),
        'has_previous': page.has_previous(),
    }


@token_required
def saved_messages_list_create(request):
    """
    POST /api/chatbot/saved-messages/ - Manejar todas las operaciones (list, create, delete)
    """
    if request.method != 'POST':
        return JsonError("Método no permitido", status=405)
    
    # Obtener la acción del FormData
    action = request.POST.get('action', 'list')
    
    if action == 'list':
        # Listar mensajes guardados del usuario
        try:
            # Obtener parámetros de consulta
            page_num = int(request.POST.get('page', 1))
            page_size = int(request.POST.get('page_size', 20))
            category_id = request.POST.get('categoryId') or request.POST.get('category_id')
            search = request.POST.get('search', '')
            
            params = {
                'category_id': category_id,
                'search': search
            }
            
            queryset = get_queryset_for_user(request.user, params)
            result = paginate_queryset(queryset, page_num, page_size)
            
            return JsonResponse({
                'success': True,
                'data': result['results'],
                'count': result['count'],
                'num_pages': result['num_pages'],
                'current_page': result['current_page'],
                'has_next': result['has_next'],
                'has_previous': result['has_previous']
            })
            
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f"Error al cargar mensajes: {str(e)}"
            })
    
    elif action == 'create':
        # Crear nuevo mensaje guardado
        try:
            # Validar y limpiar datos
            cleaned_data, errors = validate_create_data(request.POST, request.user)
            
            if errors:
                return JsonResponse({
                    'success': False,
                    'error': f"Errores de validación: {errors}"
                })
            
            # Crear el mensaje
            saved_message = SavedMessage.objects.create(**cleaned_data)
            
            return JsonResponse({
                'success': True,
                'data': format_saved_message(saved_message),
                'message': 'Mensaje guardado exitosamente'
            })
            
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f"Error al crear mensaje: {str(e)}"
            })
    
    elif action == 'delete':
        # Eliminar mensaje específico
        try:
            message_id = request.POST.get('id')
            if not message_id:
                return JsonResponse({
                    'success': False,
                    'error': 'ID del mensaje requerido'
                })
            
            message = SavedMessage.objects.get(id=message_id, user=request.user)
            message.delete()
            
            return JsonResponse({
                'success': True,
                'message': 'Mensaje eliminado exitosamente'
            })
            
        except SavedMessage.DoesNotExist:
            return JsonResponse({
                'success': False,
                'error': 'Mensaje no encontrado'
            })
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f"Error al eliminar mensaje: {str(e)}"
            })
    
    else:
        return JsonResponse({
            'success': False,
            'error': f"Acción no reconocida: {action}"
        })


@token_required
def saved_messages_detail(request, message_id):
    """
    GET /api/chatbot/saved-messages/{id}/ - Obtener mensaje específico
    PUT /api/chatbot/saved-messages/{id}/ - Actualizar mensaje completo
    DELETE /api/chatbot/saved-messages/{id}/ - Eliminar mensaje
    """
    try:
        message = SavedMessage.objects.get(id=message_id, user=request.user)
    except SavedMessage.DoesNotExist:
        return JsonError("Mensaje no encontrado", status=404)
    
    if request.method == 'GET':
        return JsonResponse(format_saved_message(message))
    
    elif request.method == 'PUT' or request.method == 'PATCH':
        # Validar y actualizar mensaje
        cleaned_data, errors = validate_update_data(request.POST, request.user)
        
        if errors:
            return JsonError(errors)
        
        # Actualizar campos
        for field, value in cleaned_data.items():
            setattr(message, field, value)
        
        message.save()
        return JsonResponse({
            'message': 'Mensaje actualizado exitosamente',
            'data': format_saved_message(message)
        })
    
    elif request.method == 'DELETE':
        # Eliminar físicamente
        message.delete()
        return JsonResponse({'message': 'Mensaje eliminado exitosamente'})
    
    else:
        return JsonError("Método no permitido", status=405)


# === VISTAS PARA CATEGORÍAS ===

@token_required
def categories_list_create(request):
    """
    POST /api/chatbot/categories/ - Manejar todas las operaciones (list, create, delete)
    """
    if request.method != 'POST':
        return JsonError("Método no permitido", status=405)
    
    # Obtener la acción del FormData
    action = request.POST.get('action', 'list')
    
    if action == 'list':
        # Listar categorías del usuario
        try:
            categories = Category.objects.filter(user=request.user)
            result = [format_category(cat) for cat in categories]
            
            return JsonResponse({
                'success': True,
                'data': result
            })
            
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f"Error al cargar categorías: {str(e)}"
            })
    
    elif action == 'create':
        # Crear nueva categoría
        try:
            name = request.POST.get('name', '').strip()
            
            if not name:
                return JsonResponse({
                    'success': False,
                    'error': 'El nombre de la categoría es requerido'
                })
            
            # Verificar que no existe ya para este usuario
            if Category.objects.filter(user=request.user, name=name).exists():
                return JsonResponse({
                    'success': False,
                    'error': 'Ya tienes una categoría con ese nombre'
                })
            
            category = Category.objects.create(
                name=name,
                user=request.user
            )
            
            return JsonResponse({
                'success': True,
                'data': format_category(category),
                'message': 'Categoría creada exitosamente'
            })
            
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f"Error al crear categoría: {str(e)}"
            })
    
    elif action == 'delete':
        # Eliminar categoría específica
        try:
            category_id = request.POST.get('id')
            if not category_id:
                return JsonResponse({
                    'success': False,
                    'error': 'ID de la categoría requerido'
                })
            
            category = Category.objects.get(id=category_id, user=request.user)
            
            # Verificar si tiene mensajes asociados
            message_count = category.savedmessage_set.count()
            if message_count > 0:
                return JsonResponse({
                    'success': False,
                    'error': f'No se puede eliminar. Esta categoría tiene {message_count} mensajes asociados.'
                })
            
            category.delete()
            
            return JsonResponse({
                'success': True,
                'message': 'Categoría eliminada exitosamente'
            })
            
        except Category.DoesNotExist:
            return JsonResponse({
                'success': False,
                'error': 'Categoría no encontrada'
            })
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f"Error al eliminar categoría: {str(e)}"
            })
    
    else:
        return JsonResponse({
            'success': False,
            'error': f"Acción no reconocida: {action}"
        })


@token_required
def categories_detail(request, category_id):
    """
    GET /api/chatbot/categories/{id}/ - Obtener categoría específica
    PUT /api/chatbot/categories/{id}/ - Actualizar categoría
    DELETE /api/chatbot/categories/{id}/ - Eliminar categoría
    """
    try:
        category = Category.objects.get(id=category_id, user=request.user)
    except Category.DoesNotExist:
        return JsonError("Categoría no encontrada", status=404)
    
    if request.method == 'GET':
        return JsonResponse(format_category(category))
    
    elif request.method == 'PUT' or request.method == 'PATCH':
        name = request.POST.get('name', '').strip()
        
        if not name:
            return JsonError('El nombre de la categoría es requerido')
        
        # Verificar que no existe ya para este usuario (excluyendo la actual)
        if Category.objects.filter(user=request.user, name=name).exclude(id=category_id).exists():
            return JsonError('Ya tienes una categoría con ese nombre')
        
        try:
            category.name = name
            category.save()
            
            return JsonResponse({
                'message': 'Categoría actualizada exitosamente',
                'data': format_category(category)
            })
        except Exception as e:
            return JsonError(f"Error al actualizar categoría: {str(e)}")
    
    elif request.method == 'DELETE':
        # Verificar si tiene mensajes asociados
        message_count = category.savedmessage_set.count()
        if message_count > 0:
            return JsonError(f'No se puede eliminar. Esta categoría tiene {message_count} mensajes asociados.')
        
        category.delete()
        return JsonResponse({'message': 'Categoría eliminada exitosamente'})
    
    else:
        return JsonError("Método no permitido", status=405)


# === VISTAS PARA MENSAJES ===


@token_required
def saved_messages_by_category(request, category_id):
    """
    GET /api/chatbot/saved-messages/by_category/{category_id}/ - Mensajes por categoría específica
    """
    if request.method != 'GET':
        return JsonError("Método no permitido", status=405)
    
    # Verificar que la categoría pertenece al usuario
    try:
        category = Category.objects.get(id=category_id, user=request.user)
    except Category.DoesNotExist:
        return JsonError("Categoría no encontrada", status=404)
    
    params = dict(request.GET.items())
    params['category_id'] = category_id
    page_num = int(params.get('page', 1))
    page_size = int(params.get('page_size', 20))
    
    queryset = get_queryset_for_user(request.user, params)
    result = paginate_queryset(queryset, page_num, page_size)
    
    return JsonResponse(result)


@token_required
def saved_messages_bulk_delete(request):
    """
    POST /api/chatbot/saved-messages/bulk_delete/ - Eliminar múltiples mensajes por IDs
    """
    if request.method != 'POST':
        return JsonError("Método no permitido", status=405)
    
    try:
        # Obtener IDs del POST
        ids_str = request.POST.get('ids', '[]')
        try:
            message_ids = json.loads(ids_str)
        except:
            message_ids = []
        
        if not message_ids or not isinstance(message_ids, list):
            return JsonError('Se requiere una lista de IDs')
        
        # Verificar que los mensajes pertenecen al usuario
        queryset = SavedMessage.objects.filter(id__in=message_ids, user=request.user)
        
        deleted_count = queryset.count()
        if deleted_count == 0:
            return JsonError('No se encontraron mensajes para eliminar', status=404)
        
        # Eliminar físicamente
        queryset.delete()
        
        return JsonResponse({
            'message': f'{deleted_count} mensajes eliminados exitosamente',
            'deleted_count': deleted_count
        })
        
    except Exception as e:
        return JsonError(f"Error al eliminar mensajes: {str(e)}")


@token_required
def saved_messages_stats(request):
    """
    GET /api/chatbot/saved-messages/stats/ - Obtener estadísticas básicas del usuario
    """
    if request.method != 'GET':
        return JsonError("Método no permitido", status=405)
    
    user_messages = SavedMessage.objects.filter(user=request.user)
    user_categories = Category.objects.filter(user=request.user)
    
    stats = {
        'total_messages': user_messages.count(),
        'total_categories': user_categories.count(),
        'recent_messages': user_messages.filter(
            created_at__gte=timezone.now() - timedelta(days=7)
        ).count(),
        'by_category': []
    }
    
    # Conteo por categoría
    for category in user_categories:
        count = user_messages.filter(category=category).count()
        stats['by_category'].append({
            'id': category.id,
            'name': category.name,
            'count': count
        })
    
    # Mensajes sin categoría
    uncategorized_count = user_messages.filter(category__isnull=True).count()
    if uncategorized_count > 0:
        stats['by_category'].append({
            'id': None,
            'name': 'Sin categoría',
            'count': uncategorized_count
        })
    
    return JsonResponse(stats)
