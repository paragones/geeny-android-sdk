package com.leondroid.demo_app.ui.common

import android.content.res.Resources
import android.util.TypedValue
import android.view.View

object ViewUtils {

    fun px(dp: Float, resources: Resources) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

    fun show(show: Boolean, view: View) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
}