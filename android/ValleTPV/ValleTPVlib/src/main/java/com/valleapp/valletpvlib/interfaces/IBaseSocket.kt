package com.valleapp.valletpvlib.interfaces;

import org.json.JSONObject;

public interface IBaseSocket {
    void rm(JSONObject o);

    void insert(JSONObject o);

    void update(JSONObject o);
}
