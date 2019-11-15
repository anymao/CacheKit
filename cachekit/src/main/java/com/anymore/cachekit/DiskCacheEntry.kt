package com.anymore.cachekit

import java.io.Serializable

/**
 * 磁盘缓存的降级策略，将原始数据转换为[ByteArray]并且保存原始类的信息[Class]
 * 这两部分都是[Serializable]的实现类是可以存储的，对于像[Parcelable]或者[Bitmap]
 * 也可以进行磁盘存储
 * Created by liuyuanmao on 2019/11/9.
 */
internal data class DiskCacheEntry(val byteArray: ByteArray, val clazz: Class<*>) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DiskCacheEntry
        if (!byteArray.contentEquals(other.byteArray)) return false
        if (clazz != other.clazz) return false
        return true
    }

    override fun hashCode(): Int {
        var result = byteArray.contentHashCode()
        result = 31 * result + clazz.hashCode()
        return result
    }
}