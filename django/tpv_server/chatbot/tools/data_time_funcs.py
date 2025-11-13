import locale
import re
from datetime import date, datetime, timedelta
from zoneinfo import ZoneInfo # Usando zoneinfo (Python 3.9+) para zonas horarias
from langchain_core.tools import tool
from chatbot.utilidades.ws_sender import send_tool_message  # Añadir este import

# --- Configuración Inicial ---
# Establecer zona horaria relevante (importante para 'now')
try:
    # Zona horaria para Granada, España
    APP_TZ = ZoneInfo("Europe/Madrid")
except Exception:
    print("Warning: Timezone 'Europe/Madrid' not found. Using system default.")
    # Fallback a UTC o local si es necesario, pero mejor instalar tzdata
    APP_TZ = None

# Establecer locale español para nombres de días/meses
try:
    locale.setlocale(locale.LC_TIME, 'es_ES.UTF-8')
except locale.Error:
    try:
        locale.setlocale(locale.LC_TIME, 'es_ES') # Intenta sin .UTF-8
    except locale.Error:
        print("Warning: Locale 'es_ES' not available. Date parsing/formatting might use English.")

# Mapeo de días de la semana en español a número (Lunes=0 ... Domingo=6)
DIAS_SEMANA_ES = {
    "lunes": 0, "martes": 1, "miércoles": 2, "miercoles": 2, # Alias común
    "jueves": 3, "viernes": 4, "sábado": 5, "sabado": 5, # Alias común
    "domingo": 6
}
# Mapeo inverso (número a nombre) para posible uso futuro o validación
DIAS_SEMANA_NUM_A_ES = {v: k for k, v in DIAS_SEMANA_ES.items() if k not in ["miercoles", "sabado"]} # Sin alias

# --- La Herramienta Combinada (Formato YYYY/MM/DD) ---
@tool
def resolve_date_reference_combined(reference_text: str) -> str | None:
    """
    Convierte una referencia de fecha (relativa o específica) en una fecha concreta YYYY/MM/DD.
    Se asume que las referencias de días de la semana (ej: 'lunes', 'viernes pasado')
    siempre se refieren al pasado más reciente. No maneja referencias futuras como 'próximo lunes'.

    Requiere que datetime, timedelta, date, re, APP_TZ (zona horaria), y
    DIAS_SEMANA_ES (dict) estén definidos en el scope.
    Para parsear nombres de meses ('abril'), el locale del sistema debe ser es_ES.

    Maneja referencias como:
    - 'hoy', 'ayer', 'mañana', 'anteayer'
    - 'hace X días' (ej: 'hace 5 días', 'hace dos días')
    - Nombres de días de la semana PASADOS (ej: 'lunes', 'viernes pasado', 'pasado martes').
    - Fechas específicas (ej: '2024/04/15', '15/04/2024', '15 de abril de 2024').

    Args:
        reference_text (str): El texto que contiene la referencia de fecha.

    Returns:
        str | None: La fecha calculada en formato YYYY/MM/DD, o None si no se pudo interpretar.
    """

    send_tool_message(f"Resolviendo referencia de fecha: {reference_text}")
  
    if not reference_text:
        return None

    # Asegurarse de que APP_TZ está disponible
    if not APP_TZ:
         print("Error: Timezone APP_TZ no está configurada.")
         # Podrías lanzar una excepción o devolver None/error
         # Intentar usar la hora local del sistema como fallback (menos fiable)
         now = datetime.now()
         # return None # O manejar el error como prefieras
    else:
         now = datetime.now(APP_TZ)

    today = now.date()
    # Limpiar y normalizar la entrada
    processed_text = reference_text.lower().strip()

    # Reemplazar números escritos comunes ANTES de procesar
    replacements = {
        "un día": "1 día", "ún día": "1 día",
        "dos días": "2 días", "tres días": "3 días", "cuatro días": "4 días",
        "cinco días": "5 días", "seis días": "6 días", "siete días": "7 días",
    }
    for word, digit in replacements.items():
         processed_text = processed_text.replace(word, digit)

    resolved_date: date | None = None

    # --- 1. Palabras Clave Directas ---
    if "hoy" in processed_text:
        resolved_date = today
    elif "anteayer" in processed_text:
        resolved_date = today - timedelta(days=2)
    elif "ayer" in processed_text:
         resolved_date = today - timedelta(days=1)

    # --- 2. "hace X días" ---
    # (No se necesita "dentro de X días" si no hay futuro)
    if resolved_date is None:
        match_ago = re.match(r"hace (\d+) día", processed_text)
        if match_ago:
            days = int(match_ago.group(1))
            resolved_date = today - timedelta(days=days)

    # --- 3. Nombres de Días de la Semana (SIMPLIFICADO - SIEMPRE PASADO) ---
    if resolved_date is None:
        current_text_to_check = processed_text

        # Limpiar artículos comunes al principio para mejorar detección
        if current_text_to_check.startswith(("el ", "la ")):
             current_text_to_check = current_text_to_check.split(" ", 1)[1]

        target_day_name = current_text_to_check # Nombre del día por defecto
        potential_day = None

        # Extraer día si se usa "pasado" (inicio o fin)
        if current_text_to_check.startswith("pasado ") and ' ' in current_text_to_check:
            potential_day = current_text_to_check.split(" ", 1)[1]
        elif current_text_to_check.endswith(" pasado") and ' ' in current_text_to_check:
             potential_day = current_text_to_check[:-len(" pasado")].strip()

        # Si extrajimos un día potencial y es válido, lo usamos.
        if potential_day and potential_day in DIAS_SEMANA_ES:
            target_day_name = potential_day
        # Si no, target_day_name sigue siendo el texto (quizás sin artículo),
        # que podría ser simplemente el nombre del día (ej: "viernes")

        # --- Calcular la fecha si el nombre del día es válido ---
        if target_day_name in DIAS_SEMANA_ES:
            target_weekday = DIAS_SEMANA_ES[target_day_name]
            today_weekday = today.weekday() # Lunes=0, Domingo=6

            # Siempre calculamos hacia el pasado
            delta_days = (today_weekday - target_weekday + 7) % 7
            if delta_days == 0:
                # Si el día coincide con hoy (ej: 'sábado' un sábado),
                # ir a la semana anterior. El caso 'hoy' ya está cubierto arriba.
                delta_days = 7
            resolved_date = today - timedelta(days=delta_days)


    # --- 4.1. "de X de este mes" ---
    if resolved_date is None:
        match_this_month = re.match(r"(\d{1,2}) de este mes", processed_text)
        if match_this_month:
            day = int(match_this_month.group(1))
            try:
                resolved_date = date(today.year, today.month, day)
            except ValueError:
                # Manejar días inválidos (ej: 30 de febrero)
                resolved_date = None

    # --- 4.2. "de X del mes pasado" ---
    if resolved_date is None:
        match_last_month = re.match(r"(\d{1,2}) del mes pasado", processed_text)
        if match_last_month:
            day = int(match_last_month.group(1))
            last_month = today.month - 1 if today.month > 1 else 12
            year = today.year if today.month > 1 else today.year - 1
            try:
                resolved_date = date(year, last_month, day)
            except ValueError:
                # Manejar días inválidos (ej: 31 de abril)
                resolved_date = None

    # --- 4. Fechas Específicas (Intentar varios formatos) ---
    if resolved_date is None:
        possible_formats = [
            "%Y/%m/%d",         # Formato estándar YYYY/MM/DD
            "%d/%m/%Y",         # DD/MM/YYYY (Común ES)
            "%d/%m/%y",         # DD/MM/YY
            "%d de %B de %Y",   # 15 de abril de 2024 (requiere locale 'es_ES')
            "%d %B %Y",         # 15 abril 2024 (requiere locale 'es_ES')
            "%d de %B del %y",  # 24 de enero del 23 (nuevo formato)
            "%d de %B",         # 15 de abril (asume año actual, requiere locale 'es_ES')
            "%d %B",            # 15 abril (asume año actual, requiere locale 'es_ES')
        ]
        for fmt in possible_formats:
            try:
                parsed_dt = datetime.strptime(processed_text, fmt)
                # Si el formato no incluye año, asumir el año actual
                if "%Y" not in fmt.upper() and "%y" not in fmt.upper():
                    parsed_dt = parsed_dt.replace(year=today.year)
                resolved_date = parsed_dt.date()
                break # Salir del bucle si se encuentra un formato válido
            except ValueError:
                continue # Probar el siguiente formato

    # --- Formateo Final ---
    if resolved_date:
        # Devolver en formato YYYY/MM/DD
        return resolved_date.strftime('%Y/%m/%d')
    else:
        # Si no se pudo resolver, devolver None
        # Considera añadir logging aquí si necesitas depurar
        # print(f"Debug: No se pudo resolver: '{reference_text}' (procesado como: '{processed_text}')")
        return "Busca el dia de hoy en get_today_date_and_time e intenta saber el dia tu con tus conocimientos."
    
    
@tool
def get_current_time() -> str:
    """
    Returns the current time in 'HH:MM' format.
    """
    send_tool_message("Obteniendo hora actual...")
    
    now = datetime.now(APP_TZ)
    return now.strftime("%H:%M")

@tool
def get_today_date_and_time() -> str:
    """
    Returns today's date and the current time in 'YYYY/MM/DD HH:MM' format.
    """
    send_tool_message("Obteniendo fecha y hora actual...")
    now = datetime.now(APP_TZ)
    return now.strftime("%Y/%m/%d %H:%M")

@tool
def get_current_year() -> str:
    """
    Returns the current year in 'YYYY' format.
    """
    send_tool_message("Obteniendo año actual...")
    now = datetime.now(APP_TZ)
    return now.strftime("%Y")

# --- Lista de herramientas para el agente ---
date_time_tools = [
    resolve_date_reference_combined,
    get_current_time,
    get_today_date_and_time,
    get_current_year,  # Añadido a la lista de herramientas
]