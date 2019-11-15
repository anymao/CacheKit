package com.anymore.cachekit.converters

import com.anymore.cachekit.DiskCacheEntry

/**
 * Created by liuyuanmao on 2019/11/11.
 */
@Suppress("UNCHECKED_CAST")
internal class TypeConverterAdapter {

    private val mConverterRegistry: ConverterRegistry = ConverterRegistry()


    fun register(converter: Converter<*>) {
        mConverterRegistry.register(converter)
    }


    fun <T : Any> downgrade(value: T): DiskCacheEntry {
        val clazz = value::class.java
        val converter: Converter<T> = findDowngradeConverter(clazz) as Converter<T>
        val byteArray = converter.downgrade(value)
        return DiskCacheEntry(byteArray, clazz)
    }

    fun <T> upgrade(byteArray: ByteArray, clazz: Class<T>): T {
        val converter: Converter<T> = findUpgradeConverter(clazz)
        return converter.upgrade(byteArray)
    }

    inline fun <reified T : Any> upgrade(byteArray: ByteArray): T =
        upgrade(byteArray, T::class.java)


    private fun <T> findDowngradeConverter(clazz: Class<T>): Converter<T> =
        mConverterRegistry.getSuitableConverterByClass(clazz) as Converter<T>

    private fun <T> findUpgradeConverter(clazz: Class<T>): Converter<T> =
        mConverterRegistry.getSuitableConverterByClass(clazz) as Converter<T>
}