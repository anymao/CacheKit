package com.anymore.cachekit.converters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * Created by liuyuanmao on 2019/11/14.
 */
object DrawableConverter : Converter<Drawable> {

    private val mBitmapConverter by lazy { BitmapConverter }

    @Suppress("DEPRECATION")
    override fun downgrade(value: Drawable): ByteArray {
        return if (value is BitmapDrawable) {
            if (value.bitmap != null) {
                mBitmapConverter.downgrade(value.bitmap)
            } else {
                ByteArray(0)
            }
        } else {
            val width = if (value.intrinsicWidth <= 0) {
                1
            } else {
                value.intrinsicWidth
            }
            val height = if (value.intrinsicHeight <= 0) {
                1
            } else {
                value.intrinsicHeight
            }
            val config = if (value.opacity != PixelFormat.OPAQUE) {
                Bitmap.Config.ARGB_8888
            } else {
                Bitmap.Config.RGB_565
            }
            val bitmap = Bitmap.createBitmap(width, height, config)
            val canvas = Canvas(bitmap)
            value.setBounds(0, 0, canvas.width, canvas.height)
            value.draw(canvas)
            mBitmapConverter.downgrade(bitmap)
        }
    }

    override fun upgrade(byteArray: ByteArray): Drawable {
        return BitmapDrawable(null,mBitmapConverter.upgrade(byteArray))
    }

    /**
     * [DrawableConverter]理论上可处理[Drawable]与其子类
     */
    override fun canConvert(clazz: Class<*>): Boolean = Drawable::class.java.isAssignableFrom(clazz)

}