package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.dto.response.Coordinates
import ru.javacat.nework.data.toModel
import ru.javacat.nework.data.toPostRequest
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PhotoModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = PostModel(
   0,0L,"",null,null,"",null,
    null,null,null,null,false,false,null,
    false, null
    )

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    val data: Flow<PagingData<PostModel>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    posts.map { it.copy(ownedByMe = it.authorId == myId) }
                }
        }

    private val _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
    get() = _state

    private val _coords = MutableLiveData(CoordinatesModel(0.0,0.0))
    val coords: LiveData<CoordinatesModel>
    get() = _coords

//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }


    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                //repository.getAll()
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            try {
                //repository.getAll()
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    it.link = null
                    it.mentionIds = listOf()
                    it.coords = coords.value
                    repository.save(
                        it.toPostRequest(), _photo.value?.uri?.let {
                            MediaUpload(it.toFile()) }
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: PostModel) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        viewModelScope.launch { repository.likeById(id) }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            repository.removeById(id)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun setCoordinates(lat: Double, long: Double){
        _coords.value = CoordinatesModel(lat, long)
    }
}