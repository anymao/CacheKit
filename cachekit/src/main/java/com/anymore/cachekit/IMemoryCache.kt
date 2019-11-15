package com.anymore.cachekit

/**
 * 内存缓存接口
 * Created by liuyuanmao on 2019/11/9.
 */
interface IMemoryCache {
    /**
     * 存储[key]对应的值到内存缓存
     */
    fun <T: Any> put(key: String, value: T): T

    /**
     * 从内存缓存中取[key]对应的值，如果没找到就返回[default]
     */
    fun <T: Any> get(key: String, default: T? = null): T?

    /**
     * 内存缓存中是否存在[key]对应的缓存
     */
    fun isContains(key: String): Boolean

    /**
     * 从缓存中移除[key]对应的缓存值
     */
    fun <T: Any> remove(key: String): T?

    /**
     * 清空所有缓存
     */
    fun clear()
}