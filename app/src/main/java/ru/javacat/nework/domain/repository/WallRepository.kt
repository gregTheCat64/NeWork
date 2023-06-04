package ru.javacat.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.domain.model.PostModel

interface WallRepository {
    suspend fun getAll()

    suspend fun getLatest(id: Long): Flow<PagingData<PostModel>>

    suspend fun getUserJob(id: Long): String?

    suspend fun getPostsCount(id: Long): Int?


}