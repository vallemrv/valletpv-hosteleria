package com.valleapp.valletpvlib.tools

import org.json.JSONObject


typealias Params = MutableMap<String, Any>
class ParamsHelper {
    companion object {
        fun createParams(p: JSONObject): Params {
            var aux = mutableMapOf<String, Any>()
            for(key in p.keys()) {
                aux[key] = p[key]
            }
            return aux
        }
    }
}