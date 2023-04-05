package ru.javacat.nework.mediaplayer

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MediaLifecycleObserver: LifecycleEventObserver {

    var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun play() {
        if (mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
            //mediaPlayer!!.release()
        } else {
            mediaPlayer?.setOnPreparedListener{
                it.start()
            }
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnCompletionListener {
                it.stop()
                it.reset()

            }
        }

    }

    fun stop(){
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (mediaPlayer!= null){
        when (event){
                Lifecycle.Event.ON_PAUSE -> {
                    if(mediaPlayer!!.isPlaying) mediaPlayer?.pause()
                }
                Lifecycle.Event.ON_STOP -> {
                    if(mediaPlayer!!.isPlaying){
                        mediaPlayer?.stop()
                        mediaPlayer?.reset()
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }

            }
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> {}
        }
        }
    }
}