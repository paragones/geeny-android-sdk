package io.geeny.sdk.common

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


object TypeConverters {

    fun intToBytes(int: Int, order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        return ByteBuffer.allocate(4).order(order).putInt(int).array()
    }

    fun bytesToInt(bytes: ByteArray, order: ByteOrder = ByteOrder.BIG_ENDIAN): Int {
        return java.nio.ByteBuffer.wrap(bytes).order(order).int
    }

    fun intToBytesDynamic(x: Int, order: ByteOrder = ByteOrder.BIG_ENDIAN): ByteArray {
        var xmut = x
        val byteSize = calculateByteSize(x)
        val bytes = ByteArray(byteSize)
        var i = 0
        while (i < calculateByteSize(x)) {
            val index = if (order == ByteOrder.BIG_ENDIAN) i else byteSize - i - 1
            bytes[index] = (x and 0xFF).toByte()
            i++
            xmut = xmut ushr 8
        }
        return bytes
    }

    fun bytesToIntDynamic(bytes: ByteArray, order: ByteOrder = ByteOrder.BIG_ENDIAN): Int {
        var ret = 0
        var i = 0
        while (i < bytes.size) {
            ret = ret shl 8
            val id = if (order == ByteOrder.BIG_ENDIAN) i else bytes.size - i - 1
            ret = ret or (bytes[id].toInt() and 0xFF)
            i++
        }
        return ret
    }

    private fun calculateByteSize(x: Int): Int {
        var xmut = x
        var n = 0
        while (xmut != 0) {
            xmut = xmut shr 8
            n++
        }
        return n
    }
}

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(prefix: Boolean = true): String {
    val result = StringBuffer()
    if (prefix)
        result.append("0x")

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}


fun ByteArray.toUUID(): UUID {
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }


    val tmpString = result.toString()
    val uuid = tmpString.substring(0, 8) + "-" + tmpString.substring(8, 12) + "-" + tmpString.substring(12, 16) + "-" + tmpString.substring(16, 20) + "-" + tmpString.substring(20, tmpString.length)

    return UUID.fromString(uuid)
}