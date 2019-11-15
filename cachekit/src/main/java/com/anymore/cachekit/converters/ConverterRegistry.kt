package com.anymore.cachekit.converters

/**
 * 转换器的注册和管理
 * Created by liuyuanmao on 2019/11/14.
 */
internal class ConverterRegistry {
    /**
     * 已经匹配过的缓存转换器
     */
    private val mConverterCache: MutableMap<Class<*>, Converter<*>> by lazy { HashMap<Class<*>, Converter<*>>() }

    private val mConverters = arrayListOf<Converter<*>>()

    private val mImmutableConverters: List<Converter<*>> by lazy { mConverters.toList() }

    /**
     * 注册内置的转换器
     */
    init {
        mConverters.add(BitmapConverter)
        mConverters.add(JSONObjectConverter)
        mConverters.add(JSONArrayConverter)
    }

    /**
     * 提供外部注册转换器接口
     */
    fun register(converter: Converter<*>) {
        mConverters.add(converter)
    }

    /**
     * 根据要转换的类型获取匹配的转化器
     */
    fun getSuitableConverterByClass(clazz: Class<*>): Converter<*> {
        //先从缓存中去取
        val cachedConverter = mConverterCache[clazz]
        if (cachedConverter != null) {
            return cachedConverter
        } else {
            //缓存中没有，则从所有里面遍历，再加入到缓存
            for (converter in mImmutableConverters) {
                if (converter.canConvert(clazz)) {
                    mConverterCache[clazz] = converter
                    return converter
                }
            }
            //don't find Suitable Converter for this class
            throw RuntimeException("don't find suitable converter for class<${clazz.name}>," +
                    "you can implements the <${Converter::class.qualifiedName}> to Convert " +
                    "this Class to ByteArray,and then register the converter.")
        }
    }

}