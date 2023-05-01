package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UsersListDialogViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>>
    get() = _users

    fun getUsersById(list: List<Long>) {
        viewModelScope.launch {
            _users.postValue(userRepository.getUsersById(list) as List<User>?)
        }
    }
}