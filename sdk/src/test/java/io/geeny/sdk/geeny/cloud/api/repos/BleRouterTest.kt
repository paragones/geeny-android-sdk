package io.geeny.sdk.geeny.cloud.api.repos

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.geeny.sdk.GeenyConfiguration
import io.geeny.sdk.SdkInitializationResult
import io.geeny.sdk.SdkTearDownResult
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.ble.BleClientPool
import io.geeny.sdk.common.GLog
import io.geeny.sdk.common.LogLevel
import io.geeny.sdk.common.MemoryKeyValueStore
import io.geeny.sdk.routing.bote.BoteBroker
import io.geeny.sdk.routing.router.Router
import io.geeny.sdk.routing.router.types.Direction
import io.geeny.sdk.routing.router.types.Route
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class BleRouterTest {

    var config: GeenyConfiguration = GeenyConfiguration.Builder().build()
    var router: Router? = null
    var brokerMock: BoteBroker? = null
    var blePoolMock: BleClientPool? = null

    @Before
    fun setup() {
        GLog.level = LogLevel.NONE
    }

    @Test
    fun testRouterInitialization() {
        defaultInit()
        //router = BleRouter(config, brokerMock!!, blePoolMock!!, MemoryKeyValueStore())
        val result = SdkInitializationResult()

        val observer: TestObserver<SdkInitializationResult> = TestObserver()

        router!!.onInit(result)
                .subscribe(observer)

        observer.assertComplete()
        val actualResult = observer.values()[0]
        assertEquals(0, actualResult.numberOfRoutesLoaded)
        assertEquals(0, actualResult.numberOfRoutesLoaded)
    }

    @Test
    fun testRouterCreateRouteAndRestore() {
        defaultInit()
        val store =MemoryKeyValueStore()

        //router = BleRouter(config, brokerMock!!, blePoolMock!!, store)

        val result = SdkInitializationResult()


        router!!.onInit(result)
                .subscribe(TestObserver())

        val address = "address"
        val characteristic = "characteristic"

        val createObserver: TestObserver<Route> = TestObserver()
//        router!!.create(Direction.CONSUMER, address, characteristic)
//                .subscribe(createObserver)

        createObserver.assertComplete()
        val actualRoute: Route = createObserver.values()[0]
        assertEquals(address, actualRoute.info().clientIdentifier)
        assertEquals(characteristic, actualRoute.info().clientResourceId)
        assertEquals(characteristic, actualRoute.info().topic)

        router!!.onTearDown(SdkTearDownResult()).subscribe(TestObserver())


        val reInitObserver = TestObserver<SdkInitializationResult>()
        router!!.onInit(result)
                .subscribe(reInitObserver)



        val getObserver: TestObserver<Route> = TestObserver()

        //router!!.get(address, characteristic, Direction.CONSUMER).subscribe(getObserver)
        val getRoute= getObserver.values()[0]
        assertEquals(address, getRoute.info().clientIdentifier)
        assertEquals(characteristic, getRoute.info().clientResourceId)
        assertEquals(characteristic, getRoute.info().topic)

    }


    fun defaultInit() {
        blePoolMock = mock()
        brokerMock = mock()
        whenever(blePoolMock!!.getOrCreate(any())).thenReturn(Observable.just(mock<BleClient>()))
//        whenever(blePoolMock!!.onInit(any())).thenAnswer {
//            Observable.just(it.arguments)
//        }
//
//        whenever(brokerMock!!.onInit(any())).thenAnswer {
//            Observable.just(it.arguments)
//        }
    }
}