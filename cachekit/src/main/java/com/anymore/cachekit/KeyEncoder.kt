package com.anymore.cachekit

/**
 * 编码器，传入的key不能直接存储，默认使用[Md5KeyEncoder]
 * Created by liuyuanmao on 2019/11/11.
 */
interface KeyEncoder {
    fun encode(key: String): String
}