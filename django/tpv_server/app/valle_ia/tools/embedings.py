from langchain.prompts.example_selector import SemanticSimilarityExampleSelector
from langchain.vectorstores import FAISS
from langchain.embeddings import OpenAIEmbeddings
from langchain.prompts import FewShotPromptTemplate, PromptTemplate


import os
import json


def crear_emedings(file_db: str, num_ejemplos=2):

    openai_api_key = os.environ.get("API_KEY")
    #cargamos los datos de ejmplos sql

    result = []
    with open(file_db, "r") as f:
        result = json.loads(f.read())

    example_prompt = PromptTemplate(
        input_variables=["input", "output"],
        template="Example Input: {input}\nExample Output: {output}",
    )

    # SemanticSimilarityExampleSelector will select examples that are similar to your input by semantic meaning

    example_selector = SemanticSimilarityExampleSelector.from_examples(
        # This is the list of examples available to select from.
        result, 
        
        # This is the embedding class used to produce embeddings which are used to measure semantic similarity.
        OpenAIEmbeddings(openai_api_key=openai_api_key), 
        
        # This is the VectorStore class that is used to store the embeddings and do a similarity search over.
        FAISS, 
        
        # This is the number of examples to produce.
        k=num_ejemplos
    )

    return  FewShotPromptTemplate(
        # The object that will help select examples
        example_selector=example_selector,
        
        # Your prompt
        example_prompt=example_prompt,
        
        # Customizations that will be added to the top and bottom of your prompt
        prefix="Traduce el texto a una consulta sql. Segun los ejemplos que acontinuacion se lista.",
        suffix="Input: {text}\nOutput:",
        
        # What inputs your prompt will receive
        input_variables=["text"],
    )