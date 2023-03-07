package ru.javacat.nework.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.javacat.nework.db.AppDb
import ru.javacat.nework.repository.PostRepository
import ru.javacat.nework.repository.PostRepositoryImpl
import ru.javacat.nework.util.SingleLiveEvent

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

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