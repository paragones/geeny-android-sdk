package io.geeny.sdk.common.netwok


class Response(
        private val _request: Request,
        private val responseCode: Int,
        private val responseMessage: String,
        private val _headers: Map<String, List<String>>,
        private val _body: ByteArray,
        val responseBody: String) {

    val isSuccessful: Boolean
        get() = responseCode >= 200 && responseCode <= 299

    fun body(): ByteArray? {
        return _body
    }

    fun code(): Int {
        return responseCode
    }

    fun message(): String {
        return responseMessage
    }

    fun request(): Request {
        return _request
    }

    fun headers(): Map<String, List<String>> {
        return _headers
    }

    fun contentType(): String? {
        return firstHeader("Content-Type")
    }

    private fun firstHeader(s: String): String? {
        val l = headers()[s]
        return if (l != null && l.size > 0) {
            l[0]
        } else null
    }

    fun log(logHeaders: Boolean): String {
        try {
            val sb = StringBuilder()
            val tab = "     "
            val newline = "\n"
            val clog = logHeaders || body() != null

            if (clog) {
                sb.append(newline)
                sb.append(newline)
            }

            sb.append("<--- ")
                    .append(code())
                    .append(" ")
                    .append(message())
                    .append(" ")
                    .append(request().url)

            sb.append(newline)

            if (logHeaders)
                for ((key, value) in headers()) {
                    for (value in value) {
                        sb.append(tab)
                        sb.append(key)
                        sb.append(" : ")
                        sb.append(value)
                        sb.append(newline)
                    }
                }

            if (body() != null) {
                sb.append(newline)
                sb.append(body().toString())
                sb.append(newline)

            }

            if (clog) {
                sb.append(" ")
            }

            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun result(): String {
        var response = ""
//        if (responseCode == HttpsURLConnection.HTTP_OK) {
//            var line: String
//            val br = BufferedReader(InputStreamReader(conn.inputStream))
//            line = br.readLine()
//
//            while (line.isNotEmpty()) {
//                response += line
//                line = br.readLine()
//            }
//        }
        return response
    }
}