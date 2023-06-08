package ru.javacat.nework.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
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
    val anon: User = User(0L, "", "", "", false, false)

    val users = userData.asLiveData()

    var newUser: User = anon

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

    fun loadUsers() = viewModelScope.launch {
        try {
            repository.getAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserById(id: Long) {
        viewModelScope.launch {
            if (id!=0L){
                _user.postValue(repository.getById(id))
            } else _user.postValue(anon)

            Log.i("mUSER", newUser.name)
        }
        //Log.i("mUSER", newUser.name)
    }

    //suspend fun getUser(id: Long): User? = repository.getById(id)

    fun getUsersById(list: List<Long>): List<User>? {
        viewModelScope.launch {
            val users = repository.getUsersById(list)
            _addedUsers.postValue(users as List<User>?)
        }
        return _addedUsers.value
    }

    fun clearUsersChecked(){
        _addedUsers.value = emptyList()
        users.value?.map {
            it.checked = false
        }
    }

//    fun addToFav(id: Long){
//        viewModelScope.launch {
//            repository.addToFav(id)
//        }
//
//    }
//
//    fun deleteFromFav(id: Long){
//        viewModelScope.launch {
//            repository.deleteFromFav(id)
//        }
//    }
}