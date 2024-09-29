import openai
import tempfile
import logging
import os
from django.conf import settings
from django.core.files.storage import FileSystemStorage
from django.http import JsonResponse
from pydub import AudioSegment
from pydub.utils import which
from django.views.decorators.csrf import csrf_exempt

from api_android.decorators import verificar_uid_activo
AudioSegment.converter = which("ffmpeg")

client = openai.OpenAI(api_key=settings.OPENAI_API_KEY)

def convertir_audio_a_mp3(audio_file):
    """
    Convierte un archivo de audio 3GP a MP3 usando un archivo temporal y lo devuelve.
    """
    try:
        # Crear un archivo temporal para el archivo 3GP
        with tempfile.NamedTemporaryFile(delete=False, suffix=".mp3") as temp_mp3:
            audio = AudioSegment.from_file(audio_file, format="3gp")
            audio.export(temp_mp3.name, format="mp3")  # Convertir a MP3 y guardar
            return temp_mp3.name  # Devolver la ruta del archivo MP3 convertido

    except Exception as e:
        logging.error(f"Error al convertir el archivo a MP3: {e}")
        raise

@verificar_uid_activo
def subir_audio(request):
    if request.method == 'POST':
        # Verificar si se recibió el archivo
        if 'audio' not in request.FILES:
            return JsonResponse({'error': 'No se proporcionó ningún archivo de audio'}, status=400)

        # Obtener el archivo de audio del request
        audio_file = request.FILES['audio']

        # Verificar que el archivo sea 3GP
        if audio_file.content_type != 'audio/3gp':
            return JsonResponse({'error': 'Formato de archivo no permitido. Solo se admite 3GP.'}, status=400)

        try:
            # Convertir el archivo a MP3
            audio_mp3_path = convertir_audio_a_mp3(audio_file)

            # Enviar el archivo MP3 convertido a Whisper
            with open(audio_mp3_path, 'rb') as f:
                response = client.audio.transcriptions.create(
                    model="whisper-1",
                    file=f,
                    response_format="text",
                    language="es"  # Especifica el idioma si es necesario
                )

            # Obtener la transcripción del archivo de audio
            transcription = response

            # Eliminar el archivo MP3 temporal después de usarlo
            os.remove(audio_mp3_path)
            print(transcription)

            # Responder con la transcripción
            return JsonResponse({
                'message': 'Transcripción exitosa',
                'transcription': transcription
            }, status=200)

        except Exception as e:
            logging.error(f"Error en el proceso de transcripción: {e}")
            return JsonResponse({'error': 'Error durante la transcripción'}, status=500)

    return JsonResponse({'error': 'Método no permitido'}, status=405)