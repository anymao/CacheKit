package com.anymore.cachekit

import java.io.Serializable

/**
 * 磁盘缓存接口，磁盘缓存只能缓存[Serializable]对象
 * Created by liuyuanmao on 2019/11/9.
 */
interface IDiskCache {

    /**
     * 存储[key]对应的[value]到磁盘，并且返回[value]
     */
    fun <T : Serializable> put(key: String, value: T): T

    /**
     * 从磁盘缓存中取[key]对应的值，如果没找到就返回[default]
     */
    fun <T : Serializable> get(key: String, default: T? = null): T?

    /**
     * 磁盘缓存中是否存在[key]对应的缓存
     */
    fun isContains(key: String): Boolean

    /**
     * 从缓存中移除[key]对应的缓存值
     */
    fun remove(key: String): Boolean

    /**
     * 清空所有缓存
     */
    fun clear()
}