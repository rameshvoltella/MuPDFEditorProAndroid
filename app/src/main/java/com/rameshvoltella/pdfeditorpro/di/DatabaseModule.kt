package com.rameshvoltella.pdfeditorpro.di

import android.content.Context
import androidx.room.Room
import com.rameshvoltella.pdfeditorpro.database.PdfDatabase
import com.rameshvoltella.pdfeditorpro.database.dao.PdfAnnotationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PdfDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PdfDatabase::class.java,
            "pdf_editor_pro_database"
        ).build()
    }

    @Provides
    fun providePdfAnnotationDao(database: PdfDatabase): PdfAnnotationDao {
        return database.pdfAnnotationDao()
    }
}
