package com.anymore.cachekit

import java.io.Serializable

/**
 * 生命有限的缓存实体
 * [data]是真正的实体
 * [expireAt]过期时间
 * Created by liuyuanmao on 2019/11/9.
 */
internal data class LifeLimitedCacheEntry(val data: Any, val expireAt: Long = -1L) : Serializable