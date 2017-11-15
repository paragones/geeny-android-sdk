package io.geeny.sdk.geeny.cloud.api.repos

import java.security.KeyPair
import java.security.cert.X509Certificate


fun emptyThing(): CloudThingInfo {
    return CloudThingInfo("", "", "", Certificate("", "", ""), "", "", true)
}

data class CloudThingInfo(val id: String,
                          val name: String,
                          val serial_number: String,
                          val certs: Certificate,
                          val thing_type: String,
                          val created: String,
                          val isEmpty: Boolean = false
)

data class Resource(
        val uri: String,
        val method: String,
        val messageType: String
)

data class ThingType(
        val id: String,
        val name: String,
        val created: String,
        val resources: List<Resource>
)


data class MessageType(
        val id: String,
        val name: String,
        val description: String,
        val mediaType: String,
        val created: String,
        val tags: List<String>
)

data class Certificate(
        val certPem: String,
        val privPem: String,
        val CAcert: String
)

val emptyCertificate = Certificate("", "", "")

data class CertificatesInfo(
        val ca: X509Certificate,
        val client: X509Certificate,
        val keyPair: KeyPair
)
