package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.WallRepository
import javax.inject.Inject

@HiltViewModel
class WallViewModel @Inject constructor(
    private val repository: WallRepository
) : ViewModel() {


    suspend fun getUserPosts(id: Long): Flow<PagingData<PostModel>> {
        return repository.getLatest(id)
    }

    suspend fun getUserJob(id: Long): String? =
        repository.getUserJob(id)

    suspend fun getPostsCount(id: Long): Int? =
        repository.getPostsCount(id)

}