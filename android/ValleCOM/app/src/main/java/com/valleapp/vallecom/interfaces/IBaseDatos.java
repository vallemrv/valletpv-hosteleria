package com.valleapp.vallecom.interfaces;

import org.json.JSONArray;

public interface IBaseDatos {
    public JSONArray filter(String cWhere);
    public void rellenarTabla(JSONArray objs);
    public void inicializar();

}
