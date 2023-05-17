package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.mappers.toEventRequest
import ru.javacat.nework.domain.model.AttachModel
import ru.javacat.nework.domain.model.AttachmentModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.EventRepository
import ru.javacat.nework.util.SingleLiveEvent
import ru.javacat.nework.util.toFile
import ru.javacat.nework.util.toLocalDateTimeWhithoutZone
import javax.inject.Inject

private val emptyEvent = EventModel(
    0, 0L, "", null,null, "",null,
    null, null, EventType.ONLINE, emptyList(), false,
    emptyList(), emptyList(), false, null, null,
    false, emptyMap()
)

private val noAttach = AttachModel()


@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {
//    val data: LiveData<List<EventModel>> = auth
//        .authStateFlow
//        .flatMapLatest { (myId, _) ->
//            repository.eventData
//                .map { events ->
//                    events.map {
//                        it.copy(ownedByMe = it.authorId == myId)
//                    }
//                }
//        }.asLiveData(Dispatchers.Default)

    val data: Flow<PagingData<EventModel>> = repository.eventData

    private val _userEvents = MutableLiveData<List<EventModel>>()
    val userEvents: LiveData<List<EventModel>>
        get() = _userEvents

    private val _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    private val _edited = MutableLiveData(emptyEvent)
    val edited: LiveData<EventModel>
        get() = _edited

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _attachFile = MutableLiveData(noAttach)
    val attachFile: LiveData<AttachModel>
        get() = _attachFile


//    //участники:
//    private val _participateIds = MutableLiveData(emptyList<Long>())
//    val participateIds: LiveData<List<Long>>
//        get() = _participateIds
//
//    private var _participateAdded = MutableLiveData<List<User>>()
//    val participateAdded: LiveData<List<User>>
//        get() = _participateAdded
//
//    //спикеры:
//    private val _speakerIds = MutableLiveData(emptyList<Long>())
//    val speakerIds: LiveData<List<Long>>
//        get() = _speakerIds
//
//    private var _speakerAdded = MutableLiveData<String>()
//    val speakerAdded: LiveData<String>
//        get() = _speakerAdded

    init {
        //loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                //repository.getAll()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun getByAuthorId(id: Long) {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            _userEvents.postValue(repository.getEventsByAuthorId(id))
            _state.value = FeedModelState(idle = true)
            //println("SPEAKER= ${_dataByAuthor.value.toString()}")
        }
    }

    fun updateEventsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _userEvents.postValue(repository.updateEventsByAuthorId(authorId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun likeById(id: Long) {
        viewModelScope.launch { repository.likeById(id) }


    }

    fun changeAttach(uri: Uri?, type: AttachmentType?) {
        _state.value = FeedModelState(loading = true)
        _edited.value = edited.value?.copy(attachment = type?.let {
            AttachmentModel(uri.toString(),
                it
            )
        })
        _state.value = FeedModelState(idle = true)
    }

    fun changeContent(content: String) {
        _edited.value = edited.value?.copy(content = content)
    }

    fun setStartDateTime(dateTime: String){
        _edited.value = edited.value?.copy(datetime = dateTime.toLocalDateTimeWhithoutZone())
    }

    fun setLink(link: String){
        _edited.value = edited.value?.copy(link = link)
    }

    fun setSpeakers(list: List<Long>){
        _edited.value = edited.value?.copy(speakerIds = list)
    }

    fun setParticipants(list: List<Long>){
        _edited.value = edited.value?.copy(participantsIds = list)
    }

    fun deleteAttachment(){
        _edited.value = _edited.value?.copy(attachment = null)
    }

    fun save(type: AttachmentType?){
        edited.value?.let {
            viewModelScope.launch {
                try {
                    _state.value = FeedModelState(loading = true)
                    repository.save(
                        it.toEventRequest(), _attachFile.value?.uri?.let {
                            MediaUpload(it.toFile())
                        } , type
                    )
                    _edited.postValue(emptyEvent)
                    _postCreated.postValue(Unit)
                    _state.value = FeedModelState(idle = true)
                }catch (e: java.lang.Exception){
                    _state.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun edit(event: EventModel){
        _edited.value = event
    }

    fun clearEdit(){
        _edited.value = emptyEvent
    }

    fun removeById(id: Long){
        viewModelScope.launch {
            repository.removeById(id)
        }
    }

    fun setState(state: FeedModelState){
        _state.value = state
    }




//    fun setParticipantIds(list: List<Long>) {
//        //_participateIds.value = list
//    }
//
//    fun setSpeakerIds(list: List<Long>) {
//        //_speakerIds.value = list
//    }
//
//    fun setParticipantAdded(string: String) {
//        //_participateAdded.value = string
//    }
//
//    fun setSpeakerAdded(string: String) {
//        //_speakerAdded.value = string
//    }
}