package com.rameshvoltella.pdfeditorpro.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.graphics.PointF

@Entity(tableName = "pdf_annotations")
data class PdfAnnotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pdfname: String,
    val pagenumber: Int,
    @TypeConverters(PointFConverter::class) val quadPoints: List<PointF>,  // Store PointF as a list
    val type: String,
    val color: Int
)