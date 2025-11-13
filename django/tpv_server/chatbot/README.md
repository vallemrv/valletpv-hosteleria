# Módulo Chatbot - ValleTPV

## Descripción

El módulo **chatbot** gestiona toda la funcionalidad relacionada con el sistema de chat de ValleTPV, incluyendo el guardado de mensajes, sesiones de chat y plantillas de mensajes.

## Estructura del Módulo

```
chatbot/
├── __init__.py
├── admin.py              # Configuración del admin de Django
├── apps.py               # Configuración de la aplicación
├── models.py             # Modelos de datos
├── urls.py               # URLs y rutas de la API
├── migrations/           # Migraciones de base de datos
├── views/
│   ├── __init__.py
│   └── saved_messages_views.py  # Vistas API para mensajes guardados
└── serializers/
    ├── __init__.py
    └── saved_messages_serializers.py  # Serializers para la API REST
```

## Modelos

### SavedMessage
Modelo principal para almacenar mensajes guardados del chat.

**Campos principales:**
- `text`: Contenido del mensaje
- `message_type`: Tipo de mensaje (message, status, error, etc.)
- `category`: Categoría del mensaje (general, errors, orders, etc.)
- `sender`: Remitente (user, bot, status)
- `tags`: Lista de etiquetas para organización
- `is_important`: Marcador de importancia
- `user`: Usuario que guardó el mensaje
- `company_id`: ID de la empresa
- `session`: Sesión de chat asociada (opcional)

**Métodos principales:**
- `add_tag(tag)`: Añadir una etiqueta
- `remove_tag(tag)`: Eliminar una etiqueta
- `get_formatted_timestamp()`: Obtener timestamp formateado

### ChatSession
Modelo para gestionar sesiones de chat.

**Campos principales:**
- `session_id`: Identificador único de la sesión
- `company_id`: ID de la empresa
- `user`: Usuario de la sesión
- `start_time`: Hora de inicio
- `end_time`: Hora de fin (opcional)
- `is_active`: Estado de la sesión
- `metadata`: Metadatos adicionales

### MessageTemplate
Modelo para plantillas de mensajes predefinidas.

**Campos principales:**
- `name`: Nombre de la plantilla
- `template_type`: Tipo de plantilla
- `content`: Contenido de la plantilla
- `variables`: Variables disponibles
- `usage_count`: Contador de uso
- `is_default`: Marcador de plantilla por defecto

## API Endpoints

Todas las URLs están bajo el prefijo `/chatbot/api/`

### Mensajes Guardados (`/saved-messages/`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/saved-messages/` | Listar mensajes guardados |
| POST | `/saved-messages/` | Crear nuevo mensaje |
| GET | `/saved-messages/{id}/` | Obtener mensaje específico |
| PATCH | `/saved-messages/{id}/` | Actualizar mensaje |
| DELETE | `/saved-messages/{id}/` | Eliminar mensaje (soft delete) |
| GET | `/saved-messages/categories/` | Categorías con conteos |
| GET | `/saved-messages/stats/` | Estadísticas generales |
| GET | `/saved-messages/by_category/{category}/` | Filtrar por categoría |
| GET | `/saved-messages/by_type/{type}/` | Filtrar por tipo |
| POST | `/saved-messages/bulk_delete/` | Eliminar múltiples mensajes |
| POST | `/saved-messages/{id}/add_tag/` | Añadir etiqueta |
| POST | `/saved-messages/{id}/remove_tag/` | Quitar etiqueta |

### Plantillas (`/templates/`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/templates/` | Listar plantillas |
| POST | `/templates/` | Crear nueva plantilla |
| GET | `/templates/{id}/` | Obtener plantilla específica |
| PATCH | `/templates/{id}/` | Actualizar plantilla |
| DELETE | `/templates/{id}/` | Eliminar plantilla |
| POST | `/templates/{id}/use_template/` | Marcar como usada |

## Filtros y Búsqueda

### Parámetros de Query Disponibles

**Para `/saved-messages/`:**
- `company_id`: Filtrar por empresa
- `category`: Filtrar por categoría
- `type`: Filtrar por tipo de mensaje
- `tags`: Filtrar por etiquetas (separadas por coma)
- `search`: Búsqueda en texto y tags

**Para `/templates/`:**
- `company_id`: Filtrar por empresa
- `type`: Filtrar por tipo de plantilla

## Categorías de Mensajes

- **general**: Mensajes generales
- **errors**: Mensajes de error
- **orders**: Confirmaciones de pedidos
- **status**: Mensajes de estado
- **important**: Mensajes importantes
- **responses**: Respuestas frecuentes

## Tipos de Mensaje

- **message**: Mensaje normal
- **status**: Mensaje de estado
- **error**: Mensaje de error
- **pedido_confirmation**: Confirmación de pedido
- **welcome**: Mensaje de bienvenida

## Integración con Frontend

El frontend utiliza el store `savedMessagesStore.ts` que se conecta con estas APIs.

### Ejemplo de uso desde el frontend:

```typescript
// Guardar un mensaje
await savedMessagesStore.saveMessage({
  text: "Mensaje importante",
  type: "message",
  sender: "user",
  category: "important",
  tags: ["urgente", "cliente"]
});

// Cargar mensajes guardados
await savedMessagesStore.loadSavedMessages();

// Eliminar mensaje
await savedMessagesStore.deleteMessage(messageId);
```

## Configuración

### En `settings.py`

Asegurarse de que `'chatbot'` esté en `INSTALLED_APPS`:

```python
INSTALLED_APPS = [
    # ... otras apps
    'chatbot',
    # ... resto de apps
]
```

### En `urls.py` principal

Incluir las URLs del chatbot:

```python
urlpatterns = [
    # ... otras URLs
    path('chatbot/', include('chatbot.urls'), name="chatbot"),
    # ... resto de URLs
]
```

## Migraciones

Para crear las tablas en la base de datos:

```bash
python manage.py makemigrations chatbot
python manage.py migrate
```

## Permisos

Todas las vistas requieren autenticación (`IsAuthenticated`). Los usuarios solo pueden acceder a sus propios mensajes guardados y a los de su empresa.

## Administración

El módulo incluye configuración completa para el admin de Django con:
- Filtros por tipo, categoría, sender, etc.
- Búsqueda en texto y etiquetas
- Vista previa de mensajes largos
- Estadísticas de uso
- Formateo amigable de fechas y tags

## Futuras Mejoras

- [ ] Sistema de notificaciones en tiempo real
- [ ] Exportación de mensajes a diferentes formatos
- [ ] Análisis de sentimientos en mensajes
- [ ] Integración con sistemas de IA
- [ ] Respuestas automáticas basadas en plantillas
- [ ] Métricas avanzadas y dashboard
