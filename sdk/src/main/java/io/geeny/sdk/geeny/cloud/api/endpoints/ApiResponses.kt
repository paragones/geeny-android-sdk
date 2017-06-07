package io.geeny.sdk.geeny.cloud.api.endpoints

import io.geeny.sdk.geeny.cloud.api.repos.Certificate


////////////////////////////////////////////////////////////////////////
//
//  Common
//
////////////////////////////////////////////////////////////////////////
data class Meta(
        val limit: Int,
        val offset: Int
)

////////////////////////////////////////////////////////////////////////
//
// ThingEndpoint
//
////////////////////////////////////////////////////////////////////////
data class ThingResponse(val id: String,
                         val name: String,
                         val serial_number: String,
                         val certs: CertificateResponse,
                         val thing_type: String,
                         val created: String
)

data class ThingListResponse(val meta: Meta,
                             val data: List<ThingResponse>
)

data class ThingPostBody(val name: String,
                         val serial_number: String,
                         val thing_type: String
)

////////////////////////////////////////////////////////////////////////
//
// ResourceEndpoint
//
////////////////////////////////////////////////////////////////////////
data class ResourceResponse(
        val uri: String,
        val method: String,
        val message_type: String
)

data class ResourceListResponse(val meta: Meta,
                                val data: List<ResourceResponse>
)

////////////////////////////////////////////////////////////////////////
//
// ThingTypeEndpoint
//
////////////////////////////////////////////////////////////////////////
data class ThingTypeResponse(val id: String,
                             val name: String,
                             val created: String,
                             val public: Boolean
)

data class ThingTypeListResponse(val meta: Meta,
                                 val data: List<ThingTypeResponse>
)

////////////////////////////////////////////////////////////////////////
//
// MessageTypeEndpoint
//
////////////////////////////////////////////////////////////////////////
data class MessageTypeResponse(
        val id: String,
        val name: String,
        val media_type: String,
        val description: String,
        val created: String,
        val tags: List<String>
)

data class MessageTypeListResponse(val meta: Meta,
                                   val data: List<MessageTypeResponse>
)


////////////////////////////////////////////////////////////////////////
//
// CertificateEndpoint
//
////////////////////////////////////////////////////////////////////////
data class CertificateResponse(
        val ca: String,
        val cert: String,
        val key: String
)
