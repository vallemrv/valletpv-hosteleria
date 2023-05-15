from tokenapi.http import JsonResponse, JsonError
from tokenapi.decorators import token_required
from django.core.files.storage import default_storage
from .tools.openai import transcribe_audio, preguntar_gpt
import os

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


