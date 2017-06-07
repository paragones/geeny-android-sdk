package io.geeny.sdk.common.netwok

class RequestBody private constructor(val mediaType: String, val content: ByteArray) {
    companion object {
        fun create(mediaType: String, content: ByteArray): RequestBody {
            return RequestBody(mediaType, content)
        }
    }
}