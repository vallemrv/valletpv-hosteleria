import sys
import sqlite3

def execute_sql_query(database, queries):
    conn = sqlite3.connect(database)
    cursor = conn.cursor()
    results = []

    for query in queries:
        query = query.strip().upper()
        if query.startswith("SELECT"):
            cursor.execute(query)
            query_results = cursor.fetchall()
            results.extend(query_results)
        elif query.startswith("UPDATE") or query.startswith("DELETE"):
            cursor.execute(query)
            conn.commit()
            print(f"Se ha ejecutado la consulta '{query}' con éxito.")
        else:
            print(f"La consulta '{query}' no es válida. Por favor, ingrese solo consultas SELECT, UPDATE o DELETE.")

    conn.close()
    return results

if __name__ == "__main__":
    # La base de datos donde deseas ejecutar la consulta
    database = "testtpv.sqlite3"

    # Las consultas SQL ingresadas como argumentos en la línea de comandos
    queries = sys.argv[1:]

    if not queries:
        print("No se proporcionaron consultas SQL. Por favor, ingrese al menos una consulta.")
        sys.exit(1)

    results = execute_sql_query(database, queries)

    if results:
        print("Resultados de las consultas SELECT:")
        for row in results:
            print(row)
    else:
        print("Las consultas SELECT no devolvieron resultados.")
