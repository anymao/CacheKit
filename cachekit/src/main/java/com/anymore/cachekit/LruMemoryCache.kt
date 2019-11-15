package com.anymore.cachekit

import androidx.collection.LruCache

/**
 *基于[LruCache]的磁盘缓存实现
 * Created by liuyuanmao on 2019/11/9.
 */
@Suppress("UNCHECKED_CAST")
class LruMemoryCache(cacheSize: Int = 128) : IMemoryCache {

    private val mCache: LruCache<String, Any> = LruCache(cacheSize)


    override fun <T : Any> put(key: String, value: T): T {
        mCache.put(key, value)
        return value
    }

    override fun <T : Any> get(key: String, default: T?): T? {
        val value: T? = mCache.get(key) as T?
        return value ?: default
    }

    override fun isContains(key: String): Boolean {
        return mCache.get(key) != null
    }

    override fun <T : Any> remove(key: String): T? {
        return mCache.remove(key) as T?
    }

    override fun clear() {
        mCache.evictAll()
    }
}