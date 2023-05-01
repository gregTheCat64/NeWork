package ru.javacat.nework.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.javacat.nework.data.dto.response.PostResponse

interface WallApi {

    @GET("my/wall")
    suspend fun getMyWall(): Response<List<PostResponse>>

    @GET("my/wall/latest")
    suspend fun getMyWallLatest(@Query("count") count: Int): Response<List<PostResponse>>

    @GET("my/wall/{id}/before")
    suspend fun getMyWallBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<PostResponse>>

    @GET("my/wall/{id}/after")
    suspend fun getMyWallAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<PostResponse>>
}