# En chatbot/ws_sender.py
from contextvars import ContextVar
from typing import  Optional, Callable
from gestion.tools.config_logs import log_debug_chatbot as logger # Asegúrate de que este import sea correcto
from asgiref.sync import async_to_sync
from langchain_core.callbacks.base import BaseCallbackHandler


# --- ContextVars para el Callback Handler ---
# ContextVar para la *función* que sabe cómo enviar mensajes (el adapter en MessageProcessor)
tool_message_sender: ContextVar[Optional[Callable[[str], None]]] = ContextVar(
    'tool_message_sender',
    default=None
)

# ContextVar para el *ID del mensaje* de la solicitud actual
current_message_id_var: ContextVar[Optional[str]] = ContextVar(
    'current_message_id',
    default=None
)

current_camarero_id_var: ContextVar[Optional[int]] = ContextVar(
    'current_camarero_id',
    default=None
)


# --- Helper Function (usada por el Callback Handler) ---

def send_tool_message(message: str):
    """
    Función helper usada internamente por el Callback Handler.
    Busca la función 'sender' real (el adapter) en el contexto y la llama.
    El adapter será responsable de buscar el 'current_message_id' en su propio contexto.
    """
    sender = tool_message_sender.get()
    message_id = current_message_id_var.get()
    if sender:
        try:
            async_to_sync(sender)(message=message, message_id=message_id, sender="status")  # Llama al adapter con el mensaje y el ID
        except Exception as e:
            logger.error(f"❌ Error llamando a la función sender desde send_tool_message: {e}", exc_info=True)
            pass
    else:
        pass
 


class CallbackHandler(BaseCallbackHandler):
    def on_chain_start(self, serialized: dict, inputs: dict, **kwargs) -> None:
        """Llamado cuando la cadena comienza a ejecutarse"""
        pass

    def on_chain_end(self, outputs: dict, **kwargs) -> None:
        """Llamado cuando la cadena termina su ejecución"""
        pass

