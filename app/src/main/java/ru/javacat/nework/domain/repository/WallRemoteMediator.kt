package ru.javacat.nework.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.WallApi
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.WallRemoteKeyDao
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.entity.WallRemoteKeyEntity
import ru.javacat.nework.data.mappers.toEntity
import ru.javacat.nework.error.ApiError
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator(
    private val api: WallApi,
    private val dao: PostDao,
    private val keyDao: WallRemoteKeyDao,
    private val appDb: AppDb
): RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    api.getMyWallLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(false)
                }
                LoadType.APPEND -> {
                    val id = keyDao.min() ?: return  MediatorResult.Success(true)
                    api.getMyWallBefore(id, state.config.pageSize)
                }
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        keyDao.clear()
                        keyDao.insert(
                            listOf(
                                WallRemoteKeyEntity(
                                    WallRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                ),
                                WallRemoteKeyEntity(
                                    WallRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        if (body.isNotEmpty()){
                            keyDao.insert(
                                WallRemoteKeyEntity(
                                    WallRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }
                    else -> Unit
                }

                dao.insert(body.map { it.toEntity() })
            }
            return  MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}