package com.anymore.cachekit.converters

import org.json.JSONArray

/**
 * [JSONArray]的缓存支持
 * Created by liuyuanmao on 2019/11/14.
 */
object JSONArrayConverter : Converter<JSONArray> {
    override fun downgrade(value: JSONArray): ByteArray =
        value.toString().toByteArray(Charsets.UTF_8)

    override fun upgrade(byteArray: ByteArray): JSONArray = JSONArray(String(byteArray))

    override fun canConvert(clazz: Class<*>): Boolean = clazz == JSONArray::class.java
}