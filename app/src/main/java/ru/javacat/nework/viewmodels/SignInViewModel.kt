package ru.javacat.nework.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.api.PostsApiService
import ru.javacat.nework.auth.AppAuth
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.db.AppDb
import ru.javacat.nework.repository.PostRepository
import ru.javacat.nework.repository.PostRepositoryImpl
import ru.javacat.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    postDao: PostDao,
    apiService: PostsApiService,
    appAuth: AppAuth,
    appDb: AppDb
) : ViewModel() {
    private val repository: PostRepository =
        PostRepositoryImpl(postDao,apiService,appAuth, appDb)

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