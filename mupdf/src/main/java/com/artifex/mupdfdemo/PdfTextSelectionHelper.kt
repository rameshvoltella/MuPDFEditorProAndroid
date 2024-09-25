package com.artifex.mupdfdemo


import android.graphics.*

class PdfTextSelectionHelper {
    private val selectedTextPaint = Paint().apply {
        this.color = Color.parseColor("#FA5D3B")
        this.alpha = 50
        this.style = Paint.Style.FILL
        this.isAntiAlias = true
    }
    private val selectionHandleColor = Paint().apply {
        this.color = Color.parseColor("#FA5D3B")
        this.style = Paint.Style.FILL_AND_STROKE
        this.isAntiAlias = true
    }
    private val selectionHandleColor2 = Paint().apply {
        this.color = Color.TRANSPARENT
        this.style = Paint.Style.FILL_AND_STROKE
        this.isAntiAlias = true
    }
    val strokeColorRed = Paint().apply {
        this.color = Color.parseColor("#ff4185")
        this.style = Paint.Style.STROKE
        this.strokeWidth = 1f
        this.isAntiAlias = true
        this.textSize = 14f
    }
    val strokeColorGreen = Paint().apply {
        this.color = Color.GREEN
        this.style = Paint.Style.STROKE
        this.strokeWidth = 1f
        this.isAntiAlias = true
    }

    // Handle
    val handleRoundRadius = 20f

    public fun drawStartHandle(canvas: Canvas, x: Float, y: Float, zoom: Float) {
        val verticalOffset = 10f  // Adjust this value to move the drawing down as needed
        val mR = handleRoundRadius * zoom
        val mX = x * zoom
        val mY = (y + mR) * zoom  // Add verticalOffset to move it down
        canvas.drawCircle(mX - mR, mY + mR, mR, selectionHandleColor)
        val path = Path()
        path.moveTo(mX, mY)
        path.lineTo(mX - mR, mY)
        path.lineTo(mX, mY + mR)
        path.close()
        canvas.drawPath(path, selectionHandleColor)
    }
    public fun drawEndHandle(canvas: Canvas, x: Float, y: Float, zoom: Float) {
        val mR = handleRoundRadius * zoom
        val mX = x * zoom
        val mY = (y+mR) * zoom
        canvas.drawCircle(mX + mR, mY + mR, mR, selectionHandleColor)
        val path = Path()
        path.moveTo(mX, mY)
        path.lineTo(mX + mR, mY)
        path.lineTo(mX, mY + mR)
        path.close()
        canvas.drawPath(path, selectionHandleColor)
    }


}
