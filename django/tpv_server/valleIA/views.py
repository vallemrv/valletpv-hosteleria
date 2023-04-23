from rest_framework.decorators import api_view
from rest_framework.response import Response
from django.core.files.storage import default_storage
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.db import connection
from .structura_info import models_info, no_autorizadas
import openai
import os
import json
import re

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

@csrf_exempt
@require_POST
def gpt3_api(request):
    # Parsear la pregunta de la solicitud POST
    data = json.loads(request.POST["message"])
    question = data.get("query", "")
    chat_item = {"type":"answer"}
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
           {"role":"user","content": "Only sql answer"},
           {'role':"user", "content": str(models_info)},
           {'role':"user", "content": ''' Crea una o mas, si fuera necesario, 
                                      si hay mas de una consultoa separalas por ';', 
                                      consultas SQL de: '''
                                      + question +
                                      ''' No des ningua explicación jamas, nunca, solo las sql query.
                                          Si no existe una tabla valida en la pregurnta, jamas des una explicacion, jamas,
                                          solo contesta False;
                                          Si en la pregunta no esta relacionada con la base de datso, jamas, jamas,
                                          des una consulta solo  contesta False. 
                                         '''}
        ],
        max_tokens=150,
        n=1,
        stop=None,
        temperature=0,
    )

    output_text = response.choices[0].message.content
    print(output_text)
    query_type = extract_query(output_text)

    if query_type == "SELECT":
        output_text = execute_select_query(output_text)
        chat_item['text'] = "Aqui tienes el resultado:"
        chat_item['table'] = output_text
    elif query_type in ["UPDATE", "DELETE"]:
        execute_update_delete_query(output_text)
        chat_item["text"] = "Operacion realizada con exito"
    else:
        chat_item["text"] = "Perdona pero no tengo autorización a contestar esto. Solo puedo contestar o ejecutar preguntas relacioneas con le TPV."
        

    return JsonResponse({"generated_text": chat_item})



def extract_query(gpt3_response):
    # Esta es solo una muestra de patrones de consulta; debes agregar patrones adicionales según sea necesario.
    query_patterns = [
        (r'SELECT.*?FROM', 'SELECT'),
        (r'INSERT INTO.*?VALUES', 'INSERT'),
        (r'UPDATE.*?SET', 'UPDATE'),
        (r'DELETE FROM', 'DELETE'),
    ]

    for pattern, query_type in query_patterns:
        if re.search(pattern, gpt3_response, re.IGNORECASE):
            return query_type

    return None


def execute_select_query(sql_query):
    with connection.cursor() as cursor:
        cursor.execute(sql_query)
        column_names = [col[0] for col in cursor.description]
        values = cursor.fetchall()
    return {'headers': column_names, 'data': values}

def execute_update_delete_query(sql_query):
    with connection.cursor() as cursor:
        cursor.execute(sql_query)
        connection.commit()