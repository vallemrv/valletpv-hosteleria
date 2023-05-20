import inflect

def find_models_in_phrase(phrase):
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

    # Inicializar el objeto de inflect
    p = inflect.engine()
    
    # Crear un diccionario de todas las formas posibles de los modelos (singular y plural)
    all_models = {}
    for model in models:
        all_models[model] = model  # Añade la versión en plural
        singular_model = p.singular_noun(model)  # Añade la versión en singular
        if singular_model:  # La función singular_noun devuelve False si no puede singularizar
            all_models[singular_model] = model  # Asigna la versión en singular al plural original
    
    # Convertir la frase a minúsculas
    phrase = phrase.lower()
    
    # Inicializar la lista de modelos encontrados
    found_models = []
    
    # Verificar si cada modelo está en la frase
    for model, original_model in all_models.items():
        if model in phrase and original_model not in found_models:
            found_models.append(original_model)
            
    # Si no se encontró ningún modelo, devolver "nono"
    if not found_models:
        return "nono"
    
    # Devolver la lista de modelos encontrados
    return found_models

# Prueba la función
print(find_models_in_phrase("La lista de camareros y gestiones_syncs son importantes."))
print(find_models_in_phrase("Dime el nombre del primer camarero."))
