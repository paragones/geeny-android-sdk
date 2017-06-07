package io.geeny.sample.ui.common.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.geeny.sample.R
import kotlinx.android.synthetic.main.textview_labeled.view.*



class LabeledTextView: LinearLayout {


    var content: String? = null
        set(value) {
            textViewContent.text = value
            textViewContent.isSelected = true
            field = value
        }

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
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.textview_labeled, this, true)

        if(attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.LabeledTextView,
                    0, 0)

            try {
                setLabel(a.getString(R.styleable.LabeledTextView_label))
            } finally {
                a.recycle()
            }
        } else{
            setLabel("No Label Provided")
        }
    }

    fun setLabel(label: String) {
        textViewLabel.text = label
    }

}