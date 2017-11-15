package io.geeny.sdk.geeny.cloud.api.endpoints

import io.geeny.sdk.geeny.cloud.api.repos.*


fun thingResponse2Thing(t: ThingResponse): CloudThingInfo {
    return CloudThingInfo(
            t.id,
            t.name,
            t.serial_number,
            certificateResponse2Certificate(t.certs),
            t.thing_type,
            t.created
    )
}


fun resourceResponse2Resource(r: ResourceResponse): Resource {
    val res =  Resource(
            r.uri,
            r.method,
            r.message_type
    )

    return res
}

fun messageTypeResponse2MessageType(mt: MessageTypeResponse): MessageType {
    return MessageType(
            mt.id,
            mt.name,
            mt.description,
            mt.media_type,
            mt.created,
            mt.tags
    )
}

fun certificateResponse2Certificate(c: CertificateResponse): Certificate {
    return Certificate(c.cert, c.key, c.ca)
}