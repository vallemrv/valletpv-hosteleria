import re
from django.db import connection

def ejecutar_sql(consulta):
    consultas = dividir_consultas(consulta)
    resultados = []

    for consulta in consultas:
        tipo_consulta = get_tipo_consulta(consulta)
        if tipo_consulta == 'select':
            resultado = ejecutar_select(consulta)
        elif tipo_consulta in ['update', 'insert', 'delete']:
            resultado = ejecutar_accion(consulta)
        else:
            resultado = {"error" : f"Tipo de consulta no soportado: {tipo_consulta}"}
        
        resultados.append(resultado)

    return resultados

def dividir_consultas(consulta):
    # Dividimos la consulta en varias consultas individuales
    consultas = consulta.split(';')
    # Eliminamos espacios en blanco al inicio y al final
    consultas = [c.strip() for c in consultas if c.strip()]
    return consultas

def get_tipo_consulta(consulta):
    # Detectamos el tipo de consulta con una expresión regular
    match = re.match(r"(\w+)", consulta, re.I)
    if match:
        return match.group(1).lower()
    else:
        return None

def ejecutar_select(consulta):
    with connection.cursor() as cursor:
        try:
            cursor.execute(consulta)
            
            # Obtenemos los nombres de las columnas
            columnas = [col[0] for col in cursor.description]

            # Fetch rows
            datos = cursor.fetchall()

            # Convertimos las filas de los datos a listas (por defecto son tuplas)
            datos = [list(fila) for fila in datos]

            # Devolvemos el resultado en el formato que has especificado
            return {'tabla': {'columnas': columnas, 'datos': datos}}
        except Exception as e:
            return "Error " + str(e)

def ejecutar_accion(consulta):
    with connection.cursor() as cursor:
        try:
            cursor.execute(consulta)
            # Commits the changes
            connection.commit()
            return "La consulta se ha ejecutado con éxito."
        except Exception as e:
            return "Error " + str(e)
