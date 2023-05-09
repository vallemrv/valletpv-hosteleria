from django.apps import apps


def get_models_list(app_name):
    try:
        app = apps.get_app_config(app_name)
    except LookupError:
        print(f"Error: La aplicación '{app_name}' no existe.")
        return None

    return [model.__name__ for model in app.get_models()]

def execute_action(action):
    try:
        print(accion)
        modelo = apps.get_model('gestion', action['modelo'])  # Reemplaza 'your_app_name' con el nombre de tu aplicación Django.

        accion = action['accion']

        if accion == 'SELECT':
            columnas = action['columnas']
            params = accion["parametros"]
           

            if not columnas:
                # Si no se especifican columnas, seleccionamos todos los campos del modelo.
                queryset = modelo.objects.all()
            else:
                # Seleccionamos solo las columnas especificadas.
                queryset = modelo.objects.values(*columnas)

            return list(queryset), None
        else:
            print("Accion no soportada:", accion)

    except Exception as e:
        return None, e