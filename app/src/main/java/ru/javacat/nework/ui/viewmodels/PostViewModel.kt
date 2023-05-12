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
import ru.javacat.nework.domain.model.AttachmentModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.domain.repository.UserRepository
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
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val data: Flow<PagingData<PostModel>> = postRepository.data


    private var _userPosts = MutableLiveData<List<PostModel>>()
    val userPosts: LiveData<List<PostModel>>
        get() = _userPosts


    private val _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData(CoordinatesModel(0.0, 0.0))
    val coords: LiveData<CoordinatesModel>
        get() = _coords

    private var _addedUsersIds = MutableLiveData(emptyList<Long>())
    val addedUsersIds: LiveData<List<Long>>
        get() = _addedUsersIds

    private val _addedUsers = MutableLiveData<List<User>>(emptyList())
    val addedUsers: LiveData<List<User>>
        get() = _addedUsers


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
        println("ПОСТ: ${_edited.value?.content}")
        //loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                postRepository.getAll()
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun loadPostsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            _userPosts.postValue(postRepository.getPostsByAuthorId(authorId))
            _state.value = FeedModelState(idle = true)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = FeedModelState(error = true)
        }
    }

    fun updatePostsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _state.value = FeedModelState(loading = true)
            _userPosts.postValue(postRepository.updatePostsByAuthorId(authorId))
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
                postRepository.data.collectLatest { }
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
                    it.coords = coords.value
                    postRepository.save(
                        it.toPostRequest(), _attachFile.value?.uri?.let {
                            MediaUpload(it.toFile())
                        }, type
                    )
                    _addedUsers.postValue(emptyList())
                    _edited.postValue(empty)

                    _postCreated.postValue(Unit)
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }

//        _attachFile.value = noAttach
//        _mentionIds.value = emptyList()
    }

    fun getUsersById(list: List<Long>) {
        viewModelScope.launch {
            _addedUsers.postValue(userRepository.getUsersById(list) as List<User>?)
        }
    }

    fun setAddedUsersIds(list: List<Long>) {
        _addedUsersIds.value = list
    }

    fun changeAttach(uri: Uri?, type: AttachmentType?) {
        //_attachFile.value = AttachModel(uri, type)
        _edited.value = edited.value?.copy(attachment = type?.let {
            AttachmentModel(uri.toString(),
                it
            )
        })
    }

    fun changeContent(content: String) {
        val text = content.trim()

//        if (edited.value?.content == text) {
//            return
//        }
        _edited.value = edited.value?.copy(content = text)
    }

    fun deleteAttechment() {
        _attachFile.value = noAttach
        _edited.value?.attachment = null
    }

    fun edit(post: PostModel) {
        _edited.value = post
        _addedUsersIds.value = post.mentionIds
        println("POST2: ${_edited.value!!.content}")
    }

    fun clearEdit(){
        _edited.value = empty
        _addedUsers.value = emptyList()
    }



    fun likeById(id: Long) {
        viewModelScope.launch { postRepository.likeById(id) }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            postRepository.removeById(id)
        }
    }

    fun setCoordinates(lat: Double, long: Double) {
        _coords.value = CoordinatesModel(lat, long)
    }



}