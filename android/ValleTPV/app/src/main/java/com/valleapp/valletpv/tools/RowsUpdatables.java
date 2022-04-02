package com.valleapp.valletpv.tools;

import org.json.JSONArray;
import org.json.JSONObject;

public class RowsUpdatables {

    public String getTb_name() {
        return tb_name;
    }

    String tb_name;

    public JSONArray getRows() {
        return rows;
    }

    JSONArray rows = new JSONArray();

    public RowsUpdatables(String tb_name, JSONObject row){
        this.tb_name = tb_name;
        this.rows.put(row);
    }


    public void addRow(JSONObject row){
        this.rows.put(row);
    }


}
