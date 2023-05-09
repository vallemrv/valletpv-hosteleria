import re




def parse_gpt_response_text(action_string):
    print(action_string)
    modelo = re.search(r'Modelo: (\w+)', action_string).group(1)
    accion = re.search(r'Acción: (\w+)', action_string).group(1)
    columnas = re.findall(r'Columnas: (.*?)\n', action_string)[0].split(', ')
    valores = re.findall(r'Valores: (.*?)\n', action_string)[0]
    valores = re.findall(r"'(.*?)'", valores)
    parametros = re.search(r'Parámetros requeridos: (.+)', action_string).group(1)

   
    response_obj = {
        "modelo": modelo,
        "accion": accion,
        "parametros": parametros,
        "valores": valores,
        "columnas": columnas,
    }
    
    return response_obj

def preguntar_gpt(openai, messages):

    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=messages,
        max_tokens=150,
        n=1,
        stop=None,
        temperature=0,
    )

    return response.choices[0].message.content