import os
from django.conf import settings
from django.core.files.storage import FileSystemStorage
from django.http import JsonResponse
from api_android.decorators import verificar_uid_activo


@verificar_uid_activo  # Utiliza tu decorador para verificar el UID
def subir_audio(request):
    if request.method == 'POST':
        # Verificar si se recibió el archivo
        if 'audio' not in request.FILES:
            return JsonResponse({'error': 'No se proporcionó ningún archivo de audio'}, status=400)

        # Obtener el archivo de audio del request
        audio_file = request.FILES['audio']
        file_name = audio_file.name

        # Definir la carpeta "whisper" dentro de MEDIA_ROOT
        whisper_folder = os.path.join(settings.MEDIA_ROOT, 'whisper')

        # Asegurarse de que la carpeta exista
        if not os.path.exists(whisper_folder):
            os.makedirs(whisper_folder)

        # Crear una instancia de FileSystemStorage con la carpeta correcta
        fs = FileSystemStorage(location=whisper_folder)

        # Verificar si ya existe un archivo con el mismo nombre y eliminarlo si existe
        existing_file_path = os.path.join(whisper_folder, file_name)
        if os.path.exists(existing_file_path):
            os.remove(existing_file_path)


        # Guardar el archivo usando FileSystemStorage
        saved_file_path = fs.save(file_name, audio_file)

        # Obtener la URL pública del archivo
        file_url = fs.url(saved_file_path)

        return JsonResponse({
            'message': 'Archivo de audio subido correctamente',
            'file_url': file_url
        }, status=200)

    return JsonResponse({'error': 'Método no permitido'}, status=405)