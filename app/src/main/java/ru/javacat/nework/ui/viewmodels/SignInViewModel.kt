package ru.javacat.nework.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.api.UserApi
import ru.javacat.nework.data.dao.ProfileDao
import ru.javacat.nework.data.dao.UserDao
import ru.javacat.nework.data.entity.toEntity
import ru.javacat.nework.data.impl.ProfileRepositoryImpl
import ru.javacat.nework.data.impl.UserRepositoryImpl
import ru.javacat.nework.domain.repository.ProfileRepository
import ru.javacat.nework.domain.repository.UserRepository
import ru.javacat.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    userDao: UserDao,
    apiService: UserApi,
    appAuth: AppAuth,
    profileDao: ProfileDao
) : ViewModel() {
    private val userRepository: UserRepository =
        UserRepositoryImpl(userDao, apiService, appAuth)

    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(profileDao, appAuth)

    private val _tokenReceived = SingleLiveEvent<Int>()
    val tokenReceived: LiveData<Int>
        get() = _tokenReceived

    private val _favList = MutableLiveData<List<Long>>()
    val favList: LiveData<List<Long>>
        get() = _favList

    fun updateUser(login: String, pass: String){
        viewModelScope.launch {
            try {
                userRepository.updateUser(login,pass)
                _tokenReceived.value = 0
            } catch (e: Exception){
                _tokenReceived.value = -1
            }

        }
    }


}