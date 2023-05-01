package ru.javacat.nework.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.EventsApi
import ru.javacat.nework.data.dao.EventDao
import ru.javacat.nework.data.dao.EventRemoteKeyDao
import ru.javacat.nework.data.entity.EventEntity
import ru.javacat.nework.data.entity.EventRemoteKeyEntity
import ru.javacat.nework.data.mappers.toEntity
import ru.javacat.nework.data.mappers.toEventEntity
import ru.javacat.nework.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val apiService: EventsApi,
    private val dao: EventDao,
    private val keyDao: EventRemoteKeyDao,
    private val appDb: AppDb
): RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(
                        false
                    )
                }
                LoadType.APPEND -> {
                    val id = keyDao.min() ?: return MediatorResult.Success(
                        true
                    )
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
            //println("RESPONSE_BODY: ${body}")

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        keyDao.clear()
                        keyDao.insert(
                            listOf(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                ),
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        )

                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            keyDao.insert(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }
                    else -> Unit
                }

                dao.insert(
                    body.map { it.toEventEntity() }
                )
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}