package com.anymore.cachekit.converters

/**转换器接口，提供将某个对象转换成字节数组以及将字节数组还原为该对象的方法
 * [canConvert]指定此转换器能否处理这个class的类型
 * Created by liuyuanmao on 2019/11/11.
 */
interface Converter<T> {

    /**
     * 降级接口,将[value]转换为字节数组[ByteArray]
     */
    fun downgrade(value: T): ByteArray

    /**
     *升级接口,将[byteArray]还原为原对象
     */
    fun upgrade(byteArray: ByteArray): T

    /**
     * 此接口能否处理这个类型的对象
     */
    fun canConvert(clazz: Class<*>): Boolean
}