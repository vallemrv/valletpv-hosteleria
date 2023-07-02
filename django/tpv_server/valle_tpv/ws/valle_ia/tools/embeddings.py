from langchain.prompts.example_selector import SemanticSimilarityExampleSelector
from langchain.vectorstores import FAISS
from langchain.embeddings import OpenAIEmbeddings
from langchain.prompts import FewShotPromptTemplate, PromptTemplate
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.document_loaders import TextLoader
import os
import json


def crear_embeddings_docs(file_db: str):
    
    loader = TextLoader(file_db)
    documents = loader.load()

    openai_api_key = os.environ.get("API_KEY")
    #cargamos los datos de ejmplos sql

    # Get your splitter ready
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=50)

    # Split your docs into texts
    docs = text_splitter.split_documents(documents)
    
    # Get embedding engine ready
    embeddings = OpenAIEmbeddings(openai_api_key=openai_api_key)

    # Embedd your texts
    db = FAISS.from_documents(docs, embeddings)
    
    # Init your retriever. Asking for just 1 document back
    return db.as_retriever()

def crear_embeddings_ex(file_db: str, num_ejemplos=2):

    openai_api_key = os.environ.get("API_KEY")
    #cargamos los datos de ejmplos sql

    result = []
    with open(file_db, "r") as f:
        result = json.loads(f.read())

    example_prompt = PromptTemplate(
        input_variables=["input", "output"],
        template="{input}\nOutput: {output}",
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
        prefix="Ejemplos de preguntas - respuesta que necesito.",
        suffix="Input: {text}",
        
        # What inputs your prompt will receive
        input_variables=["text"],
    )