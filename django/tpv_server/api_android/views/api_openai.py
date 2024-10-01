import openai
import tempfile
import logging
import os
from django.conf import settings
from django.http import JsonResponse
from pydub import AudioSegment
from pydub.utils import which
from api_android.decorators import verificar_uid_activo
from api_android.tools.impresion import imprimir_pedido
from gestion.models.teclados import Teclas, Subteclas
from gestion.models.mesas import Mesas
from gestion.models.pedidos import Pedidos
from django.views.decorators.csrf import csrf_exempt
from django.db.models import Q


import json
from uuid import uuid4

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


@verificar_uid_activo
def buscar_ids(request):
    if request.method == 'POST':
        teclas = Teclas.objects.all()

        # Crear la respuesta JSON con el id y nombre de las teclas encontradas
        items_response = []
        for tecla in teclas:
            # Obtener subteclas relacionadas con la tecla actual
            subteclas = Subteclas.objects.filter(tecla=tecla.id)
            if subteclas.exists():
                for subtecla in subteclas:
                    items_response.append({
                        'nombre': f"{tecla.nombre} {subtecla.nombre}",
                        'id': tecla.id
                    })
            else:
                items_response.append({
                    'nombre': tecla.nombre,
                    'id': tecla.id
                })

        # Devolver el resultado como JSON
        return JsonResponse({'items': items_response})
    else:
        return JsonResponse({'error': 'Método no permitido'}, status=405)


@verificar_uid_activo
def procesar_pedido(request):
    # Intentar decodificar el cuerpo del request a JSON

    
    try:
        data = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({"error": "Invalid JSON format"}, status=400)
    

    # Verificar que las claves 'mesa' y 'items' están presentes
    if 'mesa' not in data or 'pedido' not in data:
        return JsonResponse({"error": "Missing 'mesa' or 'pedido' in request"}, status=400)

    # Verificar que 'mesa' es una cadena
    if not isinstance(data['mesa'], str):
        return JsonResponse({"error": "'mesa' debe ser una cadena"}, status=400)

    # Verificar que 'items' es una lista
    if not isinstance(data['pedido'], list):
        return JsonResponse({"error": "'items' debe ser una lista"}, status=400)

    # Verificar que cada item en 'items' tiene las claves necesarias
    required_keys = {'id_articulo', 'cantidad', 'sugerencia', 'nombre'}
    for item in data['pedido']:
        if not isinstance(item, dict):
            return JsonResponse({"error": "Cada item debe ser un diccionario"}, status=400)
        missing_keys = required_keys - item.keys()
        if missing_keys:
            return JsonResponse({"error": f"Faltan las claves {missing_keys} en uno o más items"}, status=400)

        print(item)
        tecla = Teclas.objects.get(id=item["id_articulo"])
        print(tecla.serialize())
        for s in Subteclas.objects.filter(tecla__id=tecla.id):
            print(s.serialize())
    # Retornar éxito si todo está correcto
    return JsonResponse({"success": True})