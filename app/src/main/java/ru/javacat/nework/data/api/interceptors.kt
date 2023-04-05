package ru.javacat.nework.data.api

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.javacat.nework.BuildConfig
import ru.javacat.nework.data.auth.AppAuth


//
//fun authInterceptor(auth: AppAuth) = fun(chain: Interceptor.Chain): Response {
//    auth.authStateFlow.value.token?.let { token ->
//        val newRequest = chain.request().newBuilder()
//            .addHeader("Authorization", token)
//            .build()
//        return chain.proceed(newRequest)
//    }
//
//    return chain.proceed(chain.request())
//}