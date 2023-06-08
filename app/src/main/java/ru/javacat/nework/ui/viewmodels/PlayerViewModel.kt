package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.javacat.nework.domain.model.FeedModelState


class PlayerViewModel: ViewModel() {
    private var _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state

    fun setLoadingState(){
        _state.value = FeedModelState(loading = true)
    }

    fun setErrorState(){
        _state.value = FeedModelState(error = true)
    }

    fun setIdleState(){
        _state.value = FeedModelState(idle = true)
    }
}