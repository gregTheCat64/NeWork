package ru.javacat.nework.data.impl

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.WallApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.WallRemoteKeyDao
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.WallRemoteMediator
import ru.javacat.nework.domain.repository.WallRepository
import javax.inject.Inject

class WallRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val api: WallApi,
    keyDao: WallRemoteKeyDao,
    private val appAuth: AppAuth,
    appDb: AppDb,
    private val userId: Long
) : WallRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<PostModel>> = Pager(
        config = PagingConfig(5),
        pagingSourceFactory = { dao.getWallPagingSource(userId) },
        remoteMediator = WallRemoteMediator(api, dao, keyDao, appDb)
    ).flow.map { it.map (PostEntity::toDto) }

    override suspend fun getAll() {
        TODO("Not yet implemented")
    }
}