package io.geeny.sdk.geeny.cloud.api.repos

import io.geeny.sdk.common.MemoryKeyValueStore
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.RouteInfo
import io.geeny.sdk.routing.router.types.RouteType
import io.geeny.sdk.routing.router.types.RouterDisk
import io.reactivex.observers.TestObserver
import org.junit.Test
import kotlin.test.assertEquals

class RouterDiskTest {

    @Test
    fun testSaveAndGet() {
        val store = MemoryKeyValueStore()
        val disk = RouterDisk(store)

        val expectedResult = RouteInfo(RouteType.BLE, Direction.CONSUMER, "testTopic", "testClientIdentifier", "testClientResourceIdentifier")

        val testObserver = TestObserver<RouteInfo>()
        disk.save(expectedResult).subscribe(testObserver)
        testObserver.onComplete()
        assertEquals(expectedResult, testObserver.values()[0])


        val getObserver = TestObserver<RouteInfo>()
        disk.get(expectedResult.identifier()).subscribe(getObserver)
        getObserver.onComplete()
        assertEquals(expectedResult, getObserver.values()[0])

    }
}