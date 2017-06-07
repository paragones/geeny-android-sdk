package io.geeny.sdk.common.netwok

class Request private constructor(builder: Builder) {

    val method = builder.method
    val headers = builder.headers
    val url = builder.url
    var body: ByteArray?

    init {
        if (builder.body != null && builder.body!!.content != null) {
            body = builder.body!!.content
            this.headers.put("Content-Type", builder.body!!.mediaType)
        } else {
            this.body = null;
        }
    }


    fun log(logHeaders: Boolean, logBody: Boolean): String {
        try {
            val sb = StringBuilder()
            val tab = "     "
            val newline = "\n"

            if (logHeaders) {
                sb.append(newline)
                        .append(newline)
            }
            sb.append("---> ")
                    .append(method)
                    .append(" ")
                    .append(url)
                    .append(newline)

            if (logHeaders) {
                headers.entries.forEach {
                    sb.append(tab)
                            .append(it.key)
                            .append(" : ")
                            .append(it.value)
                            .append(newline)
                }
            }


            try {
                if (logBody && body != null) {
                    sb.append(newline)
                            .append(body.toString())
                            .append(newline)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (logHeaders) {
                sb.append(" ");
            }

            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }


    class Builder {
        lateinit var url: String
        lateinit var method: String
        var body: RequestBody? = null
        val headers: MutableMap<String, String> = HashMap()

        fun method(httpMethod: String): Builder {
            this.method = httpMethod
            return this
        }

        fun body(body: RequestBody): Builder {
            this.body = body
            return this
        }

        fun url(value: String): Builder {
            this.url = value
            return this
        }

//        fun contentType(contentType: String): Builder {
//            headers.put("Content-Type", contentType)
//            return this
//        }

        fun accept(contentType: String): Builder {
            headers.put("Accept", contentType)
            return this
        }

        fun userAgent(value: String): Builder {
            headers.put("User-Agent", value)
            return this
        }

        fun build(): Request {
            return Request(this)
        }

        fun basicAuth(email: String, pass: String): Builder {
            // fuck it for now
            /*try {
                _headers.put("Authorization", "Basic " + Base64.encodeToString((user + ":" + pass).getBytes("UTF-8"), Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/

            return this
        }

        fun header(key: String, value: String): Builder {
            headers.put(key, value)
            return this
        }

    }
}