import inflect
import json
import os

# Inicializa el objeto de inflect
p = inflect.engine()

# Define la lista de modelos
models = [
    "camareros", "gestion_iconchoices", "mesas", "gestion_permisoschoices",
    "receptores", "secciones", "gestion_sync", "teclas", "zonas",
    "ticketlineas", "teclaseccion", "teclascom", "sugerencias", "subteclas",
    "servidos", "gestion_peticionesautoria", "pedidos", "mesaszona",
    "mesasabiertas", "lineaspedido", "gestion_lineascompuestas", "historialnulos",
    "gastos", "familias", "efectivo", "composicionteclas", "arqueocaja",
    "cierrecaja", "gestion_historialmensajes", "infmesa", "ticket"
]

# Crea un diccionario de todas las formas posibles de los modelos (singular y plural)
all_models = {}
for model in models:
    all_models[model] = model  # Añade la versión en plural
    singular_model = p.singular_noun(model)  # Añade la versión en singular
    if singular_model:  # La función singular_noun devuelve False si no puede singularizar
        all_models[singular_model] = model  # Asigna la versión en singular al plural original

# Carga los sinónimos desde el archivo si existe
synonyms_file = "synonyms.json"
if os.path.exists(synonyms_file):
    with open(synonyms_file, "r") as f:
        synonyms = json.load(f)
else:
    synonyms = {}

def save_synonyms():
    # Guarda los sinónimos en el archivo
    with open(synonyms_file, "w") as f:
        json.dump(synonyms, f)

def add_synonym(synonym, model):
    # Añade un sinónimo a un modelo en el diccionario de sinónimos
    synonyms[synonym] = model
    save_synonyms()

def find_models_in_phrase(phrase):
    # Convertir la frase a minúsculas
    phrase = phrase.lower()
    
    # Inicializar la lista de modelos encontrados
    found_models = []
    
    # Verificar si cada modelo está en la frase
    for model, original_model in all_models.items():
        if model in phrase and original_model not in found_models:
            found_models.append(original_model)
            
    # Si no se encontró ningún modelo, buscar en los sinónimos
    if not found_models:
        for synonym, model in synonyms.items():
            if synonym in phrase and model not in found_models:
                found_models.append(model)
    
    # Devolver la lista de modelos encontrados
    return found_models

estructura_base =   {
    "camareros": (
     ["ID(integer)", "Nombre(varchar)", "Apellidos(varchar)", "Email(varchar)", "Pass(varchar)", "Activo(integer)", "Autorizado(integer)", "Permisos(varchar)"]
        , '''Pase = Autorizado.
             Borrar camareros = Activo = 0.
             Activar camareros o camarero = Activo = 1.
             En las consultas select mostrar solo las columnas (Nombre, Apellidos y Autorizado)
             Siempre muestra solo los activos, a no ser que lo especifique implicitamente.'''),
    "gestion_iconchoices": ["id(integer)"],
    "mesas": ["ID(integer)", "Nombre(varchar)", "Orden(integer)"],
    "gestion_permisoschoices": ["id(integer)"],
    "receptores": ["ID(integer)", "Nombre(varchar)", "nomImp(varchar)", "Activo(bool)", "Descripcion(varchar)"],
    "secciones": ["ID(integer)", "Nombre(varchar)", "RGB(varchar)", "Orden(integer)"],
    "gestion_sync": ["id(integer)", "nombre(varchar)", "last(varchar)"],
    "teclas": ["ID(integer)", "Nombre(varchar)", "P1(decimal)", "P2(decimal)", "Orden(integer)", "Tag(varchar)", "TTF(varchar)", "Descripcion_r(varchar)", "Descripcion_t(varchar)", "tipo(varchar)", "IDFamilia(integer, foreing_key(familias))"],
    "zonas": ["ID(integer)", "Nombre(varchar)", "Tarifa(integer)", "RGB(varchar)"],
    "ticketlineas": ["ID(integer)", "IDLinea(integer, foreing_key(lineaspedido))", "IDTicket(integer, foreing_key(ticket))"],
    "teclaseccion": ["ID(integer)", "IDSeccion(integer, foreing_key(secciones))", "IDTecla(integer, foreing_key(teclas))"],
    "teclascom": ["ID(integer)", "Orden(integer)", "IDSeccion(integer, foreing_key(secciones_com))", "IDTecla(integer, foreing_key(teclas))"],
    "sugerencias": ["ID(integer)", "Sugerencia(varchar)", "IDTecla(integer, foreing_key(teclas))"],
    "subteclas": ["ID(integer)", "nombre(varchar)", "incremento(decimal)", "Descripcion_r(varchar)", "Descripcion_t(varchar)", "Orden(integer)", "IDTecla(integer, foreing_key(teclas))"],
    "servidos": ["ID(integer)", "IDLinea(integer, foreing_key(lineaspedido))"],
    "gestion_peticionesautoria": ["id(integer)", "accion(varchar)", "instrucciones(varchar)", "idautorizado_id(integer, foreing_key(camareros))"],
    "pedidos": ["ID(integer)", "Hora(varchar)", "IDCam(integer)", "uid_device(varchar)", "UID(varchar, foreing_key(infmesa))"],
    "mesaszona": ["ID(integer)", "IDMesa(integer, foreing_key(mesas))", "IDZona(integer, foreing_key(zonas))"],
    "mesasabiertas": ["ID(integer)", "UID(varchar, foreing_key(infmesa))", "IDMesa(integer, foreing_key(mesas))"],
    "lineaspedido": ["ID(integer)", "IDArt(integer)", "Estado(varchar)", "Precio(decimal)", "Descripcion(varchar)", "es_compuesta(bool)", "cantidad(integer)", "Descripcion_t(varchar)", "UID(varchar, foreing_key(infmesa))", "IDPedido(integer, foreing_key(pedidos))", "tecla_id(integer, foreing_key(teclas))"],
    "gestion_lineascompuestas": ["id(integer)", "linea_compuesta(integer)", "composicion_id(integer, foreing_key(composicionteclas))", "linea_principal_id(integer, foreing_key(lineaspedido))"],
    "historialnulos": ["ID(integer)", "Hora(varchar)", "Motivo(varchar)", "IDCam(integer, foreing_key(camareros))", "IDLPedido(integer, foreing_key(lineaspedido))"],
    "gastos": ["ID(integer)", "Descripcion(varchar)", "Importe(decimal)", "IDArqueo(integer, foreing_key(arqueocaja))"],
    "familias": ["ID(integer)", "Nombre(varchar)", "Tipo(varchar)", "NumTapas(integer)", "IDReceptor(integer, foreing_key(receptores))"],
    "efectivo": ["ID(integer)", "Can(integer)", "Moneda(decimal)", "IDArqueo(integer, foreing_key(arqueocaja))"],
    "composicionteclas": ["ID(integer)", "composicion(varchar)", "cantidad(integer)", "IDTecla(integer, foreing_key(teclas))"],
    "arqueocaja": ["ID(integer)", "Cambio(real)", "Descuadre(real)", "IDCierre(integer, foreing_key(cierrecaja))"],
    "cierrecaja": ["ID(integer)", "TicketCom(integer)", "TicketFinal(integer)", "Hora(varchar)", "Fecha(date)"],
    "gestion_historialmensajes": ["id(integer)", "mensaje(varchar)", "hora(varchar)", "camarero_id(integer, foreing_key(camareros))", "receptor_id(integer, foreing_key(receptores))", "fecha(date)"],
    "infmesa": ["UID(varchar)", "Hora(varchar)", "NumCopias(integer)", "IDCam(integer, foreing_key(camareros))", "Fecha(date)"],
    "ticket": ["ID(integer)", "IDCam(integer)", "Hora(varchar)", "Entrega(decimal)", "UID(varchar)", "Mesa(varchar)", "url_factura(varchar)", "Fecha(date)"]
}