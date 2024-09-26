package com.rameshvoltella.pdfeditorpro.views

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class AutoHeightWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Measure the WebView based on its content
        val heightSpec = MeasureSpec.makeMeasureSpec(
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.UNSPECIFIED
        )
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}
