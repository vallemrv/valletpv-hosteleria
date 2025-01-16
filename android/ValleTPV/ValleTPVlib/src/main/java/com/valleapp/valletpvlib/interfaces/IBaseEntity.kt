package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IBaseEntity<T>{
    fun entityFromJson(obj: JSONObject): T
    fun jsonFromEntity(entity: T): JSONObject
}