package io.geeny.sdk.routing.bote.topicjournal

import com.github.daemontus.Option

abstract class Queue<T> {
    var itemSize = 0
    var memorySize = 0


    private class Item<T>(val data: T, var next: Item<T>? = null)

    private var head: Item<T>? = null
    private var tail: Item<T>? = null

    fun push(item: T) {
        itemSize += 1
        memorySize += valueSize(item)
        val i = Item(item)
        if (tail == null) {
            head = i
            tail = head
        } else {
            tail?.next = i
            tail = i
        }
    }

    fun pop(): Option<T> =
            if (head == null)
                Option.None()
            else {
                val result = head!!.data
                itemSize -= 1
                memorySize -= valueSize(result)
                head = head!!.next
                if (head == null)
                    tail = null
                Option.Some(result)
            }

    val isEmpty
        get() = head == null


    abstract fun valueSize(value: T): Int
}