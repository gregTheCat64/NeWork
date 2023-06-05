package ru.javacat.nework.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.WallRepository
import javax.inject.Inject

@HiltViewModel
class WallViewModel @Inject constructor(
    private val repository: WallRepository
) : ViewModel() {

    private val _userJob = MutableLiveData<String>()
    val userJob: LiveData<String>
        get() = _userJob

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


    suspend fun getUserPosts(id: Long): Flow<PagingData<PostModel>> {
        return repository.getLatest(id)
    }

     fun getUserJob(id: Long) {
         viewModelScope.launch {
             try {
                 Log.i("GETTING_JOB","try block in VM")
                 _userJob.postValue(repository.getUserJob(id))
             } catch (e: Exception) {
                 e.printStackTrace()
                 Log.i("GETTING_JOB","error catched in VM")
                 _dataState.value = FeedModelState(error = true)
             }

         }
    }

    suspend fun getPostsCount(id: Long): Int? =
        repository.getPostsCount(id)

}