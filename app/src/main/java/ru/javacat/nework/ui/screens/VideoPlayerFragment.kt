package ru.javacat.nework.ui.screens

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentVideoPlayerBinding
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.ui.viewmodels.PlayerViewModel
import ru.javacat.nework.util.DownloadAndSaveImageTask
import java.lang.Exception

@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {
    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var player: ExoPlayer
    private val viewModel: PlayerViewModel by activityViewModels()

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

        play(player, url)

        viewModel.state.observe(viewLifecycleOwner){
            binding.progressBar.isVisible = it.loading
            if (it == FeedModelState(error = true)){
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        play(player, url)
                    }
                    .show()
            }
        }





        binding.closeBtn.setOnClickListener {
            player.release()
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
//        dialog!!.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
    }

    override fun onStop() {
        super.onStop()
        player.stop()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility =
            View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }

    private fun play(player: ExoPlayer, url: String){
        try {
            viewModel.setLoadingState()
            // Build the media item.
            val mediaItem = MediaItem.fromUri(url.toUri().toString())
// Set the media item to be played.
            player.setMediaItem(mediaItem)
// Prepare the player.
            player.prepare()
// Start the playback.
            viewModel.setIdleState()
            player.play()
        } catch (e: PlaybackException) {
            viewModel.setErrorState()
        }
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