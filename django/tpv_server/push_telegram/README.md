# Sistema de Push Notifications para Telegram

Sistema simple de notificaciones push a Telegram basado en eventos/hooks.

## Concepto

1. **Eventos**: Define tipos de eventos (ej: `nuevo_dispositivo`, `pedido_completado`)
2. **Suscripciones**: Asocia IDs de Telegram a eventos específicos
3. **Push**: Cuando ocurre un evento, envía mensaje a usuarios suscritos

## Configuración Inicial

### 1. Ejecutar migraciones

```bash
python manage_testTPV.py makemigrations
python manage_testTPV.py migrate
```

### 2. Crear evento inicial

```bash
python manage_testTPV.py init_telegram_events
```

### 3. Obtener tu ID de Telegram

1. Habla con el bot @userinfobot en Telegram
2. Te dará tu ID numérico (ej: 123456789)

### 4. Crear suscripción en el admin

1. Ir a `/admin/`
2. Entrar en "Suscripciones Push"
3. Crear nueva:
   - **Telegram user id**: Tu ID de Telegram (123456789)
   - **Nombre usuario**: Tu nombre (para referencia)
   - **Event type**: Seleccionar "nuevo_dispositivo"
   - **Activo**: ✓ Marcado

## Uso

### Desde código Python

```python
from push_telegram.push_sender import enviar_push_telegram

# Enviar notificación personalizada
enviar_push_telegram(
    event_code='nuevo_dispositivo',
    mensaje='<b>Alerta:</b> Nuevo dispositivo detectado',
    metadata={'uid': 'ABC123'}
)

# O usar función de conveniencia
from push_telegram.push_sender import notificar_nuevo_dispositivo
notificar_nuevo_dispositivo(uid='ABC123', descripcion='Tablet cocina')
```

### Desde modelos (ya implementado)

El modelo `Dispositivo` ya envía automáticamente cuando se crea un nuevo dispositivo.

## Crear Nuevos Eventos

### 1. Crear el tipo de evento en el admin

- **Code**: `pedido_completado`
- **Nombre**: Pedido Completado
- **Descripción**: Se notifica cuando se completa un pedido
- **Activo**: ✓

### 2. Crear función de envío

```python
# En push_telegram/push_sender.py

def notificar_pedido_completado(pedido_id, mesa, total):
    mensaje = f"""
✅ <b>Pedido Completado</b>

🆔 <b>Pedido:</b> #{pedido_id}
🪑 <b>Mesa:</b> {mesa}
💰 <b>Total:</b> {total}€
    """.strip()
    
    metadata = {
        'pedido_id': pedido_id,
        'mesa': mesa,
        'total': total
    }
    
    return enviar_push_telegram('pedido_completado', mensaje, metadata)
```

### 3. Llamar desde tu código

```python
from push_telegram.push_sender import notificar_pedido_completado

# Al completar un pedido
notificar_pedido_completado(
    pedido_id=123,
    mesa='Mesa 5',
    total=45.50
)
```

## Ver Logs

Ir a `/admin/` → "Logs de Notificaciones" para ver:
- Mensajes enviados
- Errores si los hay
- Metadatos de cada envío

## Estructura de Tablas

- `telegram_event_types` - Tipos de eventos disponibles
- `telegram_subscriptions` - Quién recibe qué eventos
- `telegram_notification_logs` - Historial de envíos

## Notas

- No requiere bot corriendo constantemente
- Envía mediante API REST de Telegram
- Simple y directo
- Sin complejidad de polling/webhooks

## Descripción

Bot de Telegram integrado con el sistema ValleTPV para enviar notificaciones en tiempo real sobre eventos importantes del restaurante. Este bot está completamente integrado con el sistema de templates de ValleTPV, permitiendo crear servidores de desarrollo y producción sin exponer datos sensibles en GitHub.

## 🔐 Sistema de Configuración Segura

Este bot utiliza el sistema de templates de ValleTPV que separa:
- **Código público**: Templates y lógica de la aplicación
- **Configuración sensible**: Tokens, claves API y datos privados en `local_config.py`
- **Servidores específicos**: Generados con `create_tpv.py` sin subir a GitHub

## � Instalación y Configuración

### 1. Configurar Variables Sensibles

Edita el archivo `template_tpv_app/local_config.py`:

```python
# Configuración del Bot de Telegram
TELEGRAM_BOT_TOKEN = "123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh"  # Tu token real
TELEGRAM_BOT_NAME = "RestaurantePepe Notificaciones"  # Nombre de tu bot
TELEGRAM_ADMIN_USER_IDS = [123456789, 987654321]  # IDs de administradores
TELEGRAM_ENABLED = True  # Activar el bot
```

### 2. Crear el Bot en Telegram

1. **Habla con @BotFather** en Telegram
2. **Crea tu bot**: `/newbot`
3. **Sigue las instrucciones**:
   - Nombre: `RestaurantePepe Notificaciones`
   - Username: `restaurantepeppe_bot`
4. **Copia el token** que te da
5. **Obtén tu ID**: Envía `/start` a @userinfobot

### 3. Crear Servidor TPV con Bot

Ejecuta el script de creación:

```bash
python create_tpv.py
```

El script te preguntará:
- Datos de la empresa
- Configuración de la base de datos
- **¿Activar bot de Telegram?** → **Sí**
- Token del bot (usará el de `local_config.py`)
- Nombre del bot
- IDs de administradores

### 4. Configurar el Servidor

```bash
# Cambiar al directorio del servidor generado
cd server_TUNOMBRE

# Instalar dependencias
pip install python-telegram-bot schedule

# Ejecutar migraciones
python manage_TUNOMBRE.py migrate

# Configurar tipos de notificaciones
python manage_TUNOMBRE.py run_telegram_bot --setup-types

# Verificar configuración
python manage_TUNOMBRE.py run_telegram_bot --check-config
```

### 5. Ejecutar el Bot

```bash
# Ejecutar en modo desarrollo
python manage_TUNOMBRE.py run_telegram_bot

# O en segundo plano
nohup python manage_TUNOMBRE.py run_telegram_bot > telegram_bot.log 2>&1 &
```

### Enviar Notificaciones desde Código

#### Desde Views/Models de Django
```python
from telegram_bot.integrations.websocket_listener import send_telegram_notification

# Notificar nuevo pedido
await send_telegram_notification(
    'new_order',
    order_id=123,
    mesa='Mesa 5',
    camarero='Juan',
    total=25.50,
    items_count=3
)

# Notificar error del sistema  
await send_telegram_notification(
    'system_error',
    component='Base de Datos',
    message='Error de conexión',
    severity='high'
)
```

#### Usando el Conector TPV
```python
from telegram_bot.integrations.tpv_connector import TPVSystemConnector
from telegram_bot.bot.telegram_bot import telegram_bot

# Inicializar conector
connector = TPVSystemConnector(telegram_bot.notification_sender)

# Notificar cierre de caja
await connector.notify_cash_closing({
    'camarero': 'María',
    'total': 1250.75,
    'fecha': '28/10/2024'
})
```

## Administración

### Panel de Administración Django

Accede a `/admin/` y encontrarás:

- **Configuración de Telegram** - Configurar el bot
- **Usuarios de Telegram** - Gestionar usuarios registrados
- **Tipos de Notificación** - Configurar tipos de eventos
- **Preferencias de Notificación** - Ver preferencias por usuario
- **Notificaciones de Telegram** - Historial de mensajes enviados

### Gestión de Usuarios

#### Convertir Usuario en Administrador
1. Ve a "Usuarios de Telegram" en el admin
2. Selecciona el usuario
3. Marca "Es administrador"
4. Guarda

#### Desactivar Usuario
1. Selecciona usuarios en la lista
2. Usa la acción "Desactivar usuarios"

### Configurar Notificaciones

#### Crear Nuevo Tipo
1. Ve a "Tipos de Notificación"
2. Haz clic en "Añadir"
3. Configura:
   - **Código**: Identificador único (ej: `nuevo_articulo`)
   - **Nombre**: Nombre descriptivo
   - **Emoji**: Icono para el mensaje
   - **Prioridad**: 1-4 (4 = crítica)
   - **Activo**: ✅ Marcado

## Integración con el Sistema TPV

### WebSocket Consumer

El bot incluye un consumer de WebSocket que escucha eventos del sistema:

```python
# En tu routing.py
from telegram_bot.integrations.websocket_listener import TelegramNotificationConsumer

websocket_urlpatterns = [
    # ... otras rutas
    path("ws/telegram-notifications/", TelegramNotificationConsumer.as_asgi()),
]
```

### Eventos Soportados

| Evento del Sistema | Tipo de Notificación | Descripción |
|-------------------|---------------------|-------------|
| `pedido_creado` | `new_order` | Nuevo pedido creado |
| `pedido_completado` | `order_completed` | Pedido completado |
| `mesa_ocupada` | `table_occupied` | Mesa ocupada |
| `mesa_liberada` | `table_freed` | Mesa liberada |
| `arqueo_caja` | `cash_closing` | Cierre de caja |
| `error_sistema` | `system_error` | Error crítico |
| `chatbot_accion` | `chatbot_activity` | Actividad del chatbot |

## Estructura del Proyecto

```
telegram_bot/
├── __init__.py
├── apps.py
├── models.py          # Modelos de datos
├── admin.py           # Panel de administración
├── views.py           # Vistas y API endpoints
├── urls.py           # URLs de la aplicación
├── bot/
│   ├── __init__.py
│   ├── telegram_bot.py    # Bot principal
│   ├── handlers.py        # Handlers de comandos
│   └── notifications.py   # Sistema de notificaciones
├── integrations/
│   ├── __init__.py
│   ├── tpv_connector.py      # Conector con TPV
│   └── websocket_listener.py # Listener de WebSocket
└── management/
    └── commands/
        └── run_telegram_bot.py  # Comando de gestión
```

## Troubleshooting

### El bot no recibe mensajes
1. Verifica que el token sea correcto
2. Comprueba que el bot esté iniciado: `python manage.py run_telegram_bot --check-config`
3. Revisa los logs: `tail -f telegram_bot.log`

### Las notificaciones no se envían
1. Verifica que hay usuarios activos: `/admin/telegram_bot/telegramuser/`
2. Comprueba que los tipos de notificación estén activos
3. Revisa las preferencias de los usuarios

### Error de conexión a la base de datos
1. Ejecuta las migraciones: `python manage.py migrate`
2. Verifica la configuración de la base de datos en settings

### El bot se desconecta constantemente
1. Verifica la conexión a internet
2. Comprueba que el token no esté siendo usado por otra instancia
3. Revisa los logs para errores específicos

## Logs

Los logs del bot se guardan en:
- **Consola**: Mensajes de debug y info
- **Archivo**: `telegram_bot.log` (configurado en settings)

Niveles de log:
- `DEBUG`: Información detallada
- `INFO`: Eventos importantes
- `WARNING`: Problemas menores
- `ERROR`: Errores que requieren atención

## Seguridad

### Recomendaciones
1. **Nunca** compartas el token del bot públicamente
2. Usa variables de entorno para el token en producción
3. Limita los usuarios administradores
4. Revisa regularmente los logs por actividad sospechosa
5. Usa HTTPS para webhooks en producción

### Configuración de Producción
```python
TELEGRAM_BOT = {
    'TOKEN': os.environ.get('TELEGRAM_BOT_TOKEN'),
    'WEBHOOK_URL': 'https://tu-dominio.com/telegram/webhook/',
    'MAX_RETRIES': 3,
    'RETRY_DELAY': 1,
}
```

## Próximas Características

- [ ] Webhooks para mejor rendimiento
- [ ] Notificaciones programadas
- [ ] Integración con Celery para tareas asíncronas
- [ ] Soporte para grupos de Telegram
- [ ] Comandos interactivos avanzados
- [ ] Dashboard web en tiempo real
- [ ] Análisis y métricas detalladas

## Soporte

Para problemas o preguntas:
1. Revisa este README
2. Consulta los logs del bot
3. Verifica la configuración en el admin de Django
4. Contacta al equipo de desarrollo

---

**ValleTPV Bot** - Sistema de notificaciones inteligente para restaurantes 🍽️