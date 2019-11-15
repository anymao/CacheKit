package com.anymore.cachekit.converters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * [Bitmap]转字节数组的实现
 * Created by liuyuanmao on 2019/11/11.
 */
object BitmapConverter : Converter<Bitmap> {

    override fun downgrade(value: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        value.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    override fun upgrade(byteArray: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

    override fun canConvert(clazz: Class<*>): Boolean = clazz == Bitmap::class.java
}