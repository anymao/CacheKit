package com.anymore.cachekit

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 文本编解码相关
 * Created by liuyuanmao on 2019/11/11.
 */
// MD5
internal val DIGITS =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

internal fun md5(source: String): String {
    val digest: MessageDigest
    try {
        digest = MessageDigest.getInstance("MD5")
    } catch (e: NoSuchAlgorithmException) {
        throw IllegalStateException("System doesn't support MD5 algorithm.")
    }
    try {
        digest.update(source.toByteArray(Charsets.UTF_8))
    } catch (e: UnsupportedEncodingException) {
        throw IllegalStateException("System doesn't support your EncodingException.")
    }
    val bytes = digest.digest()
    return String(encodeHex(bytes))
}

internal fun encodeHex(byteArray: ByteArray): CharArray {
    val length = byteArray.size
    val result = CharArray(length shl 1)
    // two characters form the hex value.
    var i = 0
    var j = 0
    while (i < length) {
        result[j++] = DIGITS[(0xF0 and byteArray[i].toInt()).ushr(4)]
        result[j++] = DIGITS[0x0F and byteArray[i].toInt()]
        i++
    }
    return result
}