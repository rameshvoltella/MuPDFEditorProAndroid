package com.artifex.mupdfdemo

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.coroutines.flow.Flow

fun showPopupAtCenterTopOfRect(context: Context, rectF: RectF, parentView: View, selectionListener: SelectionListener) {
    // Create the content of the popup
    val inflater = LayoutInflater.from(context)
    val popupView = inflater.inflate(R.layout.popup_layout, null) // Inflate your custom layout
    val screenOffset = DensityUtil.dpToPx(context, 38f)//You can change to Reposition the popup
    val deleteButton = popupView.findViewById<AppCompatTextView>(R.id.deleteButton)
    // Create a PopupWindow with the specified view
    val popupWindow = PopupWindow(popupView, WRAP_CONTENT, WRAP_CONTENT, true)

    // Measure the popup content size
    popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

    // Calculate the x and y offsets to center the popup at the top of the rectF
    val xOffset = (rectF.centerX() - (popupView.measuredWidth / 2)).toInt()
    val yOffset = (rectF.top - popupView.measuredHeight).toInt() + screenOffset
    deleteButton.setOnClickListener {
        selectionListener.onDeleteClicked()
        popupWindow.dismiss()
    }

    // Now show the popup at the calculated position
    popupWindow.showAtLocation(parentView, 0, xOffset, yOffset) // 0 for NO_GRAVITY
}



