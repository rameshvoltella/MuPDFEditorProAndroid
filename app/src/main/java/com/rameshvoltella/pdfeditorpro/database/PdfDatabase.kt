package com.rameshvoltella.pdfeditorpro.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rameshvoltella.pdfeditorpro.database.dao.PdfAnnotationDao
import com.rameshvoltella.pdfeditorpro.database.dao.PdfDrawerAnnotationDao

/*
@Database(entities = [PdfAnnotation::class], version = 1)
@TypeConverters(PointFConverter::class)
abstract class PdfDatabase : RoomDatabase() {
    abstract fun pdfAnnotationDao(): PdfAnnotationDao
}*/

@Database(
    entities = [PdfAnnotation::class, PdfDrawAnnotation::class],
    version = 1 // Increment the version number
)
@TypeConverters(PointFConverter::class) // If you have any specific type converters
abstract class PdfDatabase : RoomDatabase() {
    abstract fun pdfAnnotationDao(): PdfAnnotationDao // Existing DAO for PdfAnnotation
    abstract fun pdfAnnotationDrawDao(): PdfDrawerAnnotationDao // New DAO for PdfAnnotationNew
}
