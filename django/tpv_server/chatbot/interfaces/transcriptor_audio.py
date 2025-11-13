# interfaces/transcriptor_audio.py
from abc import ABC, abstractmethod

class TranscriptorDeAudio(ABC):
    @abstractmethod
    async def transcribir(self, audio_bytes: bytes, mime_type: str) -> str:
        pass