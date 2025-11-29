package com.example.hiddencam.di

import android.content.Context
import com.example.hiddencam.data.datastore.SecurityDataStore
import com.example.hiddencam.data.repository.VideoGalleryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideSecurityDataStore(
        @ApplicationContext context: Context
    ): SecurityDataStore {
        return SecurityDataStore(context)
    }
    
    @Provides
    @Singleton
    fun provideVideoGalleryRepository(
        @ApplicationContext context: Context
    ): VideoGalleryRepository {
        return VideoGalleryRepository(context)
    }
}
