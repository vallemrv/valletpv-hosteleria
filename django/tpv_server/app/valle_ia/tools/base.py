from django.db import connection
import re

def ejecutar_sql(consulta):
    with connection.cursor() as cursor:
        # Dividimos la consulta en varias consultas individuales
        consultas = consulta.split(';')

        resultados = []

        for consulta in consultas:
            consulta = consulta.strip() # Eliminamos espacios en blanco al inicio y al final

            if not consulta:  # Ignoramos consultas vacías
                continue
 
            # Detectamos el tipo de consulta con una expresión regular
            match = re.match(r"(\w+)", consulta, re.I)

            if match:
                consulta_tipo = match.group(1).lower()
            else:
                resultados.append("No se pudo detectar el tipo de consulta.")
                continue

            if consulta_tipo == 'select':
                cursor.execute(consulta)
                # Obtenemos los nombres de las columnas
                columnas = [col[0] for col in cursor.description]
                # Fetch rows
                filas = [
                    dict(zip(columnas, fila))
                    for fila in cursor.fetchall()
                ]
                resultados.append({'tabla':filas})
            elif consulta_tipo in ['update', 'insert', 'delete']:
                try:
                    cursor.execute(consulta)
                    # Commits the changes
                    connection.commit()
                    resultados.append("La consulta se ha ejecutado con éxito.")
                except Exception as e:
                    resultados.append({'error': str(e)})
            else:
                resultados.append({"error" : f"Tipo de consulta no soportado: {consulta_tipo}"})

        return resultados
