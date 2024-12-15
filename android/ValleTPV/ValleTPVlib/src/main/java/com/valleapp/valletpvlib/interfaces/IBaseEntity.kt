package com.valleapp.valletpvlib.interfaces

import org.json.JSONObject

interface IBaseEntity<T> {
    fun emtityFromJson(obj: JSONObject): T
    fun jsonFromEmtity(entity: T): JSONObject
}