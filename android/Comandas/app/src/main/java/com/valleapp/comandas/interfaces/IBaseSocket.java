package com.valleapp.comandas.interfaces;

import org.json.JSONObject;

public interface IBaseSocket {
    public void rm(JSONObject o);
    public void insert(JSONObject o);
    public void update(JSONObject o);
}
