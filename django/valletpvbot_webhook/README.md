# Valle TPV Bot Webhook# ValleTPV Bot Webhook



Sistema de webhook para el bot de Telegram de Valle TPV.Sistema de webhook para bot de Telegram que actúa como router central para múltiples instancias de servidores TPV (Terminal Punto de Venta).



## Funcionalidad## 🎯 Características



- Procesa comando `/start` para registrar usuarios nuevos- **Router inteligente**: Asigna automáticamente usuarios a instancias TPV según estrategias de balanceo de carga

- Enruta callbacks de botones inline a instancias TPV correspondientes- **Multi-instancia**: Soporta múltiples servidores TPV conectados simultáneamente

- Sistema simple sin dependencias entre servidores- **API REST completa**: Para que los TPVs se registren, reciban instrucciones y envíen mensajes push

- **Gestión de estado**: Seguimiento del estado de instrucciones y mensajes

## Instalación- **Heartbeat**: Monitoreo de conexión de las instancias TPV

- **Escalable**: Diseñado para crecer con múltiples TPVs

### 1. Crear entorno virtual e instalar dependencias

## 📋 Requisitos

```bash

cd /home/valle/proyectos/valletpv-hosteleria/django/valletpvbot_webhook- Python 3.9+

- PostgreSQL 12+ (o SQLite para desarrollo)

# Crear entorno virtual- Redis (opcional, para Celery)

python3 -m venv venv- Bot de Telegram (obtener token desde @BotFather)

source venv/bin/activate

## 🚀 Instalación

# Instalar dependencias

pip install -r requirements.txt### 1. Clonar y preparar entorno

```

```bash

### 2. Configurar variables de entornocd /home/valle/proyectos/valletpv-hosteleria/django/valletpvbot_webhook



```bash# Crear entorno virtual

# Copiar archivo de ejemplopython -m venv venv

cp .env.example .envsource venv/bin/activate  # En Windows: venv\Scripts\activate



# Editar con tus configuraciones# Instalar dependencias

nano .envpip install -r requirements.txt

``````



**Configuraciones requeridas en `.env`:**### 2. Configurar variables de entorno

```bash

DEBUG=False```bash

SECRET_KEY=tu_clave_secreta_django_aqui# Copiar archivo de ejemplo

ALLOWED_HOSTS=valletpvbot.vallemrv.es,localhostcp .env.example .env



TELEGRAM_BOT_TOKEN=8480781390:AAEmu5kUZq8cUl6u0B1tm4kdHfFEwCmogxs# Editar .env con tus valores

TELEGRAM_WEBHOOK_URL=https://valletpvbot.vallemrv.es/webhook/telegram/nano .env

```

# Base de datos

DB_ENGINE=django.db.backends.postgresqlVariables importantes:

DB_NAME=valletpvbot_webhook- `TELEGRAM_BOT_TOKEN`: Token del bot de Telegram

DB_USER=tu_usuario- `TELEGRAM_WEBHOOK_URL`: URL pública del webhook (ej: https://tu-dominio.com/webhook/telegram/)

DB_PASSWORD=tu_password- `DB_*`: Configuración de base de datos

DB_HOST=localhost- `CORS_ALLOWED_ORIGINS`: URLs de tus servidores TPV

DB_PORT=5432

```### 3. Inicializar base de datos



### 3. Configurar base de datos```bash

python manage.py makemigrations

```bashpython manage.py migrate

# Crear base de datos PostgreSQLpython manage.py createsuperuser

sudo -u postgres createdb valletpvbot_webhook```

sudo -u postgres createuser tu_usuario

sudo -u postgres psql -c "ALTER USER tu_usuario WITH PASSWORD 'tu_password';"### 4. Ejecutar servidor

sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE valletpvbot_webhook TO tu_usuario;"

```bash

# Aplicar migraciones# Desarrollo

python manage.py migratepython manage.py runserver 0.0.0.0:8000



# Crear superusuario# Producción con Gunicorn

python manage.py createsuperusergunicorn config.wsgi:application --bind 0.0.0.0:8000 --workers 4

``````



### 4. Configurar webhook de Telegram### 5. Configurar webhook de Telegram



```bashUna vez el servidor esté corriendo en una URL pública, configura el webhook:

# Configurar URL del webhook en Telegram

curl -X POST "https://api.telegram.org/bot8480781390:AAEmu5kUZq8cUl6u0B1tm4kdHfFEwCmogxs/setWebhook" \```bash

  -H "Content-Type: application/json" \curl -X POST http://tu-servidor:8000/webhook/setup/ \

  -d '{"url": "https://valletpvbot.vallemrv.es/webhook/telegram/"}'  -H "Authorization: Token <tu-token-admin>"

```

# Verificar webhook

curl "https://api.telegram.org/bot8480781390:AAEmu5kUZq8cUl6u0B1tm4kdHfFEwCmogxs/getWebhookInfo"O desde el panel de admin de Django en: `/admin/`

```

## 🔌 API para TPVs

### 5. Instalar como servicio systemd

### 1. Registrar un TPV

```bash

# Instalar el servicio```bash

./manage_webhook_service.sh installcurl -X POST http://localhost:8000/api/tpv/register/ \

  -H "Content-Type: application/json" \

# Iniciar el servicio  -d '{

./manage_webhook_service.sh start    "name": "TPV Restaurante Principal",

    "endpoint_url": "http://localhost:8001",

# Ver estado    "max_users": 100,

./manage_webhook_service.sh status    "version": "1.0.0"

  }'

# Ver logs en tiempo real```

./manage_webhook_service.sh logs

```Respuesta:

```json

## Gestión del Servicio{

  "id": "uuid-del-tpv",

El script `manage_webhook_service.sh` proporciona comandos para gestionar el servicio:  "name": "TPV Restaurante Principal",

  "api_key": "tu-api-key-generado-automaticamente",

```bash  "message": "TPV registrado correctamente. Guarda el API Key de forma segura."

./manage_webhook_service.sh [comando]}

```

Comandos disponibles:

  install      - Instala el servicio en systemd**⚠️ IMPORTANTE**: Guarda el `api_key` de forma segura. Lo necesitarás para todas las peticiones.

  start        - Inicia el servicio

  stop         - Detiene el servicio### 2. Heartbeat (mantener conexión activa)

  restart      - Reinicia el servicio

  status       - Muestra el estado del servicio```bash

  logs         - Muestra logs en tiempo real (systemd)curl -X POST http://localhost:8000/api/tpv/heartbeat/ \

  logs-error   - Muestra logs de errores (gunicorn)  -H "Authorization: ApiKey tu-api-key"

  logs-access  - Muestra logs de acceso (gunicorn)```

  uninstall    - Desinstala el servicio

```**Recomendación**: Enviar heartbeat cada 60-120 segundos.



## Configuración de Nginx (Proxy Reverso)### 3. Obtener instrucciones pendientes



```nginx```bash

server {curl -X GET "http://localhost:8000/api/tpv/instructions/?status=pending&limit=10" \

    listen 80;  -H "Authorization: ApiKey tu-api-key"

    server_name valletpvbot.vallemrv.es;```

    

    # Redirigir a HTTPSRespuesta:

    return 301 https://$server_name$request_uri;```json

}{

  "count": 2,

server {  "instructions": [

    listen 443 ssl http2;    {

    server_name valletpvbot.vallemrv.es;      "id": "uuid-instruccion",

          "command": "process_message",

    ssl_certificate /etc/letsencrypt/live/valletpvbot.vallemrv.es/fullchain.pem;      "payload": {

    ssl_certificate_key /etc/letsencrypt/live/valletpvbot.vallemrv.es/privkey.pem;        "chat_id": 123456789,

            "text": "Hola, quiero hacer un pedido"

    location / {      },

        proxy_pass http://127.0.0.1:8100;      "status": "sent",

        proxy_set_header Host $host;      "telegram_user_id": 123456789,

        proxy_set_header X-Real-IP $remote_addr;      "telegram_username": "usuario",

        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;      "created_at": "2025-11-09T10:30:00Z"

        proxy_set_header X-Forwarded-Proto $scheme;    }

        proxy_read_timeout 120s;  ]

    }}

    ```

    location /static/ {

        alias /home/valle/proyectos/valletpv-hosteleria/django/valletpvbot_webhook/staticfiles/;### 4. Actualizar estado de instrucción

    }

}```bash

```curl -X PUT http://localhost:8000/api/tpv/instructions/uuid-instruccion/ \

  -H "Authorization: ApiKey tu-api-key" \

## Desarrollo  -H "Content-Type: application/json" \

  -d '{

```bash    "status": "completed",

# Activar entorno virtual    "response": {"result": "Pedido procesado correctamente"}

source venv/bin/activate  }'

```

# Ejecutar en modo desarrollo

python manage.py runserver 0.0.0.0:8100Estados posibles: `delivered`, `processing`, `completed`, `failed`



# Crear migraciones### 5. Enviar mensaje push a usuario

python manage.py makemigrations

```bash

# Aplicar migracionescurl -X POST http://localhost:8000/api/tpv/push/ \

python manage.py migrate  -H "Authorization: ApiKey tu-api-key" \

```  -H "Content-Type: application/json" \

  -d '{

## Flujo de Funcionamiento    "telegram_user_id": 123456789,

    "message_type": "text",

### 1. Registro de Usuario    "text": "¡Tu pedido está listo! 🎉"

- Usuario envía `/start` al bot  }'

- Webhook responde con su ID de Telegram```

- Usuario copia el ID y lo comunica al administrador

- Administrador registra el ID en el TPV correspondienteTipos de mensaje:

- `text`: Mensaje de texto simple

### 2. Notificación Push- `photo`: Imagen (requiere `photo_url` en `data`)

- TPV envía mensaje directamente a Telegram con botones- `document`: Documento (requiere `document_url` en `data`)

- Botones contienen URLs completas: `https://tpvtest.valletpv.es/api/dispositivo/activate?uid=xxx&token=yyy`

- TPV guarda autorizaciones en su BD localEjemplo con foto:

```json

### 3. Callback de Botón{

- Usuario pulsa botón en Telegram  "telegram_user_id": 123456789,

- Telegram envía callback al webhook  "message_type": "photo",

- Webhook extrae URL del callback y la llama  "text": "Tu ticket de compra",

- TPV valida token en su BD local y ejecuta acción  "data": {

- TPV responde con resultado    "photo_url": "https://ejemplo.com/ticket.jpg"

  }

## Logs}

```

Los logs se guardan en:

- `logs/access.log` - Logs de acceso HTTP### 6. Obtener usuarios asignados

- `logs/error.log` - Logs de errores

- `journalctl -u valletpvbot_webhook` - Logs de systemd```bash

curl -X GET http://localhost:8000/api/tpv/users/ \

## Puerto  -H "Authorization: ApiKey tu-api-key"

```

El servicio escucha en el puerto `8100`

### 7. Obtener estadísticas del TPV

## Endpoints

```bash

- `POST /webhook/telegram/` - Recibe actualizaciones de Telegramcurl -X GET http://localhost:8000/api/tpv/stats/ \

- `GET /webhook/info/` - Información del webhook  -H "Authorization: ApiKey tu-api-key"

```

## 🔄 Flujo de trabajo

### Recepción de mensajes de usuarios

1. Usuario envía mensaje al bot de Telegram
2. Telegram envía actualización al webhook (`/webhook/telegram/`)
3. El webhook:
   - Registra o actualiza el usuario
   - Asigna un TPV si es un usuario nuevo (estrategia de balanceo)
   - Crea una **Instrucción** pendiente para el TPV
4. El TPV consulta periódicamente por instrucciones (`GET /api/tpv/instructions/`)
5. El TPV procesa la instrucción y actualiza su estado
6. El TPV puede responder enviando un mensaje push

### Envío de mensajes push desde TPV

1. TPV quiere notificar a un usuario
2. TPV llama a `POST /api/tpv/push/`
3. El webhook envía el mensaje a través de Telegram
4. Retorna el resultado al TPV

## 📊 Modelos de datos

### TPVInstance
- Instancia de servidor TPV registrado
- Contiene API Key, URL, configuración
- Monitorea heartbeat y estado

### TelegramUser
- Usuario de Telegram registrado
- Asignado a una instancia TPV
- Tracking de interacciones

### Instruction
- Comando/instrucción enviada de Telegram → TPV
- Estados: pending, sent, delivered, processing, completed, failed
- Incluye payload con datos del mensaje

### PushMessage
- Mensaje enviado de TPV → Usuario de Telegram
- Soporta texto, fotos, documentos
- Tracking de envío y errores

## 🎛️ Panel de administración

Accede a `/admin/` para:
- Ver y gestionar instancias TPV
- Monitorear usuarios y asignaciones
- Revisar instrucciones y su estado
- Ver mensajes push enviados
- Estadísticas y logs

## 🔐 Seguridad

- Autenticación mediante API Keys únicas por TPV
- CSRF deshabilitado solo para webhook de Telegram
- CORS configurado para URLs específicas de TPVs
- Validación de API Keys en cada petición
- Heartbeat para detectar TPVs caídos

## 🧪 Testing

```bash
# Ejecutar tests
python manage.py test

# Con cobertura
pip install coverage
coverage run --source='.' manage.py test
coverage report
```

## 📦 Estructura del proyecto

```
valletpvbot_webhook/
├── config/              # Configuración Django
│   ├── settings.py
│   ├── urls.py
│   └── wsgi.py
├── webhook/             # App del webhook de Telegram
│   ├── models.py        # Modelos (TPV, User, Instruction, PushMessage)
│   ├── views.py         # Endpoint del webhook
│   ├── router.py        # Lógica de routing
│   ├── telegram_service.py  # Cliente de Telegram API
│   └── admin.py
├── tpv_api/             # API REST para TPVs
│   ├── views.py         # Endpoints de la API
│   ├── serializers.py   # Serializers DRF
│   ├── authentication.py  # Auth por API Key
│   └── urls.py
├── manage.py
├── requirements.txt
└── README.md
```

## 🔧 Configuración del TPV (Cliente)

Para conectar tu servidor TPV al webhook, necesitas:

### 1. Registrar el TPV (una sola vez)

```python
import requests

response = requests.post(
    'http://webhook-server:8000/api/tpv/register/',
    json={
        'name': 'Mi TPV',
        'endpoint_url': 'http://mi-tpv-server:8001',
        'max_users': 100,
        'version': '1.0.0'
    }
)

api_key = response.json()['api_key']
# Guardar api_key en configuración del TPV
```

### 2. Enviar heartbeat periódicamente

```python
import requests
import time

API_KEY = 'tu-api-key'
WEBHOOK_URL = 'http://webhook-server:8000'

while True:
    try:
        requests.post(
            f'{WEBHOOK_URL}/api/tpv/heartbeat/',
            headers={'Authorization': f'ApiKey {API_KEY}'}
        )
        print('Heartbeat enviado')
    except Exception as e:
        print(f'Error en heartbeat: {e}')
    
    time.sleep(60)  # Cada 60 segundos
```

### 3. Consultar instrucciones

```python
import requests
import time

API_KEY = 'tu-api-key'
WEBHOOK_URL = 'http://webhook-server:8000'

def poll_instructions():
    while True:
        try:
            response = requests.get(
                f'{WEBHOOK_URL}/api/tpv/instructions/?status=pending&limit=10',
                headers={'Authorization': f'ApiKey {API_KEY}'}
            )
            
            instructions = response.json()['instructions']
            
            for instruction in instructions:
                process_instruction(instruction)
                
        except Exception as e:
            print(f'Error polling: {e}')
        
        time.sleep(5)  # Polling cada 5 segundos

def process_instruction(instruction):
    instruction_id = instruction['id']
    command = instruction['command']
    payload = instruction['payload']
    
    # Procesar según el comando
    if command == 'process_message':
        result = handle_message(payload)
    
    # Actualizar estado
    requests.put(
        f'{WEBHOOK_URL}/api/tpv/instructions/{instruction_id}/',
        headers={'Authorization': f'ApiKey {API_KEY}'},
        json={
            'status': 'completed',
            'response': result
        }
    )
```

### 4. Enviar mensajes push

```python
def send_notification(telegram_user_id, message):
    response = requests.post(
        f'{WEBHOOK_URL}/api/tpv/push/',
        headers={'Authorization': f'ApiKey {API_KEY}'},
        json={
            'telegram_user_id': telegram_user_id,
            'message_type': 'text',
            'text': message
        }
    )
    return response.json()
```

## 📈 Estrategias de routing

El sistema soporta varias estrategias para asignar usuarios a TPVs:

- **least_users** (predeterminado): Asigna al TPV con menos usuarios
- **round_robin**: Distribución rotativa entre TPVs
- **specific**: Asignación manual desde admin

Configurar en `webhook/router.py`

## 🐛 Solución de problemas

### El webhook no recibe mensajes
1. Verifica que la URL pública esté configurada correctamente
2. Comprueba el webhook: `curl https://api.telegram.org/bot<TOKEN>/getWebhookInfo`
3. Revisa los logs de Django

### TPV no recibe instrucciones
1. Verifica que el heartbeat se esté enviando
2. Comprueba el API Key
3. Revisa que el TPV esté marcado como activo en admin

### Mensajes push no se envían
1. Verifica el token del bot de Telegram
2. Comprueba que el usuario esté asignado al TPV
3. Revisa los logs de errores en el modelo PushMessage

## 📝 Licencia

Proyecto privado - Valle TPV Hostelería

## 👥 Contacto

Para soporte o consultas sobre el proyecto, contacta con el equipo de desarrollo.
