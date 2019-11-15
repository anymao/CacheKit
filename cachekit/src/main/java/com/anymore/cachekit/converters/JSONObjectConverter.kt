package com.anymore.cachekit.converters

import org.json.JSONObject

/**
 * [JSONObject]缓存转换器
 * Created by liuyuanmao on 2019/11/14.
 */
object JSONObjectConverter : Converter<JSONObject> {

    override fun downgrade(value: JSONObject): ByteArray =
        value.toString().toByteArray(Charsets.UTF_8)

    override fun upgrade(byteArray: ByteArray): JSONObject = JSONObject(String(byteArray))

    override fun canConvert(clazz: Class<*>): Boolean = clazz == JSONObject::class.java
}