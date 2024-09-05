package com.valleapp.valletpv.interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IBaseDatos {
    public JSONArray filter(String cWhere);
    public void rellenarTabla(JSONArray objs);
    public void inicializar();

}
