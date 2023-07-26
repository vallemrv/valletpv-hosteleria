package com.valleapp.valletpv.tareas;

import org.json.JSONObject;

public interface IController {
    void sync_device(String[] devices, long timeout);
    void updateTables(JSONObject o);
}

