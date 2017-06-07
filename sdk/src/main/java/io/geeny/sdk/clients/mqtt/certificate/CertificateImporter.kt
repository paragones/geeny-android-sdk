package io.geeny.sdk.clients.mqtt.certificate

import android.util.Log
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter
import org.spongycastle.openssl.PEMKeyPair
import org.spongycastle.openssl.PEMParser
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringReader
import java.security.KeyPair
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory


class CertificateImporter(
        val pemPrivateKey: String,
        val pemClientCertificate: String,
        val pemCACertificate: String,
        val passPhrase: String,
        val outputDirectory: String,
        val outputFilename: String) {


    constructor(c: io.geeny.sdk.geeny.cloud.api.repos.Certificate, dir: String, filename: String) : this(c.privPem, c.certPem, c.CAcert, pass_phrase, dir, filename)

    companion object {
        val pass_phrase = "a_passphrase"
        val KEYSTORE_FRIENDLY_NAME = "Geeny_Mobile_SDK"
    }


    fun openPEMResource(pemString: String): PEMParser = PEMParser(StringReader(pemString))

    fun createBouncyCastleKeyStore(): Boolean {

        val path = outputDirectory + "/" + outputFilename
        Log.d("BITCH", "Path in importer: " + path)

        val pemPrivateKeyRd = openPEMResource(pemPrivateKey)
        val pemClientCertificateRd = openPEMResource(pemClientCertificate)
        val pemCACertificateRd = openPEMResource(pemCACertificate)

        var o: Any


        var pair: KeyPair? = null
        var clientCertificate: X509Certificate? = null
        var CACertificate: X509Certificate? = null

        o = pemCACertificateRd.readObject()

        if (o != null && o is X509CertificateHolder) {
            CACertificate = JcaX509CertificateConverter().setProvider("SC").getCertificate(o)
        }

        o = pemClientCertificateRd.readObject()

        if (o != null && o is X509CertificateHolder) {
            clientCertificate = JcaX509CertificateConverter().setProvider("SC").getCertificate(o)
        }

        o = pemPrivateKeyRd.readObject();

        if (o != null && o is PEMKeyPair) {
            pair = JcaPEMKeyConverter().setProvider("SC").getKeyPair(o)
        }


        // CA certificate is used to authenticate server
//        val caKs = KeyStore.getInstance(KeyStore.getDefaultType())
//        caKs.load(null, null)
//        caKs.setCertificateEntry("ca-certificate", CACertificate)
//        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//        tmf.onInit(caKs)

        // Create Bouncy Castle Keystore
        val keyStore: KeyStore = KeyStore.getInstance("BKS", "SC")
        keyStore.load(null, null)
        keyStore.store(FileOutputStream(path), passPhrase.toCharArray())


        // Store the certificates and private keys
        val ks = KeyStore.getInstance("BKS", "SC")
        ks.load(FileInputStream(path), passPhrase.toCharArray())

        // Finally, keystore is saved to file.
        ks.setKeyEntry(KEYSTORE_FRIENDLY_NAME, pair!!.private, passPhrase.toCharArray(), arrayOf(clientCertificate, CACertificate))

        val os = FileOutputStream(path)
        ks.store(os, passPhrase.toCharArray())
        os.close()

        return true
//
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    fun saveKeyStore(keyStore: KeyStore, path: String) {

    }



    @Throws(Exception::class)
    fun getSocketFactory(password: String): SSLSocketFactory {

        val pemPrivateKeyRd = openPEMResource(pemPrivateKey)
        val pemClientCertificateRd = openPEMResource(pemClientCertificate)
        val pemCACertificateRd = openPEMResource(pemCACertificate)

        var o: Any


        var pair: KeyPair? = null
        var clientCertificate: X509Certificate? = null
        var CACertificate: X509Certificate? = null

        o = pemCACertificateRd.readObject()

        if (o != null && o is X509CertificateHolder) {
            CACertificate = JcaX509CertificateConverter().setProvider("SC").getCertificate(o)
        }

        o = pemClientCertificateRd.readObject()

        if (o != null && o is X509CertificateHolder) {
            clientCertificate = JcaX509CertificateConverter().setProvider("SC").getCertificate(o)
        }

        o = pemPrivateKeyRd.readObject();

        if (o != null && o is PEMKeyPair) {
            pair = JcaPEMKeyConverter().setProvider("SC").getKeyPair(o)
        }


        // CA certificate is used to authenticate server
        val caKs = KeyStore.getInstance("BKS", "SC")
        caKs.load(null, null)
        caKs.setCertificateEntry("ca-certificate", CACertificate)
        val tmf = TrustManagerFactory.getInstance("PKIX")
        tmf.init(caKs)

        // client key and certificates are sent to server so it can authenticate us
        val ks = KeyStore.getInstance("BKS", "SC")
        ks.load(null, null)
        ks.setCertificateEntry("certificate", clientCertificate)
        ks.setKeyEntry("private-key", pair!!.private, password.toCharArray(), arrayOf(clientCertificate))
        val kmf = KeyManagerFactory.getInstance("PKIX")
        kmf.init(ks, password.toCharArray())

        // finally, create SSL socket factory
        val context = SSLContext.getInstance("TLSv1")
        context.init(kmf.keyManagers, tmf.trustManagers, null)

        return context.socketFactory
    }
}