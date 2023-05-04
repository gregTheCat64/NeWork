package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.mappers.toPostRequest
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PhotoModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = PostModel(
    0, 0L, "", null, null, "", null,
    null, null, null, emptyList(), false, false, null,
    false, false, emptyMap()
)

private val noPhoto = PhotoModel()

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    val data: Flow<PagingData<PostModel>> = repository.data


    private var _userPosts = MutableLiveData<List<PostModel>>()
    val userPosts: LiveData<List<PostModel>>
        get() = _userPosts


    private val _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData(CoordinatesModel(0.0, 0.0))
    val coords: LiveData<CoordinatesModel>
        get() = _coords

    private val _mentionIds = MutableLiveData(emptyList<Long>())
    val mentionIds: LiveData<List<Long>>
        get() = _mentionIds

    private var _usersAdded = MutableLiveData<String>()
    val usersAdded: LiveData<String>
        get() = _usersAdded


//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }


    private val _edited = MutableLiveData(empty)
    val edited: LiveData<PostModel>
        get() = _edited

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    init {
        Log.i("MYTAG", "Init postViewModel")
        println("PHOTO: ${photo.value?.uri}")
        //loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                repository.getAll()
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun loadPostsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            _userPosts.postValue(repository.getPostsByAuthorId(authorId))
            _state.value = FeedModelState(idle = true)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = FeedModelState(error = true)
        }
    }

    fun updatePostsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            _userPosts.postValue(repository.updatePostsByAuthorId(authorId))
            _state.value = FeedModelState(idle = true)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = FeedModelState(error = true)
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
                    it.mentionIds = mentionIds.value!!
                    it.coords = coords.value
                    repository.save(
                        it.toPostRequest(), _photo.value?.uri?.let {
                            MediaUpload(it.toFile())
                        }
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        _edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: PostModel) {
        //changePhoto(post.attachment?.url?.toUri())
        //changeContent(post.content)
            _edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()

        if (edited.value?.content == text) {
            return
        }
        _edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        viewModelScope.launch { repository.likeById(id) }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            repository.removeById(id)
        }
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
        println("URI: ${_photo.value!!.uri.toString()}")
        println("PHOTO after changePhotot: ${photo.value?.uri}")
    }

    fun deleteAttechment(){
        _photo.value = noPhoto
        _edited.value?.attachment = null
    }

    fun setCoordinates(lat: Double, long: Double) {
        _coords.value = CoordinatesModel(lat, long)
    }

    fun setMentionIds(list: List<Long>) {
        _mentionIds.value = list
    }

    fun setUsersAdded(usersAdded: String) {
        _usersAdded.value = usersAdded
    }


}