package ru.javacat.nework.data.impl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.EventsApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.EventDao
import ru.javacat.nework.data.dao.EventRemoteKeyDao
import ru.javacat.nework.data.dto.Media
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.dto.request.EventCreateRequest
import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.entity.EventEntity
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.entity.toDto
import ru.javacat.nework.data.mappers.toEventEntity
import ru.javacat.nework.data.mappers.toEventModel
import ru.javacat.nework.data.mappers.toModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.EventRemoteMediator
import ru.javacat.nework.domain.repository.EventRepository
import ru.javacat.nework.domain.repository.PostRemoteMediator
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventsApi: EventsApi,
    eventRemoteKeyDao: EventRemoteKeyDao,
    private val appAuth: AppAuth,
    appDb: AppDb
) : EventRepository {
    //    override val eventData = eventDao.getAll()
//        .map(List<EventEntity>::toDto)
//        .flowOn(Dispatchers.Default)
    @OptIn(ExperimentalPagingApi::class)
    override val eventData: Flow<PagingData<EventModel>> = Pager(
        config = PagingConfig(pageSize = 5),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(
            eventsApi,
            eventDao,
            eventRemoteKeyDao,
            appDb
        )
    ).flow
        .map { it.map(EventEntity::toDto) }

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

    override suspend fun getEventsByAuthorId(authorId: Long): List<EventModel> {
        val daoResult = eventDao.getByAuthorId(authorId)
        return daoResult.toDto()
    }

    override suspend fun updateEventsByAuthorId(authorId: Long): List<EventModel>? {
        try {
            val response = eventsApi.getAll().body()?.filter {
                it.authorId == authorId
            }
            if (response != null) {
                eventDao.insert(response.map { it.toEventEntity() })
            }
            return response?.map { it.toEventModel() }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
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

    override suspend fun save(event: EventCreateRequest, upload: MediaUpload?, type: AttachmentType?) {
        try {
            val eventWithAttachment = upload?.let {
                upload(it)
            }?.let {
                event.copy(attachment = type?.name?.let { type-> Attachment(it.url, type) })
            }
            eventsApi.create(eventWithAttachment?:event)
        } catch (e:IOException){
            throw NetworkError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            val response =eventsApi.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}