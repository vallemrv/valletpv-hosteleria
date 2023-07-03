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
    is_correct = ""
    while is_correct not in ["yes"]:
        print("Introduce los datos de la empresa:")
        print()
        nombre_empresa = input("Nombre de la comercial [Valle TPV]: ")
        if nombre_empresa == "":
            nombre_empresa = "Valle TPV"
        
        
        print("Datos configuración del la aplicación:")
        print()

        name_tpv = input("Nombre del servidor tpv [testTPV]: ")
        if name_tpv == "":
            name_tpv = "testTPV"
        name_db = input("Nombre de la base: [%a]" % name_tpv)
        if name_db == "":
            name_db = name_tpv.lower()
        sql_mode = input("Tipo de base de datos [lite/mysql] [lite]: ")
        name_user_db = ""
        if sql_mode not in ["mysql", "lite"]:
            sql_mode = "lite"
            name_user_db = name_tpv
            password_db = ""
            
        if sql_mode == "mysql":
            print("Datos de conexion a la base de datos. ")
            name_user_db = input("Nombre del usuario de la base de datos [valletpv]: ")
            if name_user_db == "":
                name_user_db = "valletpv"
            password_db = input("Contraseña de la base de datos []: ")
            
        is_debug = bool(input("Debug  True o False [True]: "))
        if is_debug not in [True, False]:
            is_debug = True
       
        print()
        print("Datos de configuración servidor smtp:")
        print()
        
        host_smtp = input("Host smtp: ") 
        port_smtp = input("Port smtp: ")
        email_smtp = input("Usuario smtp: ")
        password_mail = input("Password smtp: ")

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
    r_settings = template_settings.render(**{"name_tpv":name_tpv,
                                              "name_db": name_db,
                                              "name_user_db": name_user_db,
                                              "password_db": password_db,
                                              "nombre_empresa":nombre_empresa,
                                              "is_debug":is_debug,
                                              "secret_key":secret_key,
                                              "sql_mode":sql_mode,
                                              "password_mail":password_mail,
                                              "email_smtp":email_smtp,
                                              "port_smtp":port_smtp,
                                              "host_smtp":host_smtp
                                              })

    manage = open(os.path.join(BASE_DIR, "template_tpv_app",  "manage_template.py"), "r")
    template_manage  = Template(manage.read())
    manage.close()
    r_manage = template_manage.render(**{"name_tpv":name_tpv})



    path = os.path.join(BASE_DIR, "server_"+ name_tpv)
    path_source = os.path.join(BASE_DIR, "template_tpv_app")
    path_media = os.path.join(BASE_DIR, "static", "media_"+name_tpv )

    #creando el directorio de la aplicacion
    if os.path.isdir(path):
        shutil.rmtree(path)
    os.mkdir(path)

    #creando el directorio para los fichero media
    if os.path.isdir(path_media):
        shutil.rmtree(path_media)
    os.mkdir(path_media)

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
