package io.geeny.sdk.geeny.cloud.api.repos

import io.geeny.sdk.common.TypeConverters
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
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
        val bytes = ByteArray(4)
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
        val bytes = ByteArray(4)
        bytes[0] = 3
        bytes[1] = 8
        bytes[2] = 13
        bytes[3] = 1

        val result = TypeConverters.bytesToIntDynamic(bytes, ByteOrder.LITTLE_ENDIAN)
        val expected = TypeConverters.bytesToInt(bytes, ByteOrder.LITTLE_ENDIAN)

        assertEquals(expected, result)
    }

    @Test
    fun intToBytesToIntDynAndBackWithBigEndian() {
        val bytes = ByteArray(4)
        bytes[0] = 3
        bytes[1] = 8
        bytes[2] = 1
        bytes[3] = 1

        val actualInt = TypeConverters.bytesToIntDynamic(bytes)
        val actualByte = TypeConverters.intToBytesDynamic(actualInt)

        val expectedInt = TypeConverters.bytesToIntDynamic(actualByte)

        assertEquals(16843009, expectedInt)
    }

    @Test
    fun intToBytesToIntDynAndBackWithLittleEndian() {
        val bytes = ByteArray(4)
        bytes[0] = 3
        bytes[1] = 8
        bytes[2] = 1
        bytes[3] = 1

        val actualInt = TypeConverters.bytesToIntDynamic(bytes, ByteOrder.LITTLE_ENDIAN)
        val actualByte = TypeConverters.intToBytesDynamic(actualInt, ByteOrder.LITTLE_ENDIAN)

        val expectedInt = TypeConverters.bytesToIntDynamic(actualByte, ByteOrder.LITTLE_ENDIAN)

        assertEquals(50529027, expectedInt)
    }
}