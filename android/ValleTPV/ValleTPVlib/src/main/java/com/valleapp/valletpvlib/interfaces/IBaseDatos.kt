package com.valleapp.valletpvlib.Interfaces;

import org.json.JSONArray;

public interface IBaseDatos {
    suspend fun JSONArray filter(String cWhere);

    suspend fun rellenarTabla(JSONArray objs);

    void inicializar();
}
