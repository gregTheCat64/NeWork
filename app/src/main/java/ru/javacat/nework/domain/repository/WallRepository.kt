package ru.javacat.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.domain.model.PostModel

interface WallRepository {
    val data: Flow<PagingData<PostModel>>
    suspend fun getAll()

}