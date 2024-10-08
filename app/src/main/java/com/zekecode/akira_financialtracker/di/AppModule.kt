package com.zekecode.akira_financialtracker.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        // Use "AkiraPrefs" as the name for the SharedPreferences file
        return context.getSharedPreferences("AkiraPrefs", Context.MODE_PRIVATE)
    }
}
