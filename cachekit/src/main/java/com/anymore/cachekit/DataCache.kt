package com.anymore.cachekit

import androidx.annotation.IntRange
import com.anymore.cachekit.converters.Converter
import com.anymore.cachekit.converters.TypeConverterAdapter
import java.io.Serializable

/**
 * 双缓存管理入口类
 * Created by liuyuanmao on 2019/11/9.
 */
@Suppress("UNCHECKED_CAST")
class DataCache private constructor(
    private val mMemoryCache: IMemoryCache,
    private val mDiskCache: IDiskCache,
    private val mTypeConverterAdapter: TypeConverterAdapter
) {
    /**
     * 存储缓存数据至内存
     */
    fun <T : Any> putToMemory(key: String, value: T, expireAt: Long = -1): T {
        mMemoryCache.put(key, LifeLimitedCacheEntry(value, expireAt))
        return value
    }

    /**
     * 从内存缓存取数据
     */
    fun <T : Any> getFromMemory(key: String, default: T? = null): T? {
        val lce = mMemoryCache.get<LifeLimitedCacheEntry>(key) ?: return default
        val expireAt = lce.expireAt
        return when {
            expireAt < 0 -> lce.data as T//永不过期
            expireAt < System.currentTimeMillis() -> {//已经过期
                removeFromMemory(key)
                default
            }
            else -> lce.data as T
        }
    }

    /**
     * 从内存缓存中移除数据
     */
    fun removeFromMemory(key: String): Boolean = mMemoryCache.remove<Any>(key) != null

    /**
     * 清除内存缓存
     */
    fun clearMemoryCache() = mMemoryCache.clear()

    /**
     * 磁盘缓存存储
     */
    fun <T : Any> putToDisk(key: String, value: T, expireAt: Long = -1): T {
        val lce: LifeLimitedCacheEntry
        //如果被缓存的类是Serializable即直接缓存进文件
        lce = if (value is Serializable) {
            LifeLimitedCacheEntry(value, expireAt)
        } else {
            //否则尝试使用降级缓存
            val cacheEntry = mTypeConverterAdapter.downgrade(value)
            LifeLimitedCacheEntry(cacheEntry, expireAt)
        }
        mDiskCache.put(key, lce)
        return value
    }

    /**
     * 磁盘缓存读取
     */
    fun <T : Any> getFromDisk(key: String, default: T? = null): T? {
        //没有读取到数据，返回默认
        val lce: LifeLimitedCacheEntry = mDiskCache.get(key) ?: return default
        //如果缓存数据的类型是DiskCacheEntry，说明是通过转换器升降级的，需要进行还原
        if (lce.data is DiskCacheEntry) {
            val originData = mTypeConverterAdapter.upgrade(lce.data.byteArray, lce.data.clazz) as T
            return when {
                lce.expireAt < 0 -> originData
                lce.expireAt < System.currentTimeMillis() -> {
                    removeFromDisk(key)
                    default
                }
                else -> originData
            }
        } else {
            //否则直接转型返回
            lce.data as T
            return when {
                lce.expireAt < 0 -> lce.data//永不过期
                lce.expireAt < System.currentTimeMillis() -> {//已经过期
                    removeFromDisk(key)
                    default
                }
                else -> lce.data//在有效期内
            }
        }
    }

    /**
     * 从磁盘缓存中移除
     */
    fun removeFromDisk(key: String): Boolean = mDiskCache.remove(key)

    /**
     * 清空磁盘缓存
     */
    fun clearDiskCache() = mDiskCache.clear()

    /**
     * 双缓存
     */
    fun <T : Any> put(key: String, value: T, expireAt: Long = -1): T {
        putToMemory(key, value, expireAt)
        putToDisk(key, value, expireAt)
        return value
    }

    /**
     * 内存缓存优先读取
     * 如果不存在再读取磁盘缓存数据
     * 如果不存在返回默认值
     */
    fun <T : Any> get(key: String, default: T? = null): T? {
        val memoryCacheValue = getFromMemory(key, null) as T?
        if (memoryCacheValue != null) {
            return memoryCacheValue
        }
        val diskCacheValue = getFromDisk<T>(key, null)
        if (diskCacheValue != null) {
            return diskCacheValue
        }
        return default
    }

    fun removeFromCache(key: String) {
        removeFromMemory(key)
        removeFromDisk(key)
    }

    fun clearCache() {
        clearMemoryCache()
        clearDiskCache()
    }


    class Builder {
        private val typeConverterAdapter = TypeConverterAdapter()
        private var memoryCacheSize: Int = 128
        private var diskCacheSize: Long = 64 * 1024 * 1024
        private var diskCacheDir: String = ""
        private var diskCacheVersion = 1
        private var keyEncoder: KeyEncoder = Md5KeyEncoder()

        /**
         * 为一些没有实现[Serializable]接口的对象，但是又想使用磁盘缓存所做的降级转换器
         * 可以调用此方法进行注册自定义的转换器
         */
        fun register(converter: Converter<*>): Builder = apply {
            typeConverterAdapter.register(converter)
        }

        fun setMemoryCacheSize(size: Int): Builder = apply {
            memoryCacheSize = size
        }

        fun setDiskCacheSize(size: Long): Builder = apply {
            diskCacheSize = size
        }

        /**
         * 设置磁盘缓存的文件目录，默认不指定目录，如果不设置不会影响内存缓存相关方法的调用
         * 但是在使用磁盘缓存或者双缓存时候会抛出异常
         */
        fun setDiskCacheDir(dir: String): Builder = apply {
            diskCacheDir = dir
        }

        fun setDiskCacheVersion(@IntRange(from = 1) version: Int): Builder = apply {
            diskCacheVersion = version
        }

        /**
         * 设置缓存key的编码器，不直接使用明文key作为主键，默认使用的是[Md5KeyEncoder]
         *  注意这个[KeyEncoder]只对于磁盘缓存的时候的key进行编码加密，对于内存缓存时候
         *  感觉没有必要，所以没有进行加密
         */
        fun setDiskCacheKeyEncoder(encoder: KeyEncoder): Builder = apply {
            keyEncoder = encoder
        }

        fun build(): DataCache {
            val diskCache: IDiskCache =
                LruDiskCache(diskCacheDir, diskCacheVersion, diskCacheSize).also {
                    it.keyEncoder = keyEncoder
                }
            val memoryCache: IMemoryCache = LruMemoryCache(memoryCacheSize)
            return DataCache(memoryCache, diskCache, typeConverterAdapter)
        }
    }


}