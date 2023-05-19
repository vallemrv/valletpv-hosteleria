import sqlite3

def get_create_table_statements(database_name):
    # Conéctate a la base de datos SQLite
    con = sqlite3.connect(database_name)

    # Crea un cursor
    cur = con.cursor()

    # Obtén el nombre de todas las tablas
    cur.execute("SELECT name FROM sqlite_master")
    tables = cur.fetchall()

    create_statements = []
    print(tables)
    for table in tables:
        table = table[0]
        if "gestion_" in table or "_" not in table:
            # Obtén la sentencia CREATE TABLE
            cur.execute(f"SELECT sql FROM sqlite_master WHERE type='table' AND name='{table}';")
            create_statement = cur.fetchone()[0]
            create_statements.append(create_statement)

    # Cierra la conexión
    con.close()

    return create_statements

print(get_create_table_statements("../../../testtpv.sqlite3"))
