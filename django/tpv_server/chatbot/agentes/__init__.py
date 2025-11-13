from .main_agent import MainAgent
from .expert_agent import ExpertAgent
from pydantic import BaseModel, Field

class AgentePedidosInput(BaseModel):
    """Define la entrada para el agente de pedidos."""
    query: str = Field(description="Debe ser la petición completa del usuario en una sola cadena de texto. Incluye el pedido, la mesa y cualquier otro detalle relevante.")


__all__ = [
    "MainAgent",
    "ExpertAgent"
]

