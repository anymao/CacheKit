package com.anymore.cachekit

/**
 * MD5编码器,[LruDiskCache]使用的key默认编码器
 * Created by liuyuanmao on 2019/11/11.
 */
class Md5KeyEncoder : KeyEncoder {
    override fun encode(key: String): String = md5(key)
}