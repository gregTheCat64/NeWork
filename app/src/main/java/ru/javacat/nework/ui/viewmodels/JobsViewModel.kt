package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.data.mappers.toJobRequest
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.domain.repository.JobRepository
import ru.javacat.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repository: JobRepository
    ): ViewModel() {
    val data: LiveData<List<JobModel>> = repository.jobsData.asLiveData()

    private val _userJobs = MutableLiveData<List<JobModel>>()
    val userJobs: LiveData<List<JobModel>>
        get() = _userJobs

    private val _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated


    fun getJobsByUserId(id: Long){
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                _userJobs.postValue(repository.getJobsByUserId(id))
                _state.value = FeedModelState(idle = true)
            } catch (e:Exception){
                e.printStackTrace()
                _state.postValue(FeedModelState(error = true))
            }
        }
    }


    fun save(jobModel: JobModel){
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                repository.create(jobModel.toJobRequest())
                _jobCreated.postValue(Unit)
                _state.value = FeedModelState(idle = true)
            }catch (e: Exception) {
                _state.value = FeedModelState(error = true)
                e.printStackTrace()
            }
        }
    }


}

