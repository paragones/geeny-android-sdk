package io.geeny.sdk.common.netwok


class HttpErrorResponseException(val code: Int, message: String) : Exception("http response: $code:$message") {

    /**
     * we already start at code 499 because of nginx sending that for server timeout
     *
     * @return
     */
    val isServerError: Boolean
        get() = code in 499..599
}


object Http {
    val METHOD_POST = "POST"
    val METHOD_GET = "GET"

    val MEDIA_TYPE_APPLICATION_JSON = "application/json"
}