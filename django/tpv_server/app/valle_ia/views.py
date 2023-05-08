from tokenapi.http import JsonResponse, JsonError
from tokenapi.decorators import token_required
from django.core.files.storage import default_storage
from django.db import connection
import openai
import os
import json
import re

openai.api_key = os.environ.get("API_KEY")


@token_required
def upload_audio(request):
    if 'audio' not in request.FILES:
        return JsonError({"error": "No se subió ningún archivo"})

    audio_file = request.FILES['audio']
    file_dir = 'audios/'
    file_path = os.path.join(file_dir, audio_file.name)

    # Verifica si el archivo existe y elimínalo en caso afirmativo
    if default_storage.exists(file_path):
        default_storage.delete(file_path)

    # Guarda el archivo
    filename = default_storage.save(file_path, audio_file)
    
    transcript = transcribe_audio(default_storage.path(filename))
    if transcript is None:
        return JsonError({"error": "Error al transcribir el audio"})

    return JsonResponse({"transcript": transcript})



@token_required
def gpt3_api(request):
    # Parsear la pregunta de la solicitud POST
    data = json.loads(request.POST["message"])
    question = data.get("query", "")
    chat_item = {"type":"answer"}
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
           {"role":"user",
            "content": f"Para la siguiente pregunta, devuelve el modelo, acción y parámetros requeridos: {question}"},
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


def process_user_question(question):
    gpt_response = send_question_to_gpt(question)
    database_action = parse_gpt_response(gpt_response)
    
    if database_action:
        result = execute_database_action(database_action)
        return result
    else:
        return "No se encontró una acción relacionada con la base de datos."

def send_question_to_gpt(question):
    # Configura tus credenciales y parámetros de la API de OpenAI aquí
    openai.api_key = "your_api_key"

    response = openai.Completion.create(
        engine="davinci-codex",
        prompt=question,
        max_tokens=100,
        n=1,
        stop=None,
        temperature=0.5,
    )

    return response.choices[0].text

def parse_gpt_response(gpt_response):
    # Analiza la respuesta de GPT para extraer la información relevante
    # sobre la acción de la base de datos, como el nombre de la tabla y las operaciones a realizar

    # Retorna la acción o None si no hay una acción relacionada con la base de datos
    return database_action

def execute_database_action(database_action):
    # Ejecuta la acción de la base de datos utilizando el ORM de Django
    # y devuelve el resultado
    # ...
    return result



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




def transcribe_audio(file_path):
    try:
        
        auido_file = open(file_path, "rb")
        transcript = openai.Audio.transcribe("whisper-1", auido_file)
        
        return transcript.text

    except Exception as e:
        print("Error al transcribir el audio:", e)
        return None