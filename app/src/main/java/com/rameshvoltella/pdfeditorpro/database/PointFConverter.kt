package com.rameshvoltella.pdfeditorpro.database

import android.graphics.PointF
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PointFConverter {
    @TypeConverter
    fun fromPointFList(value: List<PointF>?): String? {
        val type = object : TypeToken<List<PointF>>() {}.type
        return Gson().toJson(value, type)
    }

    @TypeConverter
    fun toPointFList(value: String?): List<PointF>? {
        val type = object : TypeToken<List<PointF>>() {}.type
        return Gson().fromJson(value, type)
    }
}
