# 🤖 Webhook Router - Valle TPV Bot

**Proxy simple** para enrutar notificaciones de Telegram entre TPVs y usuarios.

## 🎯 ¿Qué hace?

1. **TPV → Webhook → Telegram**: El TPV envía un mensaje, el webhook lo reenvía a Telegram
2. **Telegram → Webhook → TPV**: Usuario pulsa botón, webhook devuelve al TPV

## 📡 API

### 1. Registrar Notificación (TPV → Webhook)

```http
POST /api/register_notification/
Content-Type: application/json
Authorization: Bearer <TPV_API_KEY>

{
  "token": "uuid-unico-temporal",
  "callback_url": "https://tpvtest.valletpv.es/api/dispositivo/action",
  "telegram_user_id": 123456789,
  "mensaje": "🆕 <b>Nuevo Dispositivo</b>\n\n📱 UID: abc123",
  "botones": [
    [
      {"text": "✅ Activar", "callback_data": "activate|uuid-token"},
      {"text": "❌ Rechazar", "callback_data": "reject|uuid-token"}
    ]
  ],
  "expira_en": "2024-12-31T23:59:59Z"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message_id": 12345
}
```

### 2. Callback del Usuario (Telegram → Webhook → TPV)

Cuando el usuario pulsa un botón, Telegram envía callback al webhook.
El webhook extrae `token` de `callback_data` y llama a `callback_url`:

```http
POST https://tpvtest.valletpv.es/api/dispositivo/action
Content-Type: application/x-www-form-urlencoded

token=uuid-token&accion=activate
```

**El TPV debe responder:**
```json
{
  "success": true,
  "mensaje": "✅ Dispositivo activado"
}
```

El webhook muestra `mensaje` al usuario en Telegram.

### 3. Editar Mensaje (TPV → Webhook)

Permite editar un mensaje ya enviado (ej: quitar botones tras activar):

```http
POST /api/edit_message/
Content-Type: application/json
Authorization: Bearer <TPV_API_KEY>

{
  "telegram_user_id": 123456789,
  "message_id": 12345,
  "nuevo_texto": "✅ <b>Dispositivo Activado</b>\n\n📱 UID: abc123",
  "botones": []
}
```

**Parámetros:**
- `botones`: `[]` = quitar botones, `[[...]]` = reemplazar, omitir = mantener

### 4. Borrar Mensaje (TPV → Webhook)

Permite borrar un mensaje enviado:

```http
POST /api/delete_message/
Content-Type: application/json
Authorization: Bearer <TPV_API_KEY>

{
  "telegram_user_id": 123456789,
  "message_id": 12345
}
```

## 🔧 Configuración

### Variables de Entorno (.env)

```bash
# Telegram
TELEGRAM_BOT_TOKEN=tu_bot_token_aqui
TELEGRAM_WEBHOOK_URL=https://valletpvbot.tudominio.com/telegram/

# API Security
TPV_API_KEY=generar-con-python-secrets-token-urlsafe-32

# Database
DB_ENGINE=django.db.backends.sqlite3
DB_NAME=db.sqlite3

# Django
SECRET_KEY=cambiar-en-produccion
DEBUG=False
ALLOWED_HOSTS=localhost,127.0.0.1,valletpvbot.tudominio.com
```

### 🔐 Generar API Key

```bash
python -c "import secrets; print(secrets.token_urlsafe(32))"
# Ejemplo: vK9hX2jF_Qp8mN3cL4rT6yW1zU5sA7bD8eG0fH9iJ2k
```

**Importante**: 
- Este API Key debe ser el **mismo en el webhook y todos los TPVs**
- Los TPVs deben incluir el header: `Authorization: Bearer <API_KEY>`
- Sin el API Key correcto, las peticiones son rechazadas con error 401

**Nota sobre CORS**: No se usa CORS porque las peticiones son servidor a servidor (Django → Django), no desde navegador

## 🚀 Despliegue

```bash
# 1. Instalar dependencias
pip install -r requirements.txt

# 2. Migrar base de datos
python manage.py migrate

# 3. Configurar webhook de Telegram
python manage.py setup_telegram_webhook

# 4. Iniciar servicio
systemctl start valletpvbot_webhook
```

## 📊 Administración

```bash
# Crear superusuario
python manage.py createsuperuser

# Acceder admin
https://valletpvbot.tudominio.com/admin/
```

Desde el admin puedes ver:
- **Token Callback Mappings**: Tokens registrados y su estado (usada/activa)

## 🔍 Logs

```bash
# Ver logs del servicio
journalctl -u valletpvbot_webhook -f

# Ver logs de Gunicorn
tail -f logs/gunicorn_error.log
```

## 📝 Modelo de Datos

```
TokenCallbackMapping
├── token (PK)               # Token único del evento
├── callback_url             # URL del TPV para callback
├── telegram_user_id         # ID del usuario Telegram
├── telegram_message_id      # ID del mensaje enviado
├── expira_en                # Fecha de expiración
├── usada                    # ¿Se usó el token?
└── metadata                 # Datos adicionales (JSON)
```

## 🧪 Testing

```bash
# Enviar notificación de prueba
curl -X POST https://valletpvbot.tudominio.com/api/register_notification/ \
  -H "Content-Type: application/json" \
  -d '{
    "token": "test-123",
    "callback_url": "https://tpvtest.valletpv.es/api/dispositivo/action",
    "telegram_user_id": 123456789,
    "mensaje": "🧪 Test",
    "botones": [[{"text": "OK", "callback_data": "ok|test-123"}]],
    "expira_en": "2024-12-31T23:59:59Z"
  }'
```

## ⚠️ Importante

- **El TPV guarda los tokens localmente** para validación de seguridad
- **El webhook solo enruta**, no valida tokens (lo hace el TPV)
- **Los tokens expiran** según `expira_en`
- **Un token solo se usa una vez** (campo `usada`)

## 🔐 Seguridad

### Autenticación de TPVs
- **API Key compartido**: Todos los TPVs usan el mismo `TPV_API_KEY`
- **Header obligatorio**: `Authorization: Bearer <TPV_API_KEY>`
- **Sin API Key**: Petición rechazada con error 401

### Seguridad de Tokens
1. TPV crea token único y lo guarda localmente
2. TPV envía token al webhook (solo para enrutamiento)
3. Usuario pulsa botón → Webhook envía token al TPV
4. **TPV valida el token** en su BD local antes de ejecutar acción
5. TPV marca token como usado

El webhook **nunca valida tokens**, solo los enruta. La validación es responsabilidad del TPV.
