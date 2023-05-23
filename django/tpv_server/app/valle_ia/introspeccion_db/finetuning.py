import openai
import  os
openai.api_key = os.environ.get("API_KEY")


#modelo = "davinci:ft-personal-2023-05-23-01-45-08"
#modelo = "gpt-3.5-turbo"
#modelo = "gpt-3.5-turbo-0301"
modelo = "text-davinci-002"
response = openai.Completion.create(
  model=modelo,
  prompt="Introduce aquí tu texto inicial",
  max_tokens=100
)

print(response.choices[0].text.strip())