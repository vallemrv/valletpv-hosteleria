from django.core.management.base import BaseCommand, CommandError
from django.db import connection
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))


class Command(BaseCommand):
    help = 'Borra todos los datos de ventas.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):
        path = os.path.join(BASE_DIR,  "delete_db_ventas.sql")
        f = open(path, "r")
        command_sql = f.read().split(";")
        with connection.cursor() as cursor:
            for c in command_sql:
                if "DELETE" in c:
                    print("Ejecutado... %s" % c)
                    cursor.execute(c)
