package io.geeny.sdk.geeny.cloud.api.repos.certificate

import android.content.Context
import io.geeny.sdk.geeny.cloud.api.repos.Certificate
import io.geeny.sdk.clients.mqtt.certificate.CertificateImporter
import io.reactivex.Observable
import java.io.FileOutputStream
import com.github.daemontus.Result
import com.github.daemontus.isError
import com.github.daemontus.unwrap
import java.io.File
import java.io.FileInputStream
import java.lang.Exception


class CertificateRepository(val context: Context) {

    companion object {
        public val passphrase = "this_is_a_passphrase"
        val keystoreFilename = "myfile"
        val ca_prefix = "CA_"
        val cert_prefix = "CERT_"
        val priv_prefix = "PRIV_"
    }

    val dir = context.filesDir.path.toString()


    fun save(certificate: Certificate, filename: String): Observable<Certificate> {
        return Observable.create { subscriber ->

            try {
                save(ca_prefix + filename, certificate.CAcert)
                save(cert_prefix + filename, certificate.certPem)
                save(priv_prefix + filename, certificate.privPem)
                subscriber.onNext(certificate)
                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    fun load(filename: String): Observable<Certificate> {
        return Observable.create { subscriber ->

            val c = loadFile(cert_prefix + filename)
            val p = loadFile(priv_prefix + filename)
            val ca = loadFile(ca_prefix + filename)

            if (c.isError() || p.isError() || ca.isError()) {
              //  subscriber.onError(Throwable("error in parsing file"))
            } else {
                val cert = Certificate(c.unwrap(), p.unwrap(), ca.unwrap())
                subscriber.onNext(cert)
            }

            subscriber.onComplete()
        }
    }

    fun loadFile(filename: String): Result<String, Throwable> {
        try {
            val file = File(dir, filename)
            val bytes = ByteArray(file.length().toInt())
            val `in` = FileInputStream(file)
            try {

                `in`.read(bytes)
            } finally {
                `in`.close()
            }
            return Result.Ok(String(bytes))
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    fun save(filename: String, data: String) {
        val file = File(dir, filename)
        val stream = FileOutputStream(file)
        try {
            stream.write(data.toByteArray())
        } finally {
            stream.close()
        }
    }

    fun store(certificate: Certificate): Observable<String> {
        return Observable.create { subscriber ->
            val dir = context.filesDir.path //Environment.getExternalStorageDirectory().path
            val importer = CertificateImporter(certificate.privPem, certificate.certPem, certificate.CAcert, passphrase, dir, keystoreFilename)
            importer.createBouncyCastleKeyStore()
            subscriber.onNext(keystoreFilename)
            subscriber.onComplete()
        }
    }

}