from typing import Any, Callable, Optional, Type

from langchain.callbacks.manager import AsyncCallbackManagerForToolRun, CallbackManagerForToolRun
from langchain.schema import BaseModel, Field
from langchain.tools import BaseTool


from typing import Optional, Type, Any
from .base import ejecutar_sql
import pandas as pd


# You can provide a custom args schema to add descriptions or custom validation

class SearchSchema(BaseModel):
    query: str = Field(description="should be a SQL query")
   
class ExecSQLTools(BaseTool):
    name = "exec_sql"
    description = """Util para cuando necesites ejecutar consultas SQL. 
                   Consulta suportadas: (SELECT, INSERT, UPDATE)
                   No busca informacion de las tablas"""
    
    args_schema: Type[SearchSchema] = SearchSchema
   
    
    def _run(self, query: str,  run_manager: Optional[CallbackManagerForToolRun] = None) -> str:
        rs = ejecutar_sql(query)
        return rs

    async def _arun(self, query: str, run_manager: Optional[AsyncCallbackManagerForToolRun] = None) -> str:
        return self._run(query, run_manager)
 
class SearchInfoDBTools(BaseTool):
    name = "search_info_db"
    description = """Util para buscar informacion y ejmplos para la base de datos. 
                  "Parametros de entrada para esta herramienta palabras clave."""
    
    args_schema: Type[SearchSchema] = SearchSchema
    rt : Type[Any] = None
   

    def set_embbeding(self, rt: Type[Any]) -> Type['SearchInfoDBTools']:
        self.rt = rt
        return self


    def _run(self, query: str,  run_manager: Optional[CallbackManagerForToolRun] = None) -> str:
        return self.rt.format(text=query)

    async def _arun(self, query: str, run_manager: Optional[AsyncCallbackManagerForToolRun] = None) -> str:
        return self._run(query, run_manager)
 