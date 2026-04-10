package com.example.mediaplayback.di

import android.content.Context
import com.example.mediaplayback.manager.MediaPlaybackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaPlaybackModule {

    @Provides
    @Singleton
    fun provideMediaPlaybackManager(
        @ApplicationContext context: Context
    ) = MediaPlaybackManager(context = context)
}