import openai
import os

openai.api_key = os.environ.get("API_KEY")


def transcribe_audio(file_path, openai):
    try:
        auido_file = open(file_path, "rb")
        transcript = openai.Audio.transcribe("whisper-1", auido_file)
        
        return transcript.text

    except Exception as e:
        print("Error al transcribir el audio:", e)
        return None
    
def preguntar_gpt(messages):

    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=messages,
        max_tokens=150,
        n=1,
        stop=None,
        temperature=0,
    )

    return response.choices[0].message.content  