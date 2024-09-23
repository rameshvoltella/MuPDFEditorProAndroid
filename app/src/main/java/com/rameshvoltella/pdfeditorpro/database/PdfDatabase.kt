package com.rameshvoltella.pdfeditorpro.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rameshvoltella.pdfeditorpro.database.dao.PdfAnnotationDao

@Database(entities = [PdfAnnotation::class], version = 1)
@TypeConverters(PointFConverter::class)
abstract class PdfDatabase : RoomDatabase() {
    abstract fun pdfAnnotationDao(): PdfAnnotationDao
}
