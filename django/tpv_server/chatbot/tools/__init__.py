from .ventas_funcs import tools as ventas_tools
from .mesas_funcs import tools as mesas_tools
from .math_funcs import tools as math_tools
from .Infmesas_funcs import tools as infmesas_tools
from .data_time_funcs import date_time_tools
from .camareros_funcs import tools as camareros_tools
from .articulos_funcs.teclados_funcs import tools as teclados_tools
from .articulos_funcs.secciones_funcs import tools as secciones_tools
from .articulos_funcs.receptores_funcs import tools as receptores_tools
from .articulos_funcs.familias_funcs import tools as familias_tools
from .articulos_funcs.composiciones_funcs import tools as composiciones_tools
from .users_funcs import tools as users_tools
from .dispositivos_funcs import tools as dispostivos_tools
from .comandas.embeding_teclas_y_mesas import tools as teclas_y_mesas_tools
from .telegram_funcs import telegram_tools



# Array global con todas las herramientas disponibles
all_tools = (
    ventas_tools +
    mesas_tools +
    math_tools +
    infmesas_tools +
    date_time_tools +
    camareros_tools +
    teclados_tools +
    familias_tools +
    composiciones_tools+
    users_tools+
    dispostivos_tools+
    secciones_tools +
    receptores_tools +
    teclas_y_mesas_tools +
    telegram_tools
)

# Exportar todas las herramientas para facilitar el acceso
__all__ = ['all_tools']
