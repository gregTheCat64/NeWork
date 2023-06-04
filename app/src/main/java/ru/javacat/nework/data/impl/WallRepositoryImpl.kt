package ru.javacat.nework.data.impl

import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.WallApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.WallRemoteKeyDao
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.mappers.toEntity
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.PostRemoteMediator
import ru.javacat.nework.domain.repository.WallRemoteMediator
import ru.javacat.nework.domain.repository.WallRepository
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.AppError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class WallRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val api: WallApi,
    private val keyDao: WallRemoteKeyDao,
    private val appDb: AppDb,
    //private val userId: Long
) : WallRepository {

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getLatest(id: Long): Flow<PagingData<PostModel>>{
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { dao.getWallPagingSource(id) },
            remoteMediator = WallRemoteMediator(
                api,
                dao,
                keyDao,
                appDb,
                id
            )
        ).flow
            .map { it.map(PostEntity::toDto) }
    }

    override suspend fun getUserJob(id: Long): String? {
        return dao.getByAuthorId(id).last().authorJob
    }


    override suspend fun getAll() {

    }

    override suspend fun getPostsCount(id: Long): Int? {
         return  api.getWall(id).body()?.size
    }

}