package ru.javacat.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.javacat.nework.data.dto.Media
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.dto.request.EventCreateRequest
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel

interface EventRepository {
    val eventData: Flow<PagingData<EventModel>>
    suspend fun getAll()

    suspend fun getUserEvents(id: Long): Flow<PagingData<EventModel>>

    suspend fun getLatest(count: Int)
//    suspend fun getEventsByAuthorId(authorId: Long): List<EventModel>
//    suspend fun updateEventsByAuthorId(authorId: Long):List<EventModel>?
    suspend fun getById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun save(event: EventCreateRequest, upload: MediaUpload?, type: AttachmentType?)

    suspend fun createParticipant(event: EventModel)

    suspend fun upload(upload: MediaUpload): Media

}