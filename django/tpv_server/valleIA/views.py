from rest_framework.decorators import api_view
from rest_framework.response import Response
from django.core.files.storage import default_storage
from django.conf import settings
import openai
import os
import json
from django.apps import apps
from django.db import connection

openai.api_key = os.environ.get("API_KEY")

@api_view(['POST'])
def upload_audio(request):
    if 'audio' not in request.FILES:
        return Response({"error": "No se subió ningún archivo"}, status=400)

    audio_file = request.FILES['audio']
    file_dir = 'audios/'
    file_path = os.path.join(file_dir, audio_file.name)

    # Verifica si el archivo existe y elimínalo en caso afirmativo
    if default_storage.exists(file_path):
        default_storage.delete(file_path)

    # Guarda el archivo
    filename = default_storage.save(file_path, audio_file)
    file_url = request.build_absolute_uri(default_storage.url(filename))

    transcript = transcribe_audio(default_storage.path(filename))
    if transcript is None:
        return Response({"error": "Error al transcribir el audio"}, status=500)

    return Response({"url": file_url, "transcript": transcript})


def transcribe_audio(file_path):
    try:
        
        auido_file = open(file_path, "rb")
        transcript = openai.Audio.transcribe("whisper-1", auido_file)
        
        return transcript.text

    except Exception as e:
        print("Error al transcribir el audio:", e)
        return None



def model_info(request):
    save_model_info_to_file("db_str.json")
    Response("Holeeeeeee")

def get_model_info(cursor):
    models_info = []
    
    for model in apps.get_model("gestion", "camareros"):
        model_info = {
            "model": model.__name__,
            "table": model._meta.db_table,
            "columns": [],
            "relations": [],
        }

        for field in model._meta.get_fields():
            model_info["columns"].append({
                "name": field.name,
                "type": field.get_internal_type(),
            })

        relations = connection.introspection.get_relations(cursor, model._meta.db_table)
        for index, relation in relations.items():
            model_info["relations"].append({
                "name": model._meta.get_field(relation[1]).name,
                "target_table": apps.get_model(*relation[2])._meta.db_table,
            })

        models_info.append(model_info)

    return models_info

def save_model_info_to_file(filename):
    with connection.cursor() as cursor:
        model_info = get_model_info(cursor)
        
    with open(filename, 'w') as outfile:
        json.dump(model_info, outfile, indent=2)

