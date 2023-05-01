package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.javacat.nework.data.mappers.toJobRequest
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.domain.repository.JobRepository
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repository: JobRepository
    ): ViewModel() {
    val data: LiveData<List<JobModel>> = repository.jobsData.asLiveData()

    private val _userJobs = MutableLiveData<List<JobModel>>()
    val userJobs: LiveData<List<JobModel>>
        get() = _userJobs


    fun getJobsByUserId(id: Long){
        viewModelScope.launch {
            try {
                _userJobs.postValue(repository.getJobsByUserId(id))
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun save(jobModel: JobModel){
        viewModelScope.launch {
            try {
                repository.create(jobModel.toJobRequest())
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
