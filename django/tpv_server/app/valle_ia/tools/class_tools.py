from typing import Any, Optional, Type

from langchain.callbacks.manager import AsyncCallbackManagerForToolRun, CallbackManagerForToolRun
from langchain.schema import BaseModel, Field
from langchain.tools import BaseTool
from typing import Optional, Type, Callable, Any
from .base import ejecutar_accion, ejecutar_select
import json

# You can provide a custom args schema to add descriptions or custom validation

class SearchSchema(BaseModel):
    query: str = Field(description="should be a SQL query")
   

class ExecSQLTools(BaseTool):
    name = "exec_sql"
    description = "Useful when you need Execute a UPDATE, INSERT SQL query"
    args_schema: Type[SearchSchema] = SearchSchema
    controller : Type[Any] = None
   
    
    def set_ws_callback(self, controller: Type[Any]) -> Type[Any]:
        self.controller = controller
        return self
   
    def _run(self, query: str,  run_manager: Optional[CallbackManagerForToolRun] = None) -> str:
        rs = ejecutar_accion(query)
        if self.controller:
            self.controller.enviar_mensaje_sync(json.dumps(rs))
        return rs

    async def _arun(self, query: str, run_manager: Optional[AsyncCallbackManagerForToolRun] = None) -> str:
        return self._run(query, run_manager)
    
class QuerySQLTools(BaseTool):
    name = "query_sql"
    description = "Useful when you need Execute a SELECT SQL query"
    args_schema: Type[SearchSchema] = SearchSchema
    controller: Type[Any] = None
   

    def set_ws_callback(self, controller: Type[Any]) -> Type[Any]:
        self.controller = controller
        return self

    def _run(self, query: str,  run_manager: Optional[CallbackManagerForToolRun] = None) -> str:
        rs = ejecutar_select(query)
        if self.controller:
            self.controller.enviar_mensaje_sync(json.dumps(rs), tipo="tabla")
        return rs

    async def _arun(self, query: str, run_manager: Optional[AsyncCallbackManagerForToolRun] = None) -> str:
        return self._run(query, run_manager)
    