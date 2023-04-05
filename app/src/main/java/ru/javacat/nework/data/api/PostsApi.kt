package ru.javacat.nework.data.api

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.javacat.nework.BuildConfig
import ru.javacat.nework.data.auth.Token
import ru.javacat.nework.data.dto.*
import ru.javacat.nework.data.dto.request.PostRequest
import ru.javacat.nework.data.dto.response.PostResponse




interface PostsApi {

    @GET("posts")
    suspend fun getAll(): Response<List<PostResponse>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<PostResponse>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<PostResponse>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<PostResponse>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<PostResponse>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<PostResponse>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<PostResponse>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<PostResponse>

    @POST("posts")
    suspend fun save(@Body postRequest: PostRequest): Response<PostResponse>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") pass: String
    ): Response<Token>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("password") pass: String,
        @Field("name") name: String
    ): Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithPhoto(
        @Part("login") login: String,
        @Part("password") pass: String,
        @Part("name") name: String,
        @Part media: MultipartBody.Part,
    ): Response<Token>
}
