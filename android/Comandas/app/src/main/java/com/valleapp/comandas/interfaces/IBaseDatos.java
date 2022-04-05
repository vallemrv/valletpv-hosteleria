package com.valleapp.valletpv.interfaces;

import org.json.JSONArray;

public interface IBaseDatos {
    public void resetFlag(int id);
    public JSONArray filter(String cWhere);
    public void rellenarTabla(JSONArray objs);
}
