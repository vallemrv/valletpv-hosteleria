package com.valleapp.valletpvlib.Interfaces;

import org.json.JSONObject;

public interface IBaseSocket {
    void rm(JSONObject o);

    void insert(JSONObject o);

    void update(JSONObject o);
}
