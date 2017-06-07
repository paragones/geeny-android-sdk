package io.geeny.sdk.common.netwok


import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class NetworkClient {

    fun execute(request: Request): Response {
        val url = URL(request.url)
        val connection = url.openConnection() as HttpsURLConnection

        connection.doInput = true
        request.headers.forEach{
            connection.setRequestProperty(it.key, it.value)
        }


        if (request.body != null) {
            connection.doOutput = true
            val os = connection.outputStream
            os.write(request.body)
            os.flush()
            os.close()
        }

        connection.setRequestMethod(request.method)
        val responseCode = connection.responseCode


        var rdata = ByteArray(1)

        var responseBody = ""
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            val br = BufferedReader(InputStreamReader(connection.inputStream))

            br.forEachLine {
                responseBody += it
            }
        } else{
            val br = BufferedReader(InputStreamReader(connection.errorStream))

            br.forEachLine {
                responseBody += it
            }
        }

        val response = Response(request, responseCode, connection.responseMessage, connection.headerFields, rdata, responseBody)

        return response
    }


}