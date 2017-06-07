package io.geeny.sdk.clients.mqtt.certificate

import com.github.daemontus.Result
import io.geeny.sdk.geeny.cloud.api.repos.Certificate
import io.geeny.sdk.geeny.cloud.api.repos.CertificatesInfo
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter
import org.spongycastle.openssl.PEMKeyPair
import org.spongycastle.openssl.PEMParser
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory


/**
 * Turns a certificate defined by the CertificateManager into parsed certificates
 * and KeyPair to ensure correct formats.
 */
fun toCertificateInfo(certificate: Certificate): Result<CertificatesInfo, Throwable> {

    fun openPEMResource(pemString: String): PEMParser = PEMParser(StringReader(pemString))

    val pemPrivateKeyRd = openPEMResource(certificate.privPem)
    val pemClientCertificateRd = openPEMResource(certificate.certPem)
    val pemCACertificateRd = openPEMResource(certificate.CAcert)

    val pair: KeyPair
    val clientCertificate: X509Certificate
    val CACertificate: X509Certificate

    val cao = pemCACertificateRd.readObject()

    if (cao != null && cao is X509CertificateHolder) {
        CACertificate = JcaX509CertificateConverter().setProvider("SC").getCertificate(cao)
    } else {
        return Result.Error(Throwable("couldn't read ca certificate"))
    }

    val clo = pemClientCertificateRd.readObject()

    if (clo != null && clo is X509CertificateHolder) {
        clientCertificate = JcaX509CertificateConverter().setProvider("SC").getCertificate(clo)
    } else {
        return Result.Error(Throwable("couldn't read client certificate"))
    }

    val kpo = pemPrivateKeyRd.readObject();

    if (kpo != null && kpo is PEMKeyPair) {
        pair = JcaPEMKeyConverter().setProvider("SC").getKeyPair(kpo)
    } else {
        return Result.Error(Throwable("couldn't read key pair"))
    }

    return Result.Ok(CertificatesInfo(CACertificate, clientCertificate, pair))
}

/**
 * Creates a SSLFactory on terms of CertificateInfo
 */
@Throws(Exception::class)
fun sslSocketFactoryFrom(info: CertificatesInfo, password: String): SSLSocketFactory {
    // CA certificate is used to authenticate server
    val caKs = KeyStore.getInstance("BKS", "SC")
    caKs.load(null, null)
    caKs.setCertificateEntry("ca-certificate", info.ca)
    val tmf = TrustManagerFactory.getInstance("PKIX")
    tmf.init(caKs)

    // client key and certificates are sent to server so it can authenticate us
    val ks = KeyStore.getInstance("BKS", "SC")
    ks.load(null, null)
    ks.setCertificateEntry("certificate", info.client)
    ks.setKeyEntry("private-key", info.keyPair.private, password.toCharArray(), arrayOf(info.client))

    val kmf = KeyManagerFactory.getInstance("PKIX")
    kmf.init(ks, password.toCharArray())

    // finally, create SSL socket factory
    val context = SSLContext.getInstance("TLSv1")
    context.init(kmf.keyManagers, tmf.trustManagers, null)

    return context.socketFactory
}