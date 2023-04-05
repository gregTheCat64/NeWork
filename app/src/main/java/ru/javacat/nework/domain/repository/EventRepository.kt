package ru.javacat.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.dto.request.EventCreateRequest
import ru.javacat.nework.domain.model.EventModel

interface EventRepository {
    val eventData: Flow<List<EventModel>>
    suspend fun getAll()
    suspend fun getById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun create(event: EventCreateRequest)

}