import os
import shutil


from jinja2 import Template

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def create_secret_key():
    import string
    import random

    # Get ascii Characters numbers and punctuation (minus quote characters as they could terminate string).
    chars = ''.join([string.ascii_letters, string.digits, string.punctuation]).replace('\'', '').replace('"', '').replace('\\', '')

    SECRET_KEY = ''.join([random.SystemRandom().choice(chars) for i in range(50)])

    return SECRET_KEY



if __name__ == '__main__':
    print (BASE_DIR)
    
    # Importar todas las variables del local_config.py al principio
    from template_tpv_app.local_config import *
    
    # Crear diccionario con todas las variables sensibles disponibles
    local_config_vars = {}
    
    # APIs Keys
    local_config_vars['openai_key'] = globals().get('OPENAI_API_KEY', '')
    local_config_vars['gemini_key'] = globals().get('GEMINI_API_KEY', '')
    local_config_vars['groq_key'] = globals().get('GROQ_API_KEY', '')
    local_config_vars['grok_key'] = globals().get('GROK_API_KEY', '')
    
    # Email configuration
    local_config_vars['email_host_password'] = globals().get('EMAIL_HOST_PASSWORD', '')
    local_config_vars['email_host_user'] = globals().get('EMAIL_HOST_USER', '')
    local_config_vars['email_port'] = globals().get('EMAIL_PORT', 587)
    local_config_vars['email_host'] = globals().get('EMAIL_HOST', '')
    
    # Telegram Push - token, webhook URL y API Key
    local_config_vars['telegram_bot_token'] = globals().get('TELEGRAM_BOT_TOKEN', '')
    local_config_vars['telegram_webhook_url'] = globals().get('TELEGRAM_WEBHOOK_URL', '')
    local_config_vars['telegram_api_key'] = globals().get('TELEGRAM_API_KEY', '')
    
    print("🔑 Variables sensibles cargadas desde local_config.py:")
    print(f"   - APIs configuradas: OpenAI, Gemini, Groq, Grok")
    print(f"   - Email: {local_config_vars['email_host_user']}")
    if local_config_vars['telegram_bot_token']:
        print(f"   - Telegram Push: ✅ Token, Webhook y API Key configurados")
        print(f"     └─ Webhook: {local_config_vars['telegram_webhook_url']}")
    else:
        print(f"   - Telegram Push: ⚠️  Token no configurado")
    print()
    
    is_correct = ""
    while is_correct not in ["yes"]:
        print("Introduce los datos de la empresa:")
        print()
        nombre_empresa = input("Nombre de la comercial [Valle TPV]: ")
        if nombre_empresa == "":
            nombre_empresa = "Valle TPV"
        razon_social = input("Razón social [%a]: " % nombre_empresa)
        if razon_social == "":
            razon_social = nombre_empresa
        nif = input("NIF o CIF: ")
        direccion = input("Direccion: ")
        telefono = input("Telefono: ")
        poblacion = input("Poblacion: ")
        provincia =  input("Provicia: ")
        codio_cp = input("Codigo postal: ")
        email = input("Email de la empresa: ")
        print("Datos configuración del la aplicación:")
        print()
        name_tpv = input("Nombre del servidor tpv [testTPV]: ")
        if name_tpv == "":
            name_tpv = "testTPV"
        name_db = input("Nombre de la base: [%a]: " % name_tpv)
        if name_db == "":
            name_db = name_tpv.lower()
        sql_mode = input("Tipo de base de datos [lite/mysql] [lite]: ")
        name_user_db = name_tpv
        password_db = ""

        if sql_mode not in ["lite", "mysql"]:
            sql_mode = "lite"
       
        if sql_mode == "mysql":
            print("Datos de conexion a la base de datos. ")
            name_user_db = input("Nombre del usuario de la base de datos [valletpv]: ")
            if name_user_db == "":
                name_user_db = "valletpv"
            password_db = input("Contraseña de la base de datos []: ")
            
        
        
            
        
        print()
        print("Datos de configuración para la aplicación:")
        
        is_debug = bool(input("Debug  True o False [True]: "))
        if is_debug not in [True, False]:
            is_debug = True
       
        is_correct = input("Todos los datos correctos [yes/no]: ")
        print()
    secret_key = create_secret_key()

    asgi = open(os.path.join(BASE_DIR, "template_tpv_app", "asgi.py"), "r")
    template_asgi = Template(asgi.read())
    asgi.close()
    r_asgi = template_asgi.render(**{"name_tpv":name_tpv})


    wsgi = open(os.path.join(BASE_DIR, "template_tpv_app", "wsgi.py"), "r")
    template_wsgi = Template(wsgi.read())
    wsgi.close()
    r_wsgi = template_wsgi.render(**{"name_tpv":name_tpv})

    settings = open(os.path.join(BASE_DIR, "template_tpv_app", "settings.py"), "r")
    template_settings = Template(settings.read())
    settings.close()
    # Preparar todas las variables para el template
    template_vars = {
        "name_tpv": name_tpv,
        "name_db": name_db,
        "name_user_db": name_user_db,
        "password_db": password_db,
        "email": email,
        "nombre_empresa": nombre_empresa,
        "is_debug": is_debug,
        "secret_key": secret_key,
        "sql_mode": sql_mode,
        "razon_social": razon_social,
        "nif": nif,
        "direccion": direccion,
        "telefono": telefono,
        "poblacion": poblacion,
        "provincia": provincia,
        "cp": codio_cp,
        # Todas las variables del local_config.py
        **local_config_vars
    }
    
    r_settings = template_settings.render(**template_vars)

    manage = open(os.path.join(BASE_DIR, "template_tpv_app",  "manage_template.py"), "r")
    template_manage  = Template(manage.read())
    manage.close()
    r_manage = template_manage.render(**{"name_tpv":name_tpv})



    path = os.path.join(BASE_DIR, "server_"+ name_tpv)
    path_source = os.path.join(BASE_DIR, "template_tpv_app")
    path_media = os.path.join(BASE_DIR, "static", "media_"+name_tpv )
    path_logs = os.path.join(path_media, "logs")

    #creando el directorio de la aplicacion
    if os.path.isdir(path):
        shutil.rmtree(path)
    os.mkdir(path)

    #creando el directorio para los fichero media
    if os.path.isdir(path_media):
        shutil.rmtree(path_media)
    os.mkdir(path_media)
    
    #creando el directorio para los logs
    if not os.path.isdir(path_logs):
        os.mkdir(path_logs)

    shutil.copy(os.path.join(path_source, "__init__.py"), os.path.join(path, "__ini__.py"))
    shutil.copy(os.path.join(path_source, "routing.py"), os.path.join(path, "routing.py"))
    shutil.copy(os.path.join(path_source, "urls.py"), os.path.join(path, "urls.py"))

    f = open(os.path.join(path, "asgi.py"), "w")
    f.write(r_asgi)
    f.close()

    f = open(os.path.join(path, "wsgi.py"), "w")
    f.write(r_wsgi)
    f.close()

    f = open(os.path.join(path, "settings.py"), "w")
    f.write(r_settings)
    f.close()

    f = open(os.path.join(BASE_DIR, "manage_%s.py" % name_tpv), "w")
    f.write(r_manage)
    f.close()
