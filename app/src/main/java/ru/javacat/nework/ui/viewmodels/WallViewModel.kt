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
import ru.javacat.nework.data.entity.ProfileEntity
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.JobRepository
import ru.javacat.nework.domain.repository.ProfileRepository
import ru.javacat.nework.domain.repository.UserRepository
import ru.javacat.nework.domain.repository.WallRepository
import javax.inject.Inject

@HiltViewModel
class WallViewModel @Inject constructor(
    private val wallRepository: WallRepository,
    private val jobRepository: JobRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userJob = MutableLiveData<String>()
    val userJob: LiveData<String>
        get() = _userJob

    private val _postsSize = MutableLiveData<Int>()
    val postsSize: LiveData<Int>
        get() = _postsSize

    private val _favList = MutableLiveData<List<Long>>()
    val favList: LiveData<List<Long>>
        get() = _favList

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


    suspend fun getUserPosts(id: Long): Flow<PagingData<PostModel>> {
        return wallRepository.getLatest(id)
    }

     fun getUserJob(id: Long) {
         viewModelScope.launch {
             try {
                val result = jobRepository.getJobsByUserId(id)
                 if (!result.isNullOrEmpty()) {
                     _userJob.postValue(result.last().name)
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
                 Log.i("GETTING_JOB","error catched in VM")
                 _dataState.value = FeedModelState(error = true)
             }

         }
    }

    fun getPostsCount(id: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                _postsSize.postValue(wallRepository.getPostsCount(id))
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception){
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getFavList(profileId: Long){
        viewModelScope.launch {
            val res = profileRepository.getFavListIds(profileId)
            if (res != null) {
                _favList.postValue(res.favListIds)
            }
        }
    }

    fun addUserToFav(profileId: Long, id: Long){
        viewModelScope.launch {
//            val favs = profileRepository.getFavListIds(profileId)?.favListIds
//            val newList = favs?.plus(id)?: listOf(id)
//            profileRepository.insert(ProfileEntity(profileId, newList))
            userRepository.addToFav(id)
        }
    }

    fun deleteUserFromFav(profileId: Long, id: Long){
        viewModelScope.launch {
//            val favs = profileRepository.getFavListIds(profileId)?.favListIds
//            val newList = favs?.minus(id)?: emptyList()
//            profileRepository.insert(ProfileEntity(profileId, newList))
            userRepository.deleteFromFav(id)
        }
    }


}