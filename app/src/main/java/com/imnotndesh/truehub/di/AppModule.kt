package com.imnotndesh.truehub.di

import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.SessionHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** The manager for the active session. Feature ViewModels are only built post-login. */
    @Provides
    fun provideTrueNASApiManager(): TrueNASApiManager =
        SessionHolder.current ?: error("No active TrueNAS session")
}
