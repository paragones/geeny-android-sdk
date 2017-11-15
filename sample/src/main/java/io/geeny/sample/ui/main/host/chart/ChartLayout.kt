package io.geeny.sample.ui.main.host.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class ChartLayout : View {

    private var chartData: ChartData<Int>? = null

    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 6f
    }

    private val paintAxis: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.LTGRAY
    }

    private val paintText: Paint = TextPaint().apply {
        color = Color.LTGRAY
        textSize = 45f
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {}

    fun setData(chartData: ChartData<Int>) {
        this.chartData = chartData
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isValid()) {
            return
        }

        val path: Path = Path()
        val margin = 50f

        val w = canvas!!.width.toFloat()
        val h = canvas!!.height.toFloat()
        val half = h / 2



        val x = XToPx(0, w)
        val y = YToPx(chartData!!.y(0), h)
        path.moveTo(x, y)


        for (i in 1 until chartData!!.size()) {
            val xx = XToPx(i, w)
            val yy = YToPx(chartData!!.y(i), h)
            path.lineTo(xx, yy)
        }

        canvas.drawPath(path, paint)


        canvas.drawLine(0f, h / 2, w, h / 2, paintAxis)
        canvas.drawLine(0f, 0f, 0f, h, paintAxis)

        canvas.drawLine(w - margin / 2, half + margin / 2, w - margin / 2, half - margin / 2, paintAxis)
        canvas.drawLine(0f, margin/2, margin/2, margin/2, paintAxis)

        if (chartData!!.maxY > half || -chartData!!.minY > half) {
            canvas.drawText(chartData!!.maxY.toString(), margin, margin, paintText)
        } else {
            canvas.drawText(half.toString(), margin, margin, paintText)
        }

        if (chartData!!.maxX < w) {
            canvas.drawText(w.toString(), w - margin * 3, half + margin, paintText)
        } else {
            canvas.drawText(chartData!!.maxX.toString(), w - margin * 3, half + margin * 2, paintText)
        }
    }

    fun XToPx(x: Int, width: Float): Float {
        if (chartData!!.size() < width) {
            return x.toFloat()
        }

        val chartWidth = chartData!!.maxX - chartData!!.minX
        return width * x / chartWidth
    }

    fun YToPx(y: Int, height: Float): Float {

        val half = height / 2


        if (chartData!!.maxY > half || -chartData!!.minY > half) {
            val chartHeight = chartData!!.maxY - chartData!!.minY
            return half - half * y / chartHeight
        }

        return half - y.toFloat()

    }

    fun isValid(): Boolean {
        return chartData != null && chartData?.data!!.size > 0
    }
}