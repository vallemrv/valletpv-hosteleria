import sqlite3
import os 

def get_create_table_statements(database_name):
    # Conéctate a la base de datos SQLite
    con = sqlite3.connect(database_name)

    # Crea un cursor
    cur = con.cursor()

    # Obtén el nombre de todas las tablas
    cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
    tables = cur.fetchall()

    create_statements = {}
   
    for table in tables:
        table = table[0]
        if "gestion_" in table or "_" not in table:
            # Obtén la sentencia CREATE TABLE
            cur.execute(f"SELECT sql FROM sqlite_master WHERE type='table' AND name='{table}';")
            row = cur.fetchone()
            if row is not None:
                create_statement = row[0]
                create_statements[table] = create_statement

    # Cierra la conexión
    con.close()

    return create_statements


path = os.path.join(os.getcwd(), 
                    "django", "tpv_server", "testtpv.sqlite3")

print(get_create_table_statements(path))
