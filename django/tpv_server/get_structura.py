import sqlite3

def get_all_tables(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = cursor.fetchall()
    return [table[0] for table in tables if table[0].startswith('gestion_') or '_' not in table[0]]


def get_table_structure(conn, table_name):
    cursor = conn.cursor()
    cursor.execute(f"PRAGMA table_info({table_name})")
    return cursor.fetchall()

def main():
    conn = sqlite3.connect('testtpv.sqlite3')  # replace 'my_database.db' with your db

    tables = get_all_tables(conn)
    for table in tables:
        print(f"Table {table}:")
        structure = get_table_structure(conn, table)
        for column in structure:
            print(column)

    conn.close()

if __name__ == "__main__":
    main()
