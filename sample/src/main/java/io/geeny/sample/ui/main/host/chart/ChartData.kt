package io.geeny.sample.ui.main.host.chart

data class ChartData<T>(val data: MutableList<T>, val minX: T, val minY: T, val maxX: T, val maxY: T) {
    fun size(): Int = data.size
    fun y(at: Int): T = data[at]
}

