package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import ru.javacat.nework.domain.repository.WallRepository
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
) : ViewModel() {
    val data: Flow<PagingData<PostModel>> =
        postRepository.data.cachedIn(viewModelScope)


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



//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }

//    private val _isNewPost = MutableLiveData<Boolean>(true)
//    val isNewPost: LiveData<Boolean>
//        get() = _isNewPost


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
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                //postRepository.getAll()
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }



//    fun loadPostsByAuthorId(authorId: Long) = viewModelScope.launch {
//        try {
//            _state.value = FeedModelState(loading = true)
//            _userPosts.postValue(postRepository.getPostsByAuthorId(authorId))
//            _state.value = FeedModelState(idle = true)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            _state.value = FeedModelState(error = true)
//        }
//    }
//
//    fun updatePostsByAuthorId(authorId: Long) = viewModelScope.launch {
//        try {
//            _state.value = FeedModelState(loading = true)
//            _userPosts.postValue(postRepository.updatePostsByAuthorId(authorId))
//            _state.value = FeedModelState(idle = true)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            _state.value = FeedModelState(error = true)
//        }
//    }


    fun refresh() {
        viewModelScope.launch {

            try {
                _state.value = FeedModelState(loading = true)
                postRepository.getLatest(count)
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
                    _state.value = FeedModelState(idle = true)
                    _edited.postValue(empty)
                    _attachFile.postValue(noAttach)
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
        }
    }



    fun setNewAttach(uri: Uri?, type: AttachmentType?){
        _state.value = FeedModelState(loading = true)
        _edited.value = _edited.value?.copy(attachment = null)
        _attachFile.value = AttachModel(uri, type)
        _state.value = FeedModelState(idle = true)
    }



    fun changeContent(content: String) {
        val text = content.trim()
        _edited.value = edited.value?.copy(content = text)
    }

    fun changeLink(link: String){
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
        } catch (e:java.lang.Exception){
            _state.value = FeedModelState(error = true)
        }

        //_addedUsersIds.value = post.mentionIds
        println("POST2: ${_edited.value!!.content}")
    }

    fun clearEdit(){
        _attachFile.value = noAttach
        _edited.value = empty
    }


    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                postRepository.likeById(id)
            } catch (e:java.lang.Exception){
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                postRepository.removeById(id)
            } catch (e:java.lang.Exception){
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun setMentions(list: List<Long>){
        _edited.value = edited.value?.copy(mentionIds = list)
    }

    fun getCoordinates(lat: Double, long: Double){
        _coords.value = CoordinatesModel(lat, long)
    }

    fun setCoordinates() {
        _edited.value = edited.value?.copy(coords = coords.value)
    }

    fun clearCoordinates(){
        _edited.value = edited.value?.copy(coords = null)
    }

    fun setState(state: FeedModelState){
        _state.value = state
    }

//    fun setNew(){
//        _isNewPost.value = true
//    }
//
//    fun setOld(){
//        _isNewPost.value = false
//    }



}