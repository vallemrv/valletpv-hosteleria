import json
import os
from .constantes import estructura_bd
from django.conf import settings


def identificar_tablas(frase):
    path = os.path.join(settings.STATIC_ROOT, "palabras_clave.json")
    with open(path) as f:
        palabras_clave = json.load(f)

    palabras = frase.split()
    tablas_necesarias = []
    for palabra in palabras:
        if palabra.lower() in palabras_clave:  # palabras_clave es tu diccionario de palabras clave a tablas
            tablas_necesarias.append(palabras_clave[palabra])
    return tablas_necesarias


def obtener_campos(tablas_necesarias):
    campos_necesarios = {}
    for tabla in tablas_necesarias:
        campos_necesarios[tabla] = estructura_bd[tabla]  # Asume que todos los campos son necesarios, ajusta según sea necesario
    return campos_necesarios



