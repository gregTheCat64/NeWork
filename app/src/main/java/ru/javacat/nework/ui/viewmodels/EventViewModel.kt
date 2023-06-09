package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yandex.mapkit.geometry.Point
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
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.EventRepository
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.util.SingleLiveEvent
import ru.javacat.nework.util.toFile
import ru.javacat.nework.util.toLocalDateTimeWhithoutZone
import java.lang.Error
import javax.inject.Inject

private val emptyEvent = EventModel(
    0, 0L, "", null, null, "", null,
    null, null, EventType.OFFLINE, emptyList(), false,
    emptyList(), emptyList(), false, null, false, null,
    false, emptyMap()
)

private val noAttach = AttachModel()
private val noPoint = null


@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    val data: Flow<PagingData<EventModel>> = repository.eventData.cachedIn(viewModelScope)

    val count = 5

//    private val _userEvents = MutableLiveData<List<EventModel>>()
//    val userEvents: LiveData<List<EventModel>>
//        get() = _userEvents

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

    private val _point = MutableLiveData<DoubleArray?>()
    val point: LiveData<DoubleArray?>
        get() = _point


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

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            try {
                repository.getLatest(count)
                _state.value = FeedModelState(idle = true)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    suspend fun getUserEvents(id: Long): Flow<PagingData<EventModel>> {
        return repository.getUserEvents(id).cachedIn(viewModelScope)
    }


    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
            } catch (e: Exception) {
                println("worked in VM EXCEPTION $e")
                _state.value = FeedModelState(error = true)
            }
        }
    }


    fun setNewAttach(uri: Uri?, type: AttachmentType?) {
        _state.value = FeedModelState(loading = true)
        _edited.value = _edited.value?.copy(attachment = null)
        _attachFile.value = AttachModel(uri, type)
        _state.value = FeedModelState(idle = true)
    }

    fun changeContent(content: String) {
        _edited.value = edited.value?.copy(content = content)
    }

    fun setStartDateTime(dateTime: String) {
        _edited.value = edited.value?.copy(datetime = dateTime.toLocalDateTimeWhithoutZone())
    }

    fun setSpeakers(list: List<Long>) {
        _edited.value = edited.value?.copy(speakerIds = list)
    }


    fun setType(type: EventType) {
        _edited.value = edited.value?.copy(type = type)
    }

    fun setLink(link: String) {
        _edited.value = edited.value?.copy(link = link)
    }

    fun setPoint(point: DoubleArray) {
        _point.value = point
        _edited.value = edited.value?.copy(
            type = EventType.OFFLINE,
            coords = CoordinatesModel(point[0], point[1])
        )
    }

    fun deleteAttachment() {
        _edited.value = _edited.value?.copy(attachment = null)
        _attachFile.value = noAttach
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    _state.value = FeedModelState(loading = true)
                    repository.save(
                        it.toEventRequest(), attachFile.value?.uri?.let {
                            MediaUpload(it.toFile())
                        }, attachFile.value?.type
                    )
                    _edited.postValue(emptyEvent)
                    _attachFile.postValue(noAttach)
                    _point.postValue(noPoint)
                    _postCreated.postValue(Unit)
                    _state.value = FeedModelState(idle = true)
                } catch (e: Exception) {
                    println("IN VM ERROR")
                    _state.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun edit(event: EventModel) {
        _edited.value = event
    }

    fun takePart(event: EventModel) {
        viewModelScope.launch {
            repository.createParticipant(event)
        }
    }

    fun clearEdit() {
        _attachFile.value = noAttach
        _point.value = noPoint
        _edited.value = emptyEvent
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            repository.removeById(id)
        }
    }

    fun setState(state: FeedModelState) {
        _state.value = state
    }

}