package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.UserRepository
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository

) : ViewModel() {
    private val userData = repository.userData

    val users = userData.asLiveData()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _addedUsers = MutableLiveData<List<User>>(emptyList())
    val addedUsers: LiveData<List<User>>
        get() = _addedUsers

    private val _speakers = MutableLiveData<List<User>>(emptyList())
    val speakers: LiveData<List<User>>
        get() = _speakers


    init {
        loadUsers()
    }

    private fun loadUsers() = viewModelScope.launch {
        try {
            repository.getAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserById(id: Long) {
        viewModelScope.launch {
            try {
                _user.postValue(repository.getById(id))
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun getUsersById(list: List<Long>) {
        viewModelScope.launch {
            val users = repository.getUsersById(list)
            _addedUsers.postValue(users as List<User>?)
        }
    }

    fun getSpeakersById(list: List<Long>) {
        viewModelScope.launch {
            val users = repository.getUsersById(list)
            _speakers.postValue(users as List<User>?)
        }
    }

}