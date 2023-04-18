import sqlite3

def execute_sql_query(database, query):
    conn = sqlite3.connect(database)
    cursor = conn.cursor()

    cursor.execute(query)
    conn.commit()

    if query.startswith("SELECT"):
        results = cursor.fetchall()
        conn.close()
        return results
    else:
        conn.close()
        return None

# La consulta SQL que deseas ejecutar
query = "SELECT name, sql FROM sqlite_master WHERE type = 'table' AND (name LIKE 'gestion_%' or name NOT LIKE '%\_%' ESCAPE '\\');"
#query = "SELECT * FROM teclas WHERE ID IN (SELECT IDTecla FROM teclaseccion WHERE IDSeccion IN (SELECT ID FROM secciones WHERE Nombre = 'Bocadillos'))"


# La base de datos donde deseas ejecutar la consulta
database = "testtpv.sqlite3"

results = execute_sql_query(database, query)

if results:
    result = "{"
    for row in results:
        result += "'%s':'%s'," % (row[0], row[1].replace("CREATE TABLE \"%s\" " % row[0],""))
    
    result += "}"
    r = open("structura_info.py", "w")
    r.write("models="+result)
else:
    print("La consulta no devuelve resultados.")
