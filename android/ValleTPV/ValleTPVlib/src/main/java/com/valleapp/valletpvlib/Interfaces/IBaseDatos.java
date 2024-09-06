package com.valleapp.valletpvlib.Interfaces;

import org.json.JSONArray;

public interface IBaseDatos {
    JSONArray filter(String cWhere);

    void rellenarTabla(JSONArray objs);

    void inicializar();
}
