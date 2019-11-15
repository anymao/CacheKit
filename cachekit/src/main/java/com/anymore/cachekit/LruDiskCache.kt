package com.anymore.cachekit

import com.jakewharton.disklrucache.DiskLruCache
import java.io.*

/**
 * Created by liuyuanmao on 2019/11/9.
 */
@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
class LruDiskCache(directory: String, appVersion: Int, maxSize: Long) : IDiskCache {

    private val mCache: DiskLruCache by lazy {
        require(directory.isNotEmpty()) {
            "you must set the disk cache directory before you use LruDiskCache!"
        }
        DiskLruCache.open(File(directory), appVersion, 1, maxSize)
    }

    var keyEncoder: KeyEncoder = Md5KeyEncoder()


    override fun <T : Serializable> put(key: String, value: T): T {
        val encodedKey = keyEncoder.encode(key)
        val editor = mCache.edit(encodedKey)
        val os = editor.newOutputStream(0)
        val oos = ObjectOutputStream(os)
        try {
            oos.writeObject(value)
            oos.flush()
            editor.commit()
        } catch (e: Exception) {
            //ignored
        } finally {
            oos.closeQuietly()
        }
        return value
    }

    override fun <T : Serializable> get(key: String, default: T?): T? {
        val encodedKey = keyEncoder.encode(key)
        val snapshot: DiskLruCache.Snapshot? = mCache.get(encodedKey)
        return snapshot?.let {
            val inputStream = it.getInputStream(0)
            val ois = ObjectInputStream(inputStream)
            return@let try {
                ois.readObject() as T?
            } catch (e: Exception) {
                null
            } finally {
                ois.closeQuietly()
            }
        } ?: default
    }

    override fun isContains(key: String): Boolean {
        val snapshot: DiskLruCache.Snapshot? = mCache.get(keyEncoder.encode(key))
        return snapshot != null
    }

    override fun remove(key: String): Boolean {
        return try {
            mCache.remove(keyEncoder.encode(key))
        } catch (e: Exception) {
            false
        }
    }

    override fun clear() {
        mCache.delete()
    }



    companion object {
        private fun Closeable.closeQuietly() {
            try {
                close()
            } catch (e: Exception) {
                //ignored
            }
        }
    }
}