package com.artifex.mupdfdemo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Coordinates(
    var startX: Double,
    var startY: Double,
    var endX: Double,
    var endY: Double,
) : Parcelable
