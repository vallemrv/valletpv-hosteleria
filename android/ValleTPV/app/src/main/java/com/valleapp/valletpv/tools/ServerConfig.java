package com.valleapp.valletpv.tools;

import android.content.ContentValues;

public class BuildParams {
    String code;
    String UID;
    public BuildParams(String code, String UID){
        this.code = code;
        this.UID = UID;
    }

    public ContentValues getParams(){
        ContentValues params = new ContentValues();
        params.put("code", code);
        params.put("UID", UID);
        return params;
    }
}
