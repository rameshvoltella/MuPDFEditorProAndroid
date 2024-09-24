package com.artifex.mupdfdemo

import android.content.Context

object DensityUtil {

    /**
     * Convert dp to pixels.
     *
     * @param context the context to get resources and device specific display metrics
     * @param dp      the value in dp to convert
     * @return the converted value in pixels
     */
    fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt() // Adding 0.5 for rounding
    }

    /**
     * Convert pixels to dp.
     *
     * @param context the context to get resources and device specific display metrics
     * @param px      the value in pixels to convert
     * @return the converted value in dp
     */
    fun pxToDp(context: Context, px: Float): Int {
        val density = context.resources.displayMetrics.density
        return (px / density + 0.5f).toInt() // Adding 0.5 for rounding
    }
}
