from django.core.management.base import BaseCommand, CommandError
from django.db import connection
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))


class Command(BaseCommand):
    help = 'Inserta datos iniciales en la base de datos.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):
        path = os.path.join(BASE_DIR, "template_tpv_app", "delete_db.sql")
        f = open(path, "r")
        command_sql = f.read().split(";")
        with connection.cursor() as cursor:
            for c in command_sql:
                if "DELETE" in c:
                    print("Ejecutado... %s" % c)
                    cursor.execute(c)
