package com.rameshvoltella.pdfeditorpro.database


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.graphics.PointF // Import based on your project setup

@Entity(tableName = "pdf_draw_annotations")
data class PdfDrawAnnotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfname: String,
    val pagenumber: Int,
    val type: String,
    val color: Int,
    var quadPoints: String // Store as JSON string for PointF[][]
) {
    // Convert the PointF[][] to a JSON string
    fun setQuadPoints(points: Array<Array<PointF>>) {
        val gson = Gson()
        this.quadPoints = gson.toJson(points)
    }

    // Convert the JSON string back to PointF[][]
    fun getQuadPoints(): Array<Array<PointF>> {
        val gson = Gson()
        val type = object : TypeToken<Array<Array<PointF>>>() {}.type
        return gson.fromJson(quadPoints, type)
    }
}

