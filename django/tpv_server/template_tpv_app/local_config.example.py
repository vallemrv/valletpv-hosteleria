# template_tpv_app/local_config.example.py
# 
# Archivo de configuración para datos sensibles
# Copia este archivo como local_config.py y ajusta los valores
# NUNCA subas local_config.py a GitHub

# ========================================
# APIs Keys para Inteligencia Artificial
# ========================================
OPENAI_API_KEY = "sk-proj-tu_openai_api_key_aqui"
GEMINI_API_KEY = "AIzaSyBtu_gemini_api_key_aqui"
GROQ_API_KEY = "gsk_tu_groq_api_key_aqui"
GROK_API_KEY = "xai-tu_grok_api_key_aqui"

# ========================================
# Configuración de Email SMTP
# ========================================
EMAIL_HOST_PASSWORD = 'tu_password_smtp'
EMAIL_HOST_USER = 'tu_email@tudominio.com'
EMAIL_PORT = 587
EMAIL_HOST = 'smtp.tuproveedor.com'

# ========================================
# Configuración Push Telegram
# ========================================
# Solo necesitas el token del bot para enviar notificaciones push
# Obtén el token desde @BotFather en Telegram:
# 1. Busca @BotFather en Telegram
# 2. Envía /newbot y sigue las instrucciones
# 3. Copia el token que te da

TELEGRAM_BOT_TOKEN = "123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh"

