package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.data.api.PostsApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.UserApi
import ru.javacat.nework.data.dao.UserDao
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.domain.model.PhotoModel
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.data.impl.PostRepositoryImpl
import ru.javacat.nework.data.impl.UserRepositoryImpl
import ru.javacat.nework.domain.repository.UserRepository
import ru.javacat.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val noPhoto = PhotoModel()

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    userDao: UserDao,
    apiService: UserApi,
    appAuth: AppAuth,
) : ViewModel() {
    private val repository: UserRepository =
        UserRepositoryImpl(userDao, apiService, appAuth)

    private val _tokenReceived = SingleLiveEvent<Int>()
    val tokenReceived: LiveData<Int>
        get() = _tokenReceived

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    fun registerUser(login: String, pass: String, name: String){
        viewModelScope.launch {
            try {
                repository.registerUser(
                    login,
                    pass,
                    name,
                    _photo.value?.uri?.let { MediaUpload(it.toFile()) })
                _tokenReceived.value = 0
            } catch (e: Exception){
                _tokenReceived.value = -1
            }

        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}