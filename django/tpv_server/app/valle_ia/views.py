from tokenapi.http import JsonResponse, JsonError
from tokenapi.decorators import token_required
from django.core.files.storage import default_storage
from .tools.auido import transcribe_audio
from .tools.texto import parse_gpt_response_text, preguntar_gpt
from .tools.base import execute_action, get_models_list
from app.models import InfModelos

import openai
import os
import json


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
    
    transcript = transcribe_audio(default_storage.path(filename), openai)
    if transcript is None:
        return JsonError({"error": "Error al transcribir el audio"})

    return JsonResponse({"transcript": transcript})


@token_required
def gpt3_api(request):
    # Parsear la pregunta de la solicitud POST
    
    data = json.loads(request.POST["message"])
    pregunta = data.get("query", "")
    modelo = request.POST["tabla"]
    print(InfModelos.objects.all())
    
    inf = InfModelos.objects.filter(sinonimos__icontains=modelo).first()
    messages=[
            {"role": "user", "content": 
            f'''
            Eres experta en modelos de django.
            nombre del modelo {inf.nombre}, columnas:{inf.columnas}
            relaciones:{inf.relaciones}
            Para la siguiente pregunta: {pregunta} 
            devuelve: modelo:'el nombre del modelo',
                      accion: 'SELECT, INSERT, UPDATE, DELETE'
                      condiciones:"array de condiciones si no encuetras None",
                      otros:"datos sin categoria si no encuetras None",
                      valores:"array valores si no []',
                      columna:"array columnas si no []', no inventes. 
                      relaciones:"array de relaciones si no []'
                '''},
        ]


    chat_item = {"type":"answer"}
    try:
        output_text = preguntar_gpt(openai, messages)
        inst = parse_gpt_response_text(output_text)
        #modelo = inst["modelo"]
        #accion = inst["accion"]
        print(inst)
        
        
    except:
        chat_item["text"] = "No se puede realizar esta accion. Tendras que ser mas especifico."
    return JsonResponse({"result": chat_item})


