from typing import Any, Callable, Optional, Type

from langchain.callbacks.manager import AsyncCallbackManagerForToolRun, CallbackManagerForToolRun
from langchain.schema import BaseModel, Field
from langchain.tools import BaseTool
from typing import Optional, Type, Any
from .base import ejecutar_sql
import json

# You can provide a custom args schema to add descriptions or custom validation

class SearchSchema(BaseModel):
    query: str = Field(description="should be a SQL query")
   
class ExecSQLTools(BaseTool):
    name = "exec_sql"
    description = "Util para cuando necesites ejecutar consultas SQL "
    args_schema: Type[SearchSchema] = SearchSchema
    controller : Type[Any] = None
   
    
    def set_controller(self, controller: Type[Any]) -> Type[Any]:
        self.controller = controller
        return self
   
    def _run(self, query: str,  run_manager: Optional[CallbackManagerForToolRun] = None) -> str:
        rs = ejecutar_sql(query)
        '''
        if self.controller:
            self.controller.enviar_mensaje_sync(json.dumps(rs))
            '''
        return rs

    async def _arun(self, query: str, run_manager: Optional[AsyncCallbackManagerForToolRun] = None) -> str:
        return self._run(query, run_manager)
 