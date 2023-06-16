package ru.javacat.nework.ui.viewmodels

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.javacat.nework.domain.model.FeedModelState
import java.net.UnknownHostException


class PlayerViewModel: ViewModel() {
    private var _state = MutableLiveData(FeedModelState(idle = true))
    val state: LiveData<FeedModelState>
        get() = _state


    //TODO: Разобраться с эксепшнами и статусом загрузки!
    fun play(player: ExoPlayer, url: String){
        try {
            _state.value = FeedModelState(loading = true)
            // Build the media item.
            val mediaItem = MediaItem.fromUri(url.toUri().toString())
// Set the media item to be played.
            player.setMediaItem(mediaItem)
// Prepare the player.
            player.prepare()
// Start the playback.
            player.play()
            _state.value = FeedModelState(idle = true)
        } catch (e: UnknownHostException){
            _state.value = FeedModelState(error = true)
        }catch (e: PlaybackException) {
            _state.value = FeedModelState(error = true)
        }catch (e: UnknownHostException){
            _state.value = FeedModelState(error = true)
        }
    }
}