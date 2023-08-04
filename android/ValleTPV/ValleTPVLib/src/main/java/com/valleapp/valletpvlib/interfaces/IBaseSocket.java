package com.valleapp.valletpvlib.interfaces;

import org.json.JSONObject;

public interface IBaseSocket {
    public void rm(JSONObject o);
    public void insert(JSONObject o);
    public void update(JSONObject o);
}
