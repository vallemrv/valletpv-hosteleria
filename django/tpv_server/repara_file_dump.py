import os
import django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'server_testTPV.settings')
django.setup()

import json
from datetime import datetime
from django.utils import timezone
from django.contrib.auth.hashers import make_password

def convert_fecha(fecha):
    try:
        naive_datetime = datetime.strptime(fecha, '%Y/%m/%d')
        aware_datetime = timezone.make_aware(naive_datetime)
        return aware_datetime.strftime('%Y-%m-%d')
    except ValueError:
        return timezone.now().strftime('%Y-%m-%d')

def generate_username(nombre, apellidos):
    user_name = f'{nombre.lower()}_{apellidos.lower()}'
    user_name = user_name.replace(' ', '_')
    return user_name

def process_json_file(file_name):
    with open(file_name, 'r') as file:
        data = json.load(file)

    output_data = []
    user_pk = 1 

    for entry in data:
        if entry.get('model') == 'gestion.sync':
            continue

        fields = entry.get('fields', {})
       
        if 'fecha' in fields:
            fields['fecha'] = convert_fecha(fields['fecha'])
        if entry.get('model') in ['gestion.familias', 'gestion.lineaspedido']:
            if 'cantidad' in fields:
                fields['can_composicion'] = fields["cantidad"]
                del fields["cantidad"]
        if entry.get('model') == 'gestion.camareros':
            if 'nombre' in fields and 'apellidos' in fields:
                user_name = generate_username(fields['nombre'], fields['apellidos'])
                fields['user_name'] = user_name
            if 'activo' in fields:
                fields['activo'] = True if fields['activo'] == 1 else False
            if 'autorizado' in fields:
                fields['autorizado'] = True if fields['autorizado'] == 1 else False
            if 'fecha_creacion' not in fields:
                fields['fecha_creacion'] = timezone.now().strftime('%Y-%m-%d')
            if 'email' in fields:
                del fields['email']
            if 'permisos' in fields:
                del fields['permisos']
            if 'pass_field' in fields:
                password_hash = make_password(fields['pass_field'])  # Hash the plain text password
                del fields['pass_field']

                # Create a user entry
                user_entry = {
                    "model": "auth.user",
                    "pk": user_pk,
                    "fields": {
                        "username": user_name,
                        "password": password_hash
                    }
                }
                output_data.append(user_entry)  # Add the user entry to the output data
                user_pk += 1  # Increment the user primary key

                # Set the user for the Camareros entry to the pk of the created user
                fields['user'] = user_entry['pk']

        output_data.append(entry)

    with open('output.json', 'w') as output_file:
        json.dump(output_data, output_file, indent=2)

if __name__ == '__main__':
    process_json_file('sl_dump.json')
