package io.geeny.sdk

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.geeny.sdk.geeny.cloud.CloudComponent
import io.geeny.sdk.geeny.cloud.api.repos.Certificate
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CertificateRepositoryTest {

    @Test
    fun testCertificateSaveAndLoad() {

        val appContext = InstrumentationRegistry.getTargetContext()
        val cm = CloudComponent("http://10.0.2.2:8080", appContext)
        val repos = cm.certificateRepository

        var expected: Certificate = Certificate("1", "2", "3")
        var actual = Certificate("4", "5", "6")

        repos
                .save(expected, "TEST")
                .flatMap { repos.load("TEST") }
                .subscribe { actual = it}

        assertEquals(expected, actual)
    }
}