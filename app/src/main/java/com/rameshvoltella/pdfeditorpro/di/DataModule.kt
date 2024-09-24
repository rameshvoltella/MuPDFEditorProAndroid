package com.rameshvoltella.pdfeditorpro.di




import com.rameshvoltella.pdfeditorpro.data.database.DatabaseRepository
import com.rameshvoltella.pdfeditorpro.data.database.DatabaseRepositorySource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Tells Dagger this is a Dagger module
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {


    @Binds
    @Singleton
    abstract fun provideDatabaseRepository(remoteRepository: DatabaseRepository): DatabaseRepositorySource

}