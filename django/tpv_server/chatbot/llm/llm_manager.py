# -*- coding: utf-8 -*-
from django.conf import settings
from langchain_openai import ChatOpenAI # Importamos específicamente ChatOpenAI
from langchain_google_genai import ChatGoogleGenerativeAI # Importamos específicamente ChatGoogleGenAI
from langchain_groq import ChatGroq
from langchain_xai import ChatXAI  # Para Grok xAI
import os # Mantenemos por si quieres usar variables de entorno como fallback

# Definimos la función específica para OpenAI
def create_openai_llm(
    model_name: str = "gpt-4o-mini", # Añadimos un modelo por defecto común
    temperature: float = 0.0,
    max_tokens: int = 4000,
    timeout: int = 30
) -> ChatOpenAI: # El tipo de retorno es específicamente ChatOpenAI
    """
    Crea una instancia de un modelo Chat de OpenAI (como GPT-4o).

    Args:
        model_name (str): El nombre del modelo de OpenAI a usar (ej: "gpt-4o", "gpt-3.5-turbo").
        temperature (float): La temperatura para la generación. 0.0 para respuestas deterministas.
        max_tokens (int): El máximo número de tokens a generar.
        timeout (int): El tiempo máximo de espera para la respuesta de la API (en segundos).

    Returns:
        ChatOpenAI: Una instancia configurada del modelo de chat de OpenAI.

    Raises:
        ValueError: Si la API key de OpenAI no se encuentra en settings.py o en las variables de entorno.
    """
    # Intentamos obtener la API key desde settings.py primero, luego desde variables de entorno
    api_key = getattr(settings, "OPENAI_API_KEY", os.getenv("OPENAI_API_KEY"))

    if not api_key:
        # Si no encontramos la clave, lanzamos un error claro
        raise ValueError("No se encontró la API key de OpenAI. Asegúrate de que OPENAI_API_KEY esté definida en settings.py o como variable de entorno.")

    # Creamos y devolvemos la instancia de ChatOpenAI
    return ChatOpenAI(
        openai_api_key=api_key,
        model=model_name,
        temperature=temperature,
        max_tokens=max_tokens,
        request_timeout=timeout # El parámetro en ChatOpenAI suele ser request_timeout
        # Puedes añadir otros parámetros de ChatOpenAI aquí si los necesitas
    )

def create_gemini_llm(
    model_name: str = "gemini-2.5-flash", # Añadimos un modelo por defecto común
    temperature: float = 0.0,
    max_tokens: int = 1000,
):
    api_key = getattr(settings, "GEMINI_API_KEY", os.getenv("GEMINI_API_KEY"))
    
    return ChatGoogleGenerativeAI(
        google_api_key=api_key,
        model=model_name,
        temperature=temperature,
        max_tokens=max_tokens,
        model_kwargs={
        "thinkingBudget": 0 # <--- Aquí es donde configuras el presupuesto de pensamiento
            },
    )

def create_groq_llm(
    model_name: str = "llama3-8b-8192", 
    temperature: float = 0.0,
    max_tokens: int = 4000,
    timeout: int = 30
) -> ChatGroq:
    """
    Crea una instancia de un modelo Chat de Groq.

    Args:
        model_name (str): El nombre del modelo de Groq a usar.
        temperature (float): La temperatura para la generación. 0.0 para respuestas deterministas.
        max_tokens (int): El máximo número de tokens a generar.
        timeout (int): El tiempo máximo de espera para la respuesta de la API (en segundos).

    Returns:
        ChatGroq: Una instancia configurada del modelo de chat de Groq.

    Raises:
        ValueError: Si la API key de Groq no se encuentra en settings.py o en las variables de entorno.
    """
    api_key = getattr(settings, "GROQ_API_KEY", os.getenv("GROQ_API_KEY"))

    if not api_key:
        raise ValueError("No se encontró la API key de Groq. Asegúrate de que GROQ_API_KEY esté definida en settings.py o como variable de entorno.")

    return ChatGroq(
        groq_api_key=api_key,
        model=model_name,
        temperature=temperature,
        max_tokens=max_tokens,
        request_timeout=timeout,
        n=1,
        streaming=True,
        )

def create_grok_llm(
    model_name: str = "grok-beta", 
    temperature: float = 0.0,
    max_tokens: int = 4000,
    timeout: int = 30
) -> ChatXAI:
    """
    Crea una instancia de un modelo Chat de Grok (xAI).

    Args:
        model_name (str): El nombre del modelo de Grok a usar (ej: "grok-beta", "grok-1").
        temperature (float): La temperatura para la generación. 0.0 para respuestas deterministas.
        max_tokens (int): El máximo número de tokens a generar.
        timeout (int): El tiempo máximo de espera para la respuesta de la API (en segundos).

    Returns:
        ChatXAI: Una instancia configurada del modelo de chat de Grok xAI.

    Raises:
        ValueError: Si la API key de Grok no se encuentra en settings.py o en las variables de entorno.
    """
    api_key = getattr(settings, "GROK_API_KEY", os.getenv("GROK_API_KEY"))

    if not api_key:
        raise ValueError("No se encontró la API key de Grok. Asegúrate de que GROK_API_KEY esté definida en settings.py o como variable de entorno.")

    return ChatXAI(
        api_key=api_key,
        model=model_name,
        temperature=temperature,
        max_tokens=max_tokens,
        timeout=timeout,
    )

# ==================== CONSTANTES DE MODELOS ====================

# Modelos OpenAI
OPENAI_GPT_4O_MINI = "gpt-4o-mini"
OPENAI_GPT_5_MINI_2025_08_07 = "gpt-5-mini-2025-08-07"

# Modelos Gemini
GEMINI_2_5_FLASH = "gemini-2.5-flash"
GEMINI_2_5_LITE = "gemini-2.5-lite"

# Modelos Groq
GROQ_LLAMA_3_1_8B_INSTANT = "llama-3.1-8b-instant"
GROQ_LLAMA_3_3_70B_VERSATILE = "llama-3.3-70b-versatile"
GROQ_META_LLAMA_GUARD_4_12B = "meta-llama/llama-guard-4-12b"
GROQ_OPENAI_GPT_OSS_120B = "openai/gpt-oss-120b"
GROQ_OPENAI_GPT_OSS_20B = "openai/gpt-oss-20b"
GROQ_QWEN_3_32B = "qwen/qwen3-32b"

# Modelos Grok (xAI)
GROK_4_FAST_NON_REASONING = "grok-4-fast-non-reasoning"
GROK_4_FAST_REASONING = "grok-4-fast-reasoning"
GROK_CODE_FAST_1 = "grok-code-fast-1"

# Mapeo de modelos a sus providers
MODEL_PROVIDERS = {
    # OpenAI
    OPENAI_GPT_4O_MINI: "openai",
    OPENAI_GPT_5_MINI_2025_08_07: "openai",
    
    # Gemini
    GEMINI_2_5_FLASH: "gemini",
    GEMINI_2_5_LITE: "gemini",
    
    # Groq
    GROQ_LLAMA_3_1_8B_INSTANT: "groq",
    GROQ_LLAMA_3_3_70B_VERSATILE: "groq",
    GROQ_META_LLAMA_GUARD_4_12B: "groq",
    GROQ_OPENAI_GPT_OSS_120B: "groq",
    GROQ_OPENAI_GPT_OSS_20B: "groq",
    GROQ_QWEN_3_32B: "groq",
    
    # Grok
    GROK_4_FAST_NON_REASONING: "grok",
    GROK_4_FAST_REASONING: "grok",
    GROK_CODE_FAST_1: "grok",
}

# ==================== MANAGER CENTRALIZADO ====================

def create_llm(model_name: str, temperature: float = 0.1, max_tokens: int = 4000, timeout: int = 30):
    """
    Manager centralizado para crear instancias de LLM.
    
    Args:
        model_name (str): El nombre del modelo (usa las constantes definidas arriba).
        temperature (float): La temperatura para la generación. 0.0 para respuestas deterministas.
        max_tokens (int): El máximo número de tokens a generar.
        timeout (int): El tiempo máximo de espera para la respuesta de la API (en segundos).
    
    Returns:
        Una instancia del LLM correspondiente.
    
    Raises:
        ValueError: Si el modelo no está soportado.
    
    Examples:
        >>> llm = create_llm(GEMINI_2_5_FLASH, temperature=0.1, max_tokens=2000)
        >>> llm = create_llm(GROQ_LLAMA_3_3_70B_VERSATILE)
        >>> llm = create_llm(GROK_4_FAST_REASONING)
    """
    provider = MODEL_PROVIDERS.get(model_name)
    
    if not provider:
        raise ValueError(f"Modelo '{model_name}' no soportado. Modelos disponibles: {list(MODEL_PROVIDERS.keys())}")
    
    if provider == "openai":
        return create_openai_llm(model_name=model_name, temperature=temperature, max_tokens=max_tokens, timeout=timeout)
    elif provider == "gemini":
        return create_gemini_llm(model_name=model_name, temperature=temperature, max_tokens=max_tokens)
    elif provider == "groq":
        return create_groq_llm(model_name=model_name, temperature=temperature, max_tokens=max_tokens, timeout=timeout)
    elif provider == "grok":
        return create_grok_llm(model_name=model_name, temperature=temperature, max_tokens=max_tokens, timeout=timeout)
    else:
        raise ValueError(f"Provider '{provider}' no implementado.")

