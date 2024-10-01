package com.rameshvoltella.pdfeditorpro.di


import android.content.Context
import com.rameshvoltella.pdfeditorpro.utils.ContextModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideCoroutineContext(): CoroutineContext {
        return Dispatchers.IO
    }
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): ContextModule {
        return ContextModule(context)
    }

//    @Provides
//    @Singleton
//    fun provideEventBus(): SharedEventBus {
//        return SharedEventBus()
//    }
}