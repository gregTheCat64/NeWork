package ru.javacat.nework.data.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.javacat.nework.data.api.EventsApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.EventDao
import ru.javacat.nework.data.dto.request.EventCreateRequest
import ru.javacat.nework.data.entity.EventEntity
import ru.javacat.nework.data.entity.toDto
import ru.javacat.nework.data.mappers.toEventEntity
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.repository.EventRepository
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventsApi: EventsApi,
    private val appAuth: AppAuth
) : EventRepository {
    override val eventData = eventDao.getAll()
        .map(List<EventEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = eventsApi.getAll()
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val result = body.map { it.toEventEntity() }
            eventDao.insert(result)

        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError

        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getEventsByAuthorId(authorId: Long):List<EventModel>  {
        val daoResult = eventDao.getByAuthorId(authorId)
        return daoResult.toDto()
    }

    override suspend fun removeById(id: Long) {
        try {
            eventDao.removeById(id)
            eventsApi.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        val authorId = appAuth.authStateFlow.value.id
        val currentPost = eventDao.getById(id)
        var likeOwners = currentPost.likeOwnerIds
        if (currentPost.likedByMe == true) {
            likeOwners = likeOwners?.minusElement(authorId)
            eventDao.likeById(id, likeOwners)
            eventsApi.dislikeById(id)
        } else {
            likeOwners = likeOwners?.plusElement(authorId)
            eventDao.likeById(id, likeOwners)
            eventsApi.likeById(id)
        }
    }

    override suspend fun create(event: EventCreateRequest) {
        TODO("Not yet implemented")
    }
}