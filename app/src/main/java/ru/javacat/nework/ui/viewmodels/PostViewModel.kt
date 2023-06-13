package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.mappers.toPostRequest
import ru.javacat.nework.domain.model.AttachModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.FeedModelState
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
    private val postRepository: PostRepository,
    private val postRemoteKeyDao: PostRemoteKeyDao
) : ViewModel() {
    val data: Flow<PagingData<PostModel>> =
        postRepository.data.cachedIn(viewModelScope).flowOn(Dispatchers.IO)

    val count = 5

    private var _userPosts = MutableLiveData<List<PostModel>>()
    val userPosts: LiveData<List<PostModel>>
        get() = _userPosts


    private var _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    private val _coords = MutableLiveData(CoordinatesModel(0.0, 0.0))
    val coords: LiveData<CoordinatesModel>
        get() = _coords

    val newerCount: Flow<Int> = data.flatMapLatest {
        postRepository.getNewerCount(postRemoteKeyDao.max() ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default).asFlow()
    }


    private val _edited = MutableLiveData(empty)
    val edited: LiveData<PostModel>
        get() = _edited

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _newPost = MutableLiveData<Boolean>()
    val newPost: LiveData<Boolean>
        get() = _newPost

    private val _attachFile = MutableLiveData(noAttach)
    val attachFile: LiveData<AttachModel>
        get() = _attachFile




    fun save(type: AttachmentType?) {
        println("Edited: ${edited.value}")

        edited.value?.let {
            viewModelScope.launch {
                try {
                    _state.value = FeedModelState(loading = true)
                    //it.link = null
                    println("${attachFile.value?.uri}")
                    postRepository.create(
                        it.toPostRequest(), attachFile.value?.uri?.let {
                            MediaUpload(it.toFile())
                        }, attachFile.value?.type
                    )
                    //_addedUsers.postValue(emptyList())
                    _postCreated.postValue(Unit)
                    _newPost.postValue(true)
                    _state.value = FeedModelState(idle = true)
                    _edited.postValue(empty)
                    _attachFile.postValue(noAttach)
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
    }


    fun setNewAttach(uri: Uri?, type: AttachmentType?) {
        _state.value = FeedModelState(loading = true)
        try {
            _edited.value = _edited.value?.copy(attachment = null)
            _attachFile.value = AttachModel(uri, type)
            _state.value = FeedModelState(idle = true)
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }


    fun changeContent(content: String) {
        val text = content.trim()
        _edited.value = edited.value?.copy(content = text)
    }

    fun changeLink(link: String) {
        val link = link.trim().toString()
        _edited.value = edited.value?.copy(link = link)
    }

    fun deleteAttachment() {
        // _attachFile.value = noAttach
        //_edited.value?.attachment = null //observer не срабатывает
        _attachFile.value = noAttach
        _edited.value = _edited.value?.copy(attachment = null) //срабатывает

    }

    fun edit(post: PostModel) {
        try {
            _edited.value = post
        } catch (e: java.lang.Exception) {
            _state.value = FeedModelState(error = true)
        }
        println("POST2: ${_edited.value!!.content}")
    }

    fun clearEdit() {
        _attachFile.value = noAttach
        _edited.value = empty
    }


    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                postRepository.likeById(id)
            } catch (e: java.lang.Exception) {
                println("worked in VM EXCEPTION $e")
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                postRepository.removeById(id)
            } catch (e: java.lang.Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun setMentions(list: List<Long>) {
        _edited.value = edited.value?.copy(mentionIds = list)
    }

    fun getCoordinates(lat: Double, long: Double) {
        _coords.value = CoordinatesModel(lat, long)


    }

    fun setCoordinates() {
        try {
            _edited.value = edited.value?.copy(coords = coords.value)
        } catch (e: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }

    fun clearCoordinates() {
        _edited.value = edited.value?.copy(coords = null)
    }

    fun setState(state: FeedModelState) {
        _state.value = state
    }



}