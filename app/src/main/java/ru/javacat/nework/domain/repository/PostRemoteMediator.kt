package ru.javacat.nework.domain.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.*
import androidx.room.withTransaction
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.api.PostsApi
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.entity.PostRemoteKeyEntity
import ru.javacat.nework.data.toModel
import ru.javacat.nework.error.ApiError
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostsApi,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id != null) {
                        apiService.getAfter(id, state.config.pageSize)
                    } else
                        apiService.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(
                        false
                    )
                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        true
                    )
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
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
                        }
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }
                    else -> Unit
                }
                //val nextKey = if (body.isEmpty()) null else body.last().id
//                body.map {
//                    it.savedOnServer = true
//                }
                postDao.insert(body.map { it.toModel() }.map(PostEntity::fromDto))
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }


}