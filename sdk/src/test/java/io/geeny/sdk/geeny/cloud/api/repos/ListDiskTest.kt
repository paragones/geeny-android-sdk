package io.geeny.sdk.geeny.cloud.api.repos

import io.geeny.sdk.common.JSONConverter
import io.geeny.sdk.common.MemoryKeyValueStore
import io.geeny.sdk.common.ListDisk
import io.reactivex.observers.TestObserver
import org.json.JSONObject
import org.junit.Test
import kotlin.test.assertEquals

class ListDiskTest {

    val mockListId = "mock"

    @Test
    fun testSave() {
        val store = MemoryKeyValueStore()
        val disk = ListDisk(store, MockValueConverter(), mockListId)

        val expectedId = "expectedId"
        val expected = MockValue(expectedId)

        val testObserver = TestObserver<MockValue>()
        disk.save(expected).subscribe(testObserver)
        val actual = testObserver.values()[0]
        testObserver.assertComplete()
        assertEquals(expected.id, actual.id)

        val listObserver = TestObserver<List<MockValue>>()

        disk.list().subscribe(listObserver)

        listObserver.assertComplete()
        val actualList = listObserver.values()[0][0]
        assertEquals(expected, actualList)
    }

    @Test
    fun testRemove() {
        val store = MemoryKeyValueStore()
        val disk = ListDisk(store, MockValueConverter(), mockListId)
        val expectedId1 = "expectedId1"
        val expected1 = MockValue(expectedId1)
        val expectedId2 = "expectedId2"
        val expected2 = MockValue(expectedId2)

        disk.save(expected1).flatMap { disk.save(expected2) }.flatMap { disk.remove(expected1) }.subscribe()

        val listObserver = TestObserver<List<MockValue>>()

        disk.list().subscribe(listObserver)

        listObserver.assertComplete()
        val actualList = listObserver.values()[0]
        assertEquals(1, actualList.size)
        assertEquals(expected2, actualList[0])

    }
}

data class MockValue(val id: String)

class MockValueConverter : JSONConverter<MockValue> {
    private val JSON_KEY_ID = "JSON_KEY_ID"
    override fun id(value: MockValue) = value.id

    override fun toJSON(value: MockValue): JSONObject =
            JSONObject().apply {
                put(JSON_KEY_ID, value.id)
            }

    override fun fromJSON(json: JSONObject): MockValue =
            MockValue(json.getString(JSON_KEY_ID))

}