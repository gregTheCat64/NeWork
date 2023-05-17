package ru.javacat.nework.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.javacat.nework.data.dto.Media
import ru.javacat.nework.data.dto.request.EventCreateRequest
import ru.javacat.nework.data.dto.response.EventResponse
import ru.javacat.nework.data.dto.response.PostResponse


interface EventsApi {

    @GET("events")
    suspend fun getAll(): Response<List<EventResponse>>

    @POST("events")
    suspend fun create(@Body event: EventCreateRequest): Response<EventResponse>

    @GET("events/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<EventResponse>>

    @GET("events/{event_id}")
    suspend fun getById(@Path("event_id") id:Long): Response<EventResponse>

    @DELETE("events/{event_id}")
    suspend fun removeById(@Path("event_id") id: Long)

    @GET("events/{event_id}/before")
    suspend fun getBefore(@Path("event_id")id:Long, @Query("count") count: Int): Response<List<EventResponse>>

    @GET("events/{event_id}/after")
    suspend fun getAfter(@Path("event_id")id:Long, @Query("count") count: Int): Response<List<EventResponse>>

    @POST("events/{event_id}/likes")
    suspend fun likeById(@Path("event_id") id: Long): Response<EventResponse>

    @DELETE("events/{event_id}/likes")
    suspend fun dislikeById(@Path("event_id") id: Long): Response<EventResponse>

    @GET("events/{event_id}/newer")
    suspend fun getNewer(@Path("event_id") id: Long): Response<List<EventResponse>>

    @POST("events/{event_id}/participants")
    suspend fun createParticipant(@Path("event_id") id: Long) :Response<EventResponse>

    @DELETE("events/{event_id}/participants")
    suspend fun removeParticipant(@Path("event_id") id: Long) :Response<EventResponse>

    @POST("events")
    suspend fun save(@Body eventCreateRequest: EventCreateRequest): Response<EventResponse>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>



}