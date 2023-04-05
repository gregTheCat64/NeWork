package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.*
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    auth: AppAuth
) : ViewModel() {
    val data: LiveData<List<EventModel>> = auth
        .authStateFlow
        .flatMapLatest { (myId, _)->
            repository.eventData
                .map {events->
                    events.map {
                        it.copy(ownedByMe = it.authorId == myId)
                    }
                }
        }.asLiveData(Dispatchers.Default)


    private val _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                repository.getAll()
                _state.value = FeedModelState()
            } catch (e:Exception){
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun likeById(id: Long) {
            viewModelScope.launch { repository.likeById(id) }

    }


}