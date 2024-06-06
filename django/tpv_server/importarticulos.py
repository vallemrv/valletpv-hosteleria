import sqlite3
import csv

# Conectar a la base de datos SQLite
conn = sqlite3.connect('testtpv.sqlite3')
cursor = conn.cursor()

# Consultar las tablas Teclas y SubTeclas
cursor.execute("""
    SELECT 
        t.id, t.Nombre AS nombre_tecla, t.P1, t.P2, t.Tipo,
        st.nombre AS nombre_subtecla, st.incremento, st.descripcion_r
    FROM 
        teclas t
    LEFT JOIN 
        subteclas st ON t.ID = st.IDTecla
""")
rows = cursor.fetchall()

# Procesar los datos
output_data = []
for row in rows:
    tecla_id, nombre_tecla, p1, p2, tipo, nombre_subtecla, incremento, descripcion_r = row
    if tipo == "CM" or tipo == "COMPUESTA":
        if descripcion_r and len(descripcion_r) > 1:
            nombre = descripcion_r
        else:
            nombre = f"{nombre_tecla} {nombre_subtecla}"
            precio_p1 = round(p1 + (incremento or 0), 2)
            precio_p2 = round(p2 + (incremento or 0), 2)
            output_data.append([nombre, f"{precio_p1:.2f}", f"{precio_p2:.2f}"])
    else:
        output_data.append([nombre_tecla, f"{p1:.2f}", f"{p2:.2f}"])


# Escribir los datos en un fichero CSV
with open('articulos.csv', 'w', newline='', encoding='utf-8') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['nombre', 'p1', 'p2'])
    writer.writerows(output_data)

# Cerrar la conexión a la base de datos
conn.close()
