import openai
import os

openai.api_key = os.environ.get("API_KEY")

def transcribe_audio(file_path):
    try:
        auido_file = open(file_path, "rb")
        transcript = openai.Audio.transcribe("whisper-1", auido_file)
        
        return transcript.text

    except Exception as e:
        print("Error al transcribir el audio:", e)
        return None
    
