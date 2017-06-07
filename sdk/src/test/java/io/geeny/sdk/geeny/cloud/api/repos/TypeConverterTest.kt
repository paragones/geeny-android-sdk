package io.geeny.sdk.geeny.cloud.api.repos

import io.geeny.sdk.common.TypeConverters
import org.junit.Test
import java.nio.ByteOrder
import kotlin.test.assertEquals

class TypeConverterTest {

    @Test
    fun intToByteArrayAndBack() {
        val expected = 89
        val array = TypeConverters.intToBytes(89)
        val actual = TypeConverters.bytesToInt(array)
        assertEquals(expected, actual)
    }


    @Test
    fun bytesToIntDynAndBackBigEndian() {
        val bytes: ByteArray = ByteArray(4)
        bytes[0] = 3
        bytes[1] = 8
        bytes[2] = 13
        bytes[3] = 1

        val result = TypeConverters.bytesToIntDynamic(bytes, ByteOrder.BIG_ENDIAN)
        val expected = TypeConverters.bytesToInt(bytes, ByteOrder.BIG_ENDIAN)

        assertEquals(expected, result)
    }

    @Test
    fun bytesToIntDynAndBack() {
        val bytes: ByteArray = ByteArray(4)
        bytes[0] = 3
        bytes[1] = 8
        bytes[2] = 13
        bytes[3] = 1

        val result = TypeConverters.bytesToIntDynamic(bytes, ByteOrder.LITTLE_ENDIAN)
        val expected = TypeConverters.bytesToInt(bytes, ByteOrder.LITTLE_ENDIAN)

        assertEquals(expected, result)
    }

    @Test
    fun intToBytesToIntDynAndBack() {
        val bytes: ByteArray = ByteArray(2)
        bytes[0] = 0
        bytes[1] = 6

        val result = TypeConverters.bytesToIntDynamic(bytes, ByteOrder.BIG_ENDIAN)

        //assertEquals(expected, result)
    }
}