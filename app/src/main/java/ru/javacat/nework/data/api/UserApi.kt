package ru.javacat.nework.data.api

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.javacat.nework.data.auth.Token
import ru.javacat.nework.data.dto.response.JobResponse
import ru.javacat.nework.data.dto.response.UserResponse
import ru.javacat.nework.domain.model.User

interface UserApi {

    @GET("users")
    suspend fun getAll(): Response<List<UserResponse>>

    @GET("users/{user_id}")
    suspend fun getById(@Path("user_id") id:Long):Response<UserResponse>


    @FormUrlEncoded
    @POST("users/authentication/")
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
        @Part("login") login: RequestBody,
        @Part("password") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<Token>
}