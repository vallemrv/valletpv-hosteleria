# -*- coding: utf-8 -*-
import tempfile
import base64  # <-- Necesario para decodificar el audio
from chatbot.interfaces.transcriptor_audio import TranscriptorDeAudio
from groq import Groq
from django.conf import settings

class GroqTranscriptor(TranscriptorDeAudio):
    def __init__(self):
        self.client = Groq(api_key=settings.GROQ_API_KEY)

    async def transcribir(self, audio_data: str, mime_type: str) -> str:
        """
        Transcribe un string de audio codificado en Base64 utilizando Groq.
        """
        try:
            # PASO 1: Decodificar el string Base64 a bytes de audio
            # Esto es crucial porque desde el WebSocket recibes texto, no bytes.
            if ',' in audio_data:
                _header, encoded_data = audio_data.split(',', 1)
            else:
                encoded_data = audio_data
            
            decoded_audio_bytes = base64.b64decode(encoded_data)

            # PASO 2: Guardar los bytes en un archivo temporal
            file_extension = f".{mime_type.split('/')[-1]}"
            
            with tempfile.NamedTemporaryFile(suffix=file_extension, delete=True) as temp_audio_file:
                temp_audio_file.write(decoded_audio_bytes)
                temp_audio_file.flush()

                # PASO 3: Llamar a la API de Groq con el formato correcto
                with open(temp_audio_file.name, "rb") as file_to_transcribe:
                    
                    transcription = self.client.audio.transcriptions.create(
                        # CORRECTO: Se pasa el objeto de archivo directamente, no una tupla.
                        file=file_to_transcribe, 
                        model="whisper-large-v3",
                        language="es" 
                    )
            
            return transcription.text

        except base64.binascii.Error as e:
            error_msg = f"Error de decodificación Base64: {e}. El dato recibido no es un audio válido."
            # Si tienes un logger, aquí es un buen lugar para usarlo.
            # logger.error(error_msg)
            return error_msg
        except Exception as e:
            error_msg = f"Error inesperado en la transcripción con Groq: {e}"
            # logger.error(error_msg, exc_info=True)
            return error_msg