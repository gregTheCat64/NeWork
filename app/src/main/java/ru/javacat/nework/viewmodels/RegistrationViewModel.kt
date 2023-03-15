package ru.javacat.nework.viewmodels

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.api.PostsApiService
import ru.javacat.nework.auth.AppAuth
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.dao.PostRemoteKeyDao
import ru.javacat.nework.db.AppDb
import ru.javacat.nework.dto.MediaUpload
import ru.javacat.nework.model.PhotoModel
import ru.javacat.nework.repository.PostRepository
import ru.javacat.nework.repository.PostRepositoryImpl
import ru.javacat.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val noPhoto = PhotoModel()

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    postDao: PostDao,
    apiService: PostsApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appAuth: AppAuth,
    appDb: AppDb
) : ViewModel() {
    private val repository: PostRepository =
        PostRepositoryImpl(postDao, apiService,postRemoteKeyDao, appAuth, appDb)

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