package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentVideoPlayerBinding
import ru.javacat.nework.util.DownloadAndSaveImageTask

@AndroidEntryPoint
class VideoPlayerFragment : DialogFragment() {
    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var player: ExoPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoPlayerBinding.inflate(layoutInflater)

        player = ExoPlayer.Builder(requireContext()).build()
        binding.attachVideo.player = player
        val args = arguments
        val url = args?.getString("URL") ?: ""

        // Build the media item.
        val mediaItem = MediaItem.fromUri(url.toUri().toString())
// Set the media item to be played.
        player.setMediaItem(mediaItem)
// Prepare the player.
        player.prepare()
// Start the playback.
        player.play()

        binding.closeBtn.setOnClickListener {
            player.release()
            this.dismiss()
        }

        return binding.root
    }
    override fun onStart() {
        super.onStart()
        dialog!!.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    //    override fun onCreateDialog(
//        savedInstanceState: Bundle?
//    ): Dialog {
//
//        val listener = DialogInterface.OnClickListener{ _, which ->
//            when (which){
//                DialogInterface.BUTTON_NEGATIVE -> this.dismiss()
//                DialogInterface.BUTTON_NEUTRAL ->
//                    this.dismiss()
//            }
//        }
//
//        val builder = AlertDialog.Builder(context)
//            .setView(R.id.videoPlayerFragment)
//            .setNeutralButton("Закрыть", listener)
//            .create()
//        return builder
//    }
}