import sqlite3

# Ruta de la base de datos SQLite
database_path = 'testtpv.sqlite3'

# Conexión a la base de datos
conn = sqlite3.connect(database_path)
cursor = conn.cursor()

# Obtener lista de tablas
cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
tables = cursor.fetchall()

# Recorrer las tablas
for table in tables:
    table_name = table[0]
    # Verificar si el nombre de la tabla contiene "gestion_" y no contiene "_"
    if "gestion_" in table_name or "_" not in table_name:
        # Obtener el código CREATE TABLE
        cursor.execute(f"SELECT sql FROM sqlite_master WHERE type='table' AND name='{table_name}';")
        create_table_query = cursor.fetchone()[0]
        
        print(create_table_query)
        print("-" * 20)

# Cerrar la conexión a la base de datos
conn.close()
