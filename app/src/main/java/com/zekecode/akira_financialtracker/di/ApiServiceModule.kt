package com.zekecode.akira_financialtracker.di

import com.zekecode.akira_financialtracker.data.remote.api.AlphaVentureService
import com.squareup.moshi.Moshi
import com.zekecode.akira_financialtracker.data.remote.api.GithubApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    private const val ALPHA_BASE_URL = "https://www.alphavantage.co"
    private const val GITHUB_BASE_URL = "https://api.github.com"

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Singleton
    @Provides
    @AlphaVentureRetrofit
    fun provideAlphaVentureRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ALPHA_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Singleton
    @Provides
    fun provideAlphaVentureService(@AlphaVentureRetrofit retrofit: Retrofit): AlphaVentureService {
        return retrofit.create(AlphaVentureService::class.java)
    }

    @Singleton
    @Provides
    @GitHubRetrofit
    fun provideGitHubRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Singleton
    @Provides
    fun provideGitHubApiService(@GitHubRetrofit retrofit: Retrofit): GithubApiService {
        return retrofit.create(GithubApiService::class.java)
    }
}
