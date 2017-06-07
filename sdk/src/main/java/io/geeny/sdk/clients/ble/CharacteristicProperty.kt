package io.geeny.sdk.clients.ble

enum class CharacteristicProperty(val b: kotlin.Int, val label: String) {
    // Characteristic property: Characteristic has extended properties
    // Constant Value: 128 (0x00000080)
    PROPERTY_EXTENDED_PROPS(0x00000080, "extended_props"),

    // Characteristic property: Characteristic supports indication
    // Constant Value: 32 (0x00000020)
    PROPERTY_INDICATE(0x00000020, "indicate"),

    // Characteristic property: Characteristic supports notification
    // Constant Value: 16 (0x00000010)
    PROPERTY_NOTIFY(0x00000010, "notify"),

    // Characteristic property: Characteristic is readable.
    // Constant Value: 2 (0x00000002)
    PROPERTY_READ(0x00000002, "read"),

    // Characteristic property: Characteristic supports write with signature
    // Constant Value: 64 (0x00000040)
    PROPERTY_SIGNED_WRITE(0x00000040, "signed_write"),

    // Characteristic property: Characteristic can be written.
    // Constant Value: 8 (0x00000008)
    PROPERTY_WRITE(0x00000008, "write"),

    // Characteristic property: Characteristic can be written without response.
    // Constant Value: 4 (0x00000004)
    PROPERTY_WRITE_NO_RESPONSE(0x00000004, "write_no_response");


    companion object {
        fun list(value: Int): List<CharacteristicProperty> = CharacteristicProperty.values().filter { it.b and value != 0 }
        fun get(value: Int): CharacteristicProperty = list(value)[0]
    }
}