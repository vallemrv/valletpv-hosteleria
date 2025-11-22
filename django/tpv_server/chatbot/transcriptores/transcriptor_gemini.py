from langchain_core.messages import HumanMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from ..interfaces.transcriptor_audio import TranscriptorDeAudio
from gestion.tools.config_logs import log_debug_chatbot as logger

class TranscriptorGeminiLangChain(TranscriptorDeAudio):
    """
    Implementación del transcriptor que usa una instancia de 
    ChatGoogleGenerativeAI (LangChain) en lugar de la API nativa.
    """
    def __init__(self, llm: ChatGoogleGenerativeAI):
        """
        Inicializa el transcriptor con una instancia ya creada del LLM de LangChain.

        Args:
            llm: Instancia de ChatGoogleGenerativeAI.
        """
        self.llm = llm

    async def transcribir(self, audio_bytes: bytes, mime_type: str) -> str:
        """
        Transcribe audio usando el modelo Gemini a través de LangChain.
        """
        # 1. En LangChain, el contenido multimodal se pasa en una lista dentro de HumanMessage
        mensaje_multimodal = HumanMessage(
            content=[
                {"type": "text", "text": "Transcribe el siguiente audio:"},
                {"type": "media", "mime_type": mime_type, "data": audio_bytes}, # Asumo que 'media' y 'data' es el formato que usas
            ]
        )
        
        # 2. Invocamos el LLM de LangChain
        response = await self.llm.ainvoke([mensaje_multimodal])
        
        # 3. Devolvemos el texto extraído
        return response.content.strip()