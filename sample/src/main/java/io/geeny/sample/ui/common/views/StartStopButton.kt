package io.geeny.sample.ui.common.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.geeny.sample.GatewayApp
import io.geeny.sample.R
import io.geeny.sdk.common.ConnectionState
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.button_start_stop.view.*

class StartStopButton : LinearLayout {
    private var disposable: Disposable? = null
    var callback: Callback? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.button_start_stop, this, true)

        setPadding(ViewUtils.px(16f, resources), 0, ViewUtils.px(16f, resources), 0)
        orientation = LinearLayout.HORIZONTAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        disposable?.dispose()
    }

    fun connect(observable: Observable<ConnectionState>) {
        disposable?.dispose()
        disposable = observable
                .observeOn(GatewayApp.from(context).component.mainScheduler)
                .subscribe {
                    when (it) {
                        ConnectionState.CONNECTED -> {
                            textViewStartStopConnectionState.text = "Connected"
                            textViewStartStopConnectionState.setTextColor(Color.GREEN)
                            textViewStartStopButton.text = "Stop"
                            ViewUtils.show(false, progressBarStartStop)
                            buttonStartStop.setOnClickListener {
                                callback?.onStop()
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            textViewStartStopConnectionState.text = "Disconnected"
                            textViewStartStopConnectionState.setTextColor(Color.RED)
                            textViewStartStopButton.text = "Start"
                            ViewUtils.show(false, progressBarStartStop)
                            buttonStartStop.setOnClickListener {
                                callback?.onStart()
                            }
                        }
                        ConnectionState.CONNECTING -> {
                            textViewStartStopConnectionState.text = "Connecting..."
                            textViewStartStopConnectionState.setTextColor(Color.LTGRAY)
                            textViewStartStopButton.text = "Connecting"
                            ViewUtils.show(true, progressBarStartStop)
                        }
                    }
                }
    }

    interface Callback {
        fun onStart()
        fun onStop()
    }
}