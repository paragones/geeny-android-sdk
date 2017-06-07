package io.geeny.sdk.common.netwok

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class HttpClient {


    companion object {
        @Throws(UnsupportedEncodingException::class)
        fun getQuery(params: List<NameValuePair>): String {
            val result = StringBuilder()
            var first = true

            for ((key, value) in params) {
                if (first)
                    first = false
                else
                    result.append("&")

                result.append(URLEncoder.encode(key, "UTF-8"))
                result.append("=")
                result.append(URLEncoder.encode(value, "UTF-8"))
            }

            return result.toString()
        }
    }
}

data class NameValuePair(val key: String, val value: String)