import json
from datetime import datetime

def convert_fecha(fecha):
    try:
        return datetime.strptime(fecha, '%Y/%m/%d').strftime('%Y-%m-%d')
    except ValueError:
        return datetime.today().strftime('%Y-%m-%d')

def process_json_file(file_name):
    with open(file_name, 'r') as file:
        data = json.load(file)

    for entry in data:
        fields = entry.get('fields', {})
        if 'fecha' in fields:
            fields['fecha'] = convert_fecha(fields['fecha'])

    with open('output.json', 'w') as output_file:
        json.dump(data, output_file, indent=2)

if __name__ == '__main__':
    process_json_file('sl_dump.json')
