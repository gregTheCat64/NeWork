package ru.javacat.nework.data.impl

import android.util.Log
import androidx.paging.*
import androidx.room.withTransaction
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.api.PostsApi
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.entity.PostRemoteKeyEntity
import ru.javacat.nework.data.mappers.toEntity
import ru.javacat.nework.error.ApiError
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostsApi,
    private val dao: PostDao,
    private val keyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        Log.i("mLOAD", loadType.name)
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                        apiService.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(
                        true
                    )
                }
                LoadType.APPEND -> {
                    val id = keyDao.min() ?: return MediatorResult.Success(
                        false
                    )
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

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
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    )
                                )
                            )
                        dao.clear()
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            keyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }
                    else -> Unit
                }

                dao.insert(
                    body.map { it.toEntity() }
                )
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}