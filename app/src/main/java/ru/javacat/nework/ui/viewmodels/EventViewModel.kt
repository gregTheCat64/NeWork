package ru.javacat.nework.ui.viewmodels

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.javacat.nework.data.dao.EventRemoteKeyDao
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.mappers.toEventRequest
import ru.javacat.nework.domain.model.AttachModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.repository.EventRepository
import ru.javacat.nework.util.SingleLiveEvent
import ru.javacat.nework.util.toLocalDateTimeWhithoutZone
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
    private val repository: EventRepository,
    private val eventRemoteKeyDao: EventRemoteKeyDao
) : ViewModel() {

    val data: Flow<PagingData<EventModel>> = repository.eventData.cachedIn(viewModelScope).flowOn(Dispatchers.IO)

    val count = 5


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

    @OptIn(ExperimentalCoroutinesApi::class)
    val newerCount: Flow<Int> = data.flatMapLatest {
        repository.getNewerCount(eventRemoteKeyDao.max() ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default).asFlow()
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




    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
            } catch (e: Exception) {
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
            try {
                repository.createParticipant(event)
            }catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun clearEdit() {
        _attachFile.value = noAttach
        _point.value = noPoint
        _edited.value = emptyEvent
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun setState(state: FeedModelState) {
        _state.value = state
    }

}