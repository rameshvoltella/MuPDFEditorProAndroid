package com.rameshvoltella.pdfeditorpro.database

import android.graphics.PointF
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun getQuadPoints(quadPoints:String): Array<Array<PointF>> {
    val gson = Gson()
    val type = object : TypeToken<Array<Array<PointF>>>() {}.type
    return gson.fromJson(quadPoints, type)
}