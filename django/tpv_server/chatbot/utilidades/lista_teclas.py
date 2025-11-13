from gestion.models.teclados import Teclas
from chatbot.decorators.db_connection_manager import db_connection_handler
from asgiref.sync import sync_to_async


# Renombramos la función para que sea claro que es async
async def get_nombres_teclas_unicos_async() -> list[str]:
    """
    Obtiene de forma ASÍNCRONA una lista de todos los nombres de teclas únicos.
    """
    # Esta es la forma segura de llamar al ORM de Django desde un contexto async
    lista_teclas = await sync_to_async(
        lambda: list(Teclas.objects.values_list('nombre', flat=True).distinct())
    )()
    return lista_teclas