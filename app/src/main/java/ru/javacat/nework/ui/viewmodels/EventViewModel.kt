package ru.javacat.nework.ui.viewmodels

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
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.repository.EventRepository
import javax.inject.Inject


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


    //участники:
    private val _participateIds = MutableLiveData(emptyList<Long>())
    val participateIds: LiveData<List<Long>>
        get() = _participateIds

    private var _participateAdded = MutableLiveData<String>()
    val participateAdded: LiveData<String>
        get() = _participateAdded

    //спикеры:
    private val _speakerIds = MutableLiveData(emptyList<Long>())
    val speakerIds: LiveData<List<Long>>
        get() = _speakerIds

    private var _speakerAdded = MutableLiveData<String>()
    val speakerAdded: LiveData<String>
        get() = _speakerAdded

    init {
        //loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                repository.getAll()
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

    fun setParticipantIds(list: List<Long>) {
        _participateIds.value = list
    }

    fun setSpeakerIds(list: List<Long>) {
        _speakerIds.value = list
    }

    fun setParticipantAdded(string: String) {
        _participateAdded.value = string
    }

    fun setSpeakerAdded(string: String) {
        _speakerAdded.value = string
    }
}