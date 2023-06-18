package ru.javacat.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.javacat.nework.BuildConfig
import ru.javacat.nework.data.api.*
import ru.javacat.nework.data.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"
    }

    @Provides
    @Singleton
    fun provideLogging():  HttpLoggingInterceptor = HttpLoggingInterceptor().apply{
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
        }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor{chain->
    appAuth.authStateFlow.value.token?.let { token ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()
        return@addInterceptor chain.proceed(newRequest)
    }
    chain.proceed(chain.request())
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ) : PostsApi = retrofit.create()

    @Provides
    @Singleton
    fun provideEventsApiService(
        retrofit: Retrofit
    ) : EventsApi = retrofit.create()

    @Provides
    @Singleton
    fun provideUserApiService(
        retrofit: Retrofit
    ) : UserApi = retrofit.create()

    @Provides
    @Singleton
    fun provideJobsApiService(
        retrofit: Retrofit
    ) : JobsApi = retrofit.create()
    @Provides
    @Singleton
    fun provideWallApiService(
        retrofit: Retrofit
    ) : WallApi = retrofit.create()

}