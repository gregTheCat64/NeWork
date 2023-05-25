package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.databinding.FragmentVideoPlayerBinding

@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)

        val player = ExoPlayer.Builder(requireContext()).build()

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


        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}