package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.data.api.PostsApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.data.impl.PostRepositoryImpl
import ru.javacat.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    postDao: PostDao,
    apiService: PostsApi,
    postRemoteKeyDao: PostRemoteKeyDao,
    appAuth: AppAuth,
    appDb: AppDb
) : ViewModel() {
    private val repository: PostRepository =
        PostRepositoryImpl(postDao,apiService,postRemoteKeyDao, appAuth, appDb)

    private val _tokenReceived = SingleLiveEvent<Int>()
    val tokenReceived: LiveData<Int>
        get() = _tokenReceived

    fun updateUser(login: String, pass: String){
        viewModelScope.launch {
            try {
                repository.updateUser(login,pass)
                _tokenReceived.value = 0
            } catch (e: Exception){
                _tokenReceived.value = -1
            }

        }
    }
}