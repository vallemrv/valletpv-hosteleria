from rest_framework.decorators import api_view
from rest_framework.response import Response
from django.core.files.storage import default_storage
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from django.views import View
from django.db import connection
import openai
import os
import json
import re

openai.api_key = os.environ.get("API_KEY")

@method_decorator(csrf_exempt, name='dispatch')
class GPT3View(View):

    def post(self, request):
        try:
            body = json.loads(request.body)
        except json.JSONDecodeError:
            return JsonResponse({"error": "El cuerpo de la solicitud no es JSON válido."}, status=400)

        prompt = body.get('prompt', '')
        if not prompt:
            return JsonResponse({"error": "Falta proporcionar un 'prompt' en la consulta."}, status=400)

        try:
            response = openai.Completion.create(
                engine="text-davinci-002",
                prompt=prompt,
                max_tokens=150,
                n=1,
                stop=None,
                temperature=0.7,
            )

            text = response.choices[0].text.strip()
            return JsonResponse({"response": text})

        except Exception as e:
            return JsonResponse({"error": str(e)}, status=500)


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
def sql_query_view(request):
    if request.method == 'POST':
        sql_query = request.POST.get('sql_query')
        
        if not sql_query:
            return JsonResponse({'error': 'No SQL query provided'})

        query_type = get_query_type(sql_query)
        if not query_type:
            return JsonResponse({'error': 'Invalid SQL query type'})

        if query_type == 'SELECT':
            results = execute_select_query(sql_query)
            print(results)
            return JsonResponse(results)

        elif query_type in ['UPDATE', 'DELETE']:
            execute_update_delete_query(sql_query)
            return JsonResponse({'status': 'Query executed successfully'})

    return JsonResponse({'error': 'Invalid request method'})


def get_query_type(sql_query):
    query_type_regex = re.compile(r'^\s*(SELECT|UPDATE|DELETE)', re.IGNORECASE)
    match = query_type_regex.match(sql_query)
    return match.group(1).upper() if match else None


def execute_select_query(sql_query):
    with connection.cursor() as cursor:
        cursor.execute(sql_query)
        column_names = [col[0] for col in cursor.description]
        values = cursor.fetchall()
    return {'columnas': column_names, 'valores': values}



def execute_update_delete_query(sql_query):
    with connection.cursor() as cursor:
        cursor.execute(sql_query)
        connection.commit()