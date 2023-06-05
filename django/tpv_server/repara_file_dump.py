import json
from datetime import datetime

def convert_fecha(fecha):
    try:
        return datetime.strptime(fecha, '%Y/%m/%d').strftime('%Y-%m-%d')
    except ValueError:
        return datetime.today().strftime('%Y-%m-%d')

def generate_username(nombre, apellidos):
    user_name = f'{nombre.lower()}_{apellidos.lower()}'
    user_name = user_name.replace(' ', '_')
    return user_name

def process_json_file(file_name):
    with open(file_name, 'r') as file:
        data = json.load(file)

    for entry in data:
        fields = entry.get('fields', {})
        if 'fecha' in fields:
            fields['fecha'] = convert_fecha(fields['fecha'])
        if entry.get('model') == 'gestion.camareros':
            if 'nombre' in fields and 'apellidos' in fields:
                user_name = generate_username(fields['nombre'], fields['apellidos'])
                fields['user_name'] = user_name
            if 'activo' in fields:
                fields['activo'] = True if fields['activo'] == 1 else False
            if 'autorizado' in fields:
                fields['autorizado'] = True if fields['autorizado'] == 1 else False
            if 'fecha_creacion' not in fields:
                fields['fecha_creacion'] = datetime.today().strftime('%Y-%m-%d')
            if 'email' in fields:
                del fields['email']
            if 'pass_field' in fields:
                fields['password'] = "nuevo"

                del fields['pass_field']

    with open('output.json', 'w') as output_file:
        json.dump(data, output_file, indent=2)

if __name__ == '__main__':
    process_json_file('sl_dump.json')
