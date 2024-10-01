package com.rameshvoltella.pdfeditorpro.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.setPadding
import hearsilent.discreteslider.libs.Utils

class CustomVerticalMovableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dY = 0f
    var passProgressListener = true
    var progressMax = 100 // Max progress value (default 100 steps)
        set(value) {
            field = value
            updateProgressText(currentProgress)
        }

    private var currentProgress = 0

    var onProgressChanged: ((Int) -> Unit)? = null // Lambda callback to listen for progress changes
    var onTopReached: (() -> Unit)? = null // Callback when the top is reached
    var onBottomReached: (() -> Unit)? = null // Callback when the top is reached

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    init {
        // Add padding so the text doesn't go beyond the boundaries
        setPadding(10)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                dY = y - event.rawY
                passProgressListener=true
            }
            MotionEvent.ACTION_UP->{
                passProgressListener=true
            }

            MotionEvent.ACTION_MOVE -> {
                passProgressListener = true

                // Calculate the new Y position
                val newY = event.rawY + dY

                // Restrict the Y movement within the parent view bounds
                val parentView = parent as View
                Log.d("CKIO","newY>"+newY+"<>newY + height"+(newY + height)+"<>parentView.height<>"+parentView.height)
                if (newY >= 0 && newY + height <= parentView.height) {
                    y = newY

                    // Check if the layout has reached the topmost position


                    // Call the method to update the progress percentage
                    updateProgress(parentView.height)
                }else  if (newY <= 0f) {
                    Log.d("onTopReached","onTopReachedyo")
                    y=0f
                    updateProgress(0)
                    onTopReached?.invoke() // Notify that the top is reached
                }else if (newY + height >= parentView.height) { // Check if the bottom is reached
                    Log.d("onBottomReached", "onBottomReachedyo")
                    y = (parentView.height - height).toFloat() // Set to bottom
                    passProgressListener=false
                    setProgress(progressMax)
                    onBottomReached?.invoke() // Notify that the top is reached

//                    onBottomReached?.invoke() // Notify that the bottom is reached
                }
//
            }

            else -> return false
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the progress text in the center of the view
        val text = "$currentProgress"
        val textX = width / 2f
        val textY = height / 2f - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(text, textX, textY, paint)
    }

    private fun updateProgress(parentHeight: Int) {
        synchronized(this) {
            // Calculate the current position as a percentage of the parent height
            val currentY = y
            val progressPercentage = ((currentY / (parentHeight - height)) * progressMax).toInt()

            // Ensure the percentage is between 0 and progressMax
            currentProgress = progressPercentage.coerceIn(0, progressMax)
            if (passProgressListener) {
                // Trigger the callback with the updated progress value
                onProgressChanged?.invoke(currentProgress)
            }

            // Redraw the view to update the progress text
            invalidate()
        }
    }

    private fun updateProgressText(progress: Int) {
        currentProgress = progress
        invalidate() // Redraw the view to reflect the updated progress
    }

    /**
     * Method to set the progress by providing a percentage value
     * @param progress The desired progress percentage (0 to progressMax)
     */
    fun setProgress(progress: Int) {
        passProgressListener=false
        // Ensure progress is within the valid range
        val clampedProgress = progress.coerceIn(0, progressMax)

        // Calculate the corresponding Y position for the given progress percentage
        val parentView = parent as View
        val newY = (parentView.height - height) * (clampedProgress / progressMax.toFloat())

        // Move the view to the calculated Y position
        y = newY

        // Update the progress text and invoke the callback
        currentProgress = clampedProgress
//        if(passProgressListener) {
//            onProgressChanged?.invoke(currentProgress)
//        }

        // Redraw the view to update the progress text
        invalidate()
    }
}
