package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.mappers.toPostRequest
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.AttachModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.util.SingleLiveEvent
import javax.inject.Inject

private val empty = PostModel(
    0, 0L, "", null, null, "", null,
    null, null, null, emptyList(), false, false, null,
    false, false, emptyMap()
)

private val noAttach = AttachModel()

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

    private var _mentionIds = MutableLiveData(emptyList<Long>())
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

    private val _attachFile = MutableLiveData(noAttach)
    val attachFile: LiveData<AttachModel>
        get() = _attachFile


    init {
        Log.i("MYTAG", "Init postViewModel")
        println("PHOTO: ${attachFile.value?.uri}")
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
                repository.data.collectLatest {  }
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save(type: AttachmentType?) {
        println("Edited: ${edited.value}")
        edited.value?.let {
            viewModelScope.launch {
                try {
                    it.link = null
                    it.mentionIds = mentionIds.value!!
                    it.coords = coords.value
                    repository.save(
                        it.toPostRequest(), _attachFile.value?.uri?.let {
                            MediaUpload(it.toFile())
                        }, type
                    )
                    _postCreated.value = Unit
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
        _edited.value = empty
        _attachFile.value = noAttach
        _mentionIds.value = emptyList()
    }

    fun changeAttach(uri: Uri?, type: AttachmentType?) {
        _attachFile.value = AttachModel(uri, type)
        println("URI: ${_attachFile.value!!.uri.toString()}")
        println("PHOTO after changePhotot: ${attachFile.value?.uri}")
    }

    fun deleteAttechment(){
        _attachFile.value = noAttach
        _edited.value?.attachment = null
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