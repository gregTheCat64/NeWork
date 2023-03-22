package ru.javacat.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.javacat.nework.BuildConfig
import ru.javacat.nework.data.api.*
import ru.javacat.nework.data.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideApiService(auth: AppAuth): PostsApi {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(PostsApi::class.java)
    }

}